package sagex.phoenix.vfs.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.util.CloneUtil;
import sagex.phoenix.util.ElapsedTimer;
import sagex.phoenix.util.Loggers;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaFolder;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.IMediaResourceVisitor;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.VirtualMediaFolder;
import sagex.phoenix.vfs.filters.AbstractResourceFilterContainer;
import sagex.phoenix.vfs.filters.AndResourceFilter;
import sagex.phoenix.vfs.filters.Filter;
import sagex.phoenix.vfs.filters.FilterFactory;
import sagex.phoenix.vfs.filters.IResourceFilter;
import sagex.phoenix.vfs.visitors.DebugVisitor;

public  class ViewFactory extends Factory<ViewFolder> {
	/**
	 * {@value}
	 */
	public static final String STORE_ID = "phoenix.views";
	
	/**
	 * {@value}
	 */
	public static final String FIELD_VISIBLE = "visible";

	
	/**
	 * Flatten the view, by fetching all items.
	 * 
	 * {@value}
	 */
    public static final String OPT_FLAT = "flat";

    /**
	 * For each source and view, don't add the source and folder, but rather the children
	 * 
	 * {@value}
	 */
    public static final String OPT_CHILDREN = "children-only";

    /**
	 * prune single item folders option name
	 * {@value}
	 */
	public static final String OPT_PRUNE_SINGLE_ITEM_FOLDERS = "prune-single-item-folders";

	/** 
	 * Root path for the folder.  For exmaple a folder may have a heirachy of /Vidoes/Movies/DVDs/ but
	 * you only want to present the view at the DVDs folder, so you set the root to /Vidoes/Movies/DVDs
	 * 
	 * {@value}
	 */
	public static final String OPT_ROOT = "root";

	/** 
	 * Bookmark path for the folder.  This is similar to root in that it will attempt to find the path
	 * of the folder, but the returned value will still have a parent, so that the user can navigate
	 * up the heirarchy.  When root is used, the returned view does not have a parent.
	 * 
	 * {@value}
	 */
	public static final String OPT_BOOKMARK = "bookmark";
	
	private List<Factory<IMediaFolder>> folderSources = new ArrayList<Factory<IMediaFolder>>();
    private List<ViewFactory> viewSources = new ArrayList<ViewFactory>();
    
    private Map<Integer, ViewPresentation> viewPresentations = new HashMap<Integer, ViewPresentation>();
    
    private ArrayList<FilterFactory> rootFilterFactories = new ArrayList<FilterFactory>();
    private AbstractResourceFilterContainer rootFilters = new AndResourceFilter();
    
    public ViewFactory() {
    	super();
    	addOption(new ConfigurableOption(OPT_FLAT, "Flatten", "true", DataType.bool, true, ListSelection.single, "true:Yes,false:No"));
    	addOption(new ConfigurableOption(OPT_CHILDREN, "Children Only", "false", DataType.bool, true, ListSelection.single, "true:Yes,false:No"));
		addOption(new ConfigurableOption(OPT_PRUNE_SINGLE_ITEM_FOLDERS, "Prune Single Item Folders", null, DataType.bool, true, ListSelection.single, "true:Yes,false:No"));
		addOption(new ConfigurableOption(OPT_ROOT, "Path to the Root Folder", null, DataType.string));
		addOption(new ConfigurableOption(OPT_BOOKMARK, "Position the view at the Bookmark location", null, DataType.string));
		//addOption(new ConfigurableOption(OPT_FETCH_ITEMS, "Before Building View, Fetch ALL Items", null, DataType.bool, true, ListSelection.single, "true:Yes,false:No"));
    }

    private void flatten(final VirtualMediaFolder parent, IMediaFolder vmf) {
        // TODO: We need to optimize this
        // This basically ensures that all the files are flattened/resolved BEFORE
        // we build the view.  We do this so that grouping/filtering works correctly.
        vmf.accept(new IMediaResourceVisitor() {
			@Override
			public boolean visit(IMediaResource res, IProgressMonitor monitor) {
				if (res instanceof IMediaFile) {
					parent.addMediaResource(res);
				}
				return true;
			}
		}, null, IMediaResource.DEEP_UNLIMITED);
    }
    
