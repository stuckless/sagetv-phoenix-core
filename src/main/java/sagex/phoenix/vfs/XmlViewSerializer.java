package sagex.phoenix.vfs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import sagex.phoenix.factory.BaseConfigurable;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.Factory;
import sagex.phoenix.util.HasName;
import sagex.phoenix.vfs.filters.AbstractResourceFilterContainer;
import sagex.phoenix.vfs.filters.IResourceFilter;
import sagex.phoenix.vfs.views.ViewFactory;
import sagex.phoenix.vfs.views.ViewPresentation;

public class XmlViewSerializer {
	public void serialize(ViewFactory factory, OutputStream os)
			throws IOException {
		Document doc = DocumentHelper.createDocument();
		doc.setDocType(DocumentFactory.getInstance().createDocType("vfs", null,
				"vfs.dtd"));
		Element vfs = doc.addElement("vfs");
		Element views = vfs.addElement("views");

		serializeView(factory, views);

		XMLWriter writer = new XMLWriter(os, OutputFormat.createPrettyPrint());
		writer.write(doc);
		os.flush();
	}

	private void serializeView(ViewFactory factory, Element viewParent) {
		Element view = viewParent.addElement("view");
		view.addAttribute("name", factory.getName());

		serializeOptions(factory, view);
		
		// description is in the options as well
//		if (!StringUtils.isEmpty(factory.getDescription())) {
//			Element d = viewParent.addElement("description");
//			d.setText(factory.getDescription());
//		}
		
		seriealizeTags(factory.getTags(), view, true);
		serializeSources(factory.getFolderSources(), view);
		serializeViewSources(factory.getViewSources(), view);
		serializeRootFilters(factory.getRootFilters(), view);
		serializePresentations(factory.getViewPresentations(), view);
	}

	private void serializePresentations(Collection<ViewPresentation> viewPresentations, Element view) {
		if (viewPresentations==null) return;
		for (ViewPresentation vp: viewPresentations) {
			Element pres = view.addElement("presentation");
			pres.addAttribute("level", String.valueOf(vp.getLevel()));
			serializeConfigurable(vp.getGroupers(), "group", pres);
			serializeConfigurable(vp.getFilters(), "filter", pres);
			serializeConfigurable(vp.getSorters(), "sort", pres);
		}
	}

	private void serializeConfigurable(List<? extends BaseConfigurable> list, String type, Element view) {
		if (list != null) {
			for (BaseConfigurable f: list) {
					BaseConfigurable bf = (BaseConfigurable) f;
					Element filter = view.addElement(type);
					filter.addAttribute("by", ((HasName)bf).getName());
					serializeOptions(bf, filter);
			}
		}
	}
	
	private void serializeRootFilters(AbstractResourceFilterContainer rootFilters, Element view) {
		if (rootFilters!=null) {
			for (IResourceFilter f: rootFilters) {
				if (f instanceof BaseConfigurable) {
					BaseConfigurable bf = (BaseConfigurable) f;
					Element filter = view.addElement("filter");
					filter.addAttribute("by", ((HasName)bf).getName());
					serializeOptions(bf, filter);
				}
			}
		}
	}

	private void serializeViewSources(List<ViewFactory> viewSources, Element view) {
		if (viewSources.size()>0) {
			for (ViewFactory f: viewSources) {
				Element src = view.addElement("view-source");
				src.addAttribute("name", f.getName());
				serializeOptions(f, src);
			}
		}
	}

	private void serializeSources(List<Factory<IMediaFolder>> folderSources, Element parent) {
		if (folderSources.size()>0) {
			for (Factory f: folderSources) {
				Element src = parent.addElement("source");
				src.addAttribute("name", f.getName());
				serializeOptions(f, src);
			}
		}
	}

	private void seriealizeTags(Set<String> tags, Element parent, boolean tagOnly) {
		for (String t: tags) {
			Element el = parent.addElement("tag");
			el.addAttribute("value", t);
		}
	}

	private void serializeOptions(BaseConfigurable options,
			Element optionsParent) {
		if (options != null && options.getOptionNames().size() > 0) {
			List<String> names = options.getOptionNames();
			Collections.sort(names);
			for (String s : names) {
				// do seriealize the name option
				if ("name".equals(s)) continue;
				
				ConfigurableOption opt = options.getOption(s);
				if (!StringUtils.isEmpty(opt.value().getValue())) {
					Element o = optionsParent.addElement("option");
					o.addAttribute("name", s);
					o.addAttribute("value", opt.value().getValue());

					if (opt.getDataType() != null) {
						o.addAttribute("datatype", opt.getDataType().name());
					}
				}
			}
		}
	}
}