    public ViewFolder create(Set<ConfigurableOption> options) {
        ElapsedTimer timer =new ElapsedTimer();
        Set<String> tags = new TreeSet<String>(getTags());
        
        log.info("Creating view for: " + getLabel() + "; id: " + getName() + "; Options: " + options);
        
        VirtualMediaFolder vmf = new VirtualMediaFolder(null, getLabel());
        for (Factory<IMediaFolder> f : folderSources) {
        	// SEAN: Removed adding Tags, since Views will define their own tags
            // add all tags from this source, even if the source is empty
            // tags.addAll(f.getTags());
            
            if (isFlat()) {
                log.debug("Adding Source: " + f.getName() + " to view: " + getName() + "; Will flatten.");
                ElapsedTimer t2 = new ElapsedTimer();
                IMediaFolder folder = f.create(options);
                log.info("Created Source: " + f.getLabel() + " in " + t2.delta() + "ms");
                if (folder==null || folder.getChildren().size()==0) {
                    log.warn("Source: " + f.getName() + " was empty");
                } else {
                	flatten(vmf, folder);
                    //for (IMediaResource r : folder.getChildren()) {
                    //    vmf.addMediaResource(r);
                    //}
                }
            } else {
                log.debug("Adding Source: " + f.getName() + " to view: " + getName());
                ElapsedTimer t2 = new ElapsedTimer();
                IMediaFolder folder = f.create(options);
                log.info("Created Source: " + f.getLabel() + " in " + t2.delta() + "ms");
                if (folder==null || folder.getChildren().size()==0) {
                    log.warn("Source: " + f.getName() + " was empty");
                } else {
                	if (isChildrenOnly()) {
                		addChildren(vmf, folder);
                	} else {
                		vmf.addMediaResource(folder);
                	}
                }
            }
        }

        for (ViewFactory f : viewSources) {
            IMediaFolder view = f.create(options);
            if (view==null || view.getChildren().size()==0) {
                log.warn("view Source: " + f.getName() + " was empty");
                continue;
            }
            
            if (isFlat()) {
                log.info("Adding View Source: " + f.getName() + " to view: " + getName() + "; Will flatten.");
                flatten(vmf, view);
            } else {
                log.info("Adding View Source: " + f.getName() + " to view: " + getName());
                if (isChildrenOnly()) {
                	addChildren(vmf, view);
                } else {
                	vmf.addMediaResource(view);
                }
            }
        }
        
        if (Loggers.VFS_LOG.isDebugEnabled()) {
            DebugVisitor walk = new DebugVisitor();
            vmf.accept(walk, null, IMediaResource.DEEP_UNLIMITED);
            Loggers.VFS_LOG.debug("Dumping RAW Items for view factory: " + getLabel() + "\n" + walk.toString());
        }
        
        if (rootFilterFactories.size()>0 || rootFilters.getFilterCount()>0) {
        	AndResourceFilter filters = new AndResourceFilter();
        	
	        // if there are root filters (as factories) added to the
	        // view, then turn them into filters first
        	for (FilterFactory f: rootFilterFactories) {
        		Filter filter = f.create(null);
        		filters.addFilter(filter);
        	}

        	for (IResourceFilter f: rootFilters) {
        		filters.addFilter(f);
        	}
	        
	        if (filters.getFilterCount()>0) {
	        	log.info("applying root filters...");
	        	VirtualMediaFolder vmfFiltered = new VirtualMediaFolder(vmf.getTitle());
	        	for (IMediaResource r: vmf) {
	        		if (filters.accept(r)) {
	        			vmfFiltered.addMediaResource(r);
	        		}
	        	}
	        	vmf = vmfFiltered;
	        }
        }
        
        IMediaFolder rootFolder = vmf;
        String root = getOption(OPT_ROOT).getString(null);
        if (root==null) {
        	ConfigurableOption opt = findOption(OPT_ROOT, options);
        	if (opt!=null) {
        		root=opt.getString(null);
        	}
        }
        
        if (root!=null) {
        	log.debug("Attempting to re-root the folder at " + root);
        	try {
        		IMediaFolder f = (IMediaFolder) vmf.findChild(root);
        		if (f==null) {
        			log.warn("Failed to find the path " + root + " in view " + getLabel() + "; Using entire view.");
        		} else {
        			log.debug("Folder rooted at " + root);
        			rootFolder = f;
        		}
        	} catch (Exception e) {
        		log.warn("Failed to find root of view for path " + root);
        	}
        }
        
        boolean isOnline = rootFolder.isType(MediaResourceType.ONLINE.value());
        // create a ViewFolder using this factory as a reference
        ViewFolder folder;
		try {
			if (isOnline) {
		        log.debug("Creating ONLINE ViewFolder for Online Items");
				folder = new OnlineViewFolder((ViewFactory) this.clone(), 0, null, rootFolder);
			} else {
		        log.debug("Creating the ViewFolder");
				folder = new ViewFolder((ViewFactory) this.clone(), 0, null, rootFolder);
			}
	        folder.getTags().addAll(tags);

	        // see if the view folder is bookmarked
	        String bookmark = getOption(OPT_BOOKMARK).getString(null);
	        if (bookmark==null) {
	        	ConfigurableOption opt = findOption(OPT_BOOKMARK, options);
	        	if (opt!=null) {
	        		bookmark=opt.getString(null);
	        	}
	        }
	        
	        if (bookmark!=null) {
	        	log.debug("Attempting to find bookmark at " + bookmark);
	        	try {
	        		IMediaFolder f = (IMediaFolder) folder.findChild(bookmark);
	        		if (f==null) {
	        			log.warn("Failed to find the path " + bookmark + " in view " + getLabel() + "; Using entire view.");
	        		} else {
	        			log.debug("Folder bookmarked at " + bookmark);
	        			folder = (ViewFolder) f;
	        		}
	        	} catch (Exception e) {
	        		log.warn("Failed to find bookmar of view for path " + bookmark, e);
	        	}
	        }

	        if (Loggers.VFS_LOG.isDebugEnabled()) {
	            DebugVisitor walk = new DebugVisitor();
	            folder.accept(walk, null, IMediaResource.DEEP_UNLIMITED);
	            Loggers.VFS_LOG.debug("Dumping ViewFolder for view factory: " + getLabel() + "\n" + walk.toString());
	        }
	        
	        log.info("Created view: " + folder.getTitle() + " in " + timer.delta() + "ms");
	        return folder;
		} catch (CloneNotSupportedException e) {
			log.warn("Failed to create View: " + getName(), e);
			return null;
		}
    }

    private void addChildren(VirtualMediaFolder vmf, IMediaFolder folder) {
    	if (folder!=null) {
	        for (IMediaResource r : folder.getChildren()) {
	            vmf.addMediaResource(r);
	        }
    	}
	}

	public List<Factory<IMediaFolder>> getFolderSources() {
        return folderSources;
    }

    public List<ViewFactory> getViewSources() {
        return viewSources;
    }

    public void addViewSource(ViewFactory source) {
        if (source==null) {
            log.warn("Attepted to add a null source", new Exception());
            return;
        }
        this.viewSources.add(source);
        //noinheritable tags
        //this.addTags(source.getTags());
    }
    
    public void addFolderSource(Factory<IMediaFolder> source) {
        if (source==null) {
            log.warn("Attepted to add a null source", new Exception());
            return;
        }
        this.folderSources.add(source);
        // no ineritable tags
        //this.addTags(source.getTags());
    }

    /**
     * @return the flat
     */
    public boolean isFlat() {
    	return getOption(OPT_FLAT).getBoolean(true);
    }
    
    public boolean isChildrenOnly() {
    	return getOption(OPT_CHILDREN).getBoolean(true);
    }

	public Collection<ViewPresentation> getViewPresentations() {
		return viewPresentations.values();
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		ViewFactory v = (ViewFactory) super.clone();
		v.folderSources = CloneUtil.cloneList(folderSources);
		v.viewPresentations = new HashMap<Integer, ViewPresentation>();
		for (Map.Entry<Integer, ViewPresentation> p : viewPresentations.entrySet()) {
			v.viewPresentations.put(p.getKey(), (ViewPresentation)p.getValue().clone());
		}
		v.viewSources = CloneUtil.cloneList(viewSources);
		v.rootFilters = new AndResourceFilter();
		for (IResourceFilter f: rootFilters) {
			v.rootFilters.addFilter((IResourceFilter)((Filter)f).clone());
		}
		return v;
	}
	
	/**
	 * Get the View Presentation for the given View Depth or level.  If there isn't a
	 * configured view, then an empty default view is returned.  This method never returns null.
	 * 
	 * @param level view level starting at 1
	 * @return a {@link ViewPresentation} instance for the given view level
	 */
	public ViewPresentation getViewPresentation(int level) {
		ViewPresentation p = viewPresentations.get(level);
		if (p==null) {
			if (level<=0) {
				// store a new instance for all views at this level
				p = new ViewPresentation();
				p.setLevel(level);
				viewPresentations.put(level, p);
			} else {
				p = getViewPresentation(level-1);
			}
		}
		return p;
	}

	/**
	 * Returns true if there is a presentation set for the given level.
	 * @param level view level starting at 0
	 * @return
	 */
	public boolean hasViewPresentation(int level) {
		ViewPresentation p = viewPresentations.get(level);
		return p!=null;
	}

	/**
	 * Adds a Root Filter... ie a Filter that is applied to the items before
	 * they are passed to the view folder during creation.
	 * 
	 * @param filter
	 */
	public void addRootFilter(Filter filter) {
		rootFilters.addFilter(filter);
	}

	/**
	 * Adds a Root Filter Factory... ie a Filter that is applied to the items before
	 * they are passed to the view folder during creation.
	 * 
	 * @param filter
	 */
	public void addRootFilterFactory(FilterFactory filter) {
		rootFilterFactories.add(filter);
	}

	
	@Override
	public String toString() {
		return "ViewFactory [" + "name: " + getName() + ", " + (folderSources != null ? "folderSources=" + folderSources.size() + ", " : "")
				+ (rootFilters != null ? "rootFilters=" + rootFilters.getFilterCount() + ", " : "")
				+ (viewPresentations != null ? "viewPresentations=" + viewPresentations.size() + ", " : "")
				+ (viewSources != null ? "viewSources=" + viewSources.size() + ", " : "") + "toString()=" + super.toString() + "]";
	}

	public void addViewPresentations(ViewPresentation presentation) {
		viewPresentations.put(presentation.getLevel(), presentation);
	}

	public AbstractResourceFilterContainer getRootFilters() {
		return rootFilters;
	}
}
