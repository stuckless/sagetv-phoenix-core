package sagex.phoenix.menu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sagex.phoenix.Phoenix;
import sagex.phoenix.common.SystemConfigurationFileManager;
import sagex.phoenix.menu.Menu.Insert;
import sagex.phoenix.node.INodeVisitor;
import sagex.phoenix.util.FileUtils;

public class MenuManager extends SystemConfigurationFileManager implements SystemConfigurationFileManager.ConfigurationFileVisitor {
	public static final Logger log = Logger.getLogger(MenuManager.class);

	private Map<String, Menu> indexed = new HashMap<String, Menu>();
	private List<Menu> menus = new LinkedList<Menu>();
	private List<Fragment> fragments = new LinkedList<Fragment>();

	public MenuManager(File systemDir, File userDir) {
		super(systemDir, userDir, new SuffixFileFilter(".xml", IOCase.INSENSITIVE));
	}

	public Menu getMenu(String name) {
		if (name == null)
			return null;

		return indexed.get(name);
	}

	public synchronized void saveMenu(Menu menu) throws IOException {
		File menusDir = getUserFiles().getDir();
		if (!menusDir.exists()) {
			FileUtils.mkdirsQuietly(menusDir);
		}

		File menufile = new File(menusDir, menu.getName() + ".xml");
		if (menufile.exists()) {
			log.warn("Menu Exists: " + menufile + "; Overwriting Menus.");
		}

		log.info("Saving Menu: " + menufile);
		FileOutputStream fos = new FileOutputStream(menufile);
		XmlMenuSerializer ser = new XmlMenuSerializer();
		ser.serialize(menu, fos);
		fos.flush();
		fos.close();

		if (getMenu(menu.getName()) == null) {
			log.info("Adding new menu to the Menu Manager: " + menu.getName());
			addMenu(menu, ConfigurationType.User);
		}
	}

	public List<Menu> getMenus() {
		return menus;
	}

	@Override
	public void loadConfigurations() {
		log.info("Begin Loading Menus");
		menus.clear();
		indexed.clear();
		fragments.clear();

		accept(this);

		for (Menu m : menus) {
			indexMenu(m);
		}

		log.info("Adjusting menu item visibility based on stored settings");
		updateMenuDetails(menus);

		log.info("Processing Menu Fragments...");
		processFragments(fragments);

		log.info("Ordering Menu Items...");
		orderMenuItems(menus);

		log.info("End Loading Menus");
	}

	private void indexMenu(Menu m) {
		if (!indexed.containsKey(m.getName())) {
			indexed.put(m.getName(), m);
		}

		for (IMenuItem i : m) {
			if (i instanceof Menu) {
				indexMenu((Menu) i);
			}
		}
	}

	private void orderMenuItems(List<Menu> menus) {
		for (Menu m : menus) {
			m.visit(new INodeVisitor<IMenuItem>() {
				@Override
				public void visit(IMenuItem node) {
					if (node instanceof Menu) {
						((Menu) node).sortItems();
					}
				}
			});
		}
	}

	private void processFragments(List<Fragment> frags) {
		for (Fragment f : frags) {
			processFragment(f);
		}
	}

	private void processFragment(Fragment frag) {
		Menu m = getMenu(frag.getParentMenu());
		if (m == null) {
			log.warn("Can't process fragment: " + frag.getParentMenu() + " since the parent menu was not found");
			return;
		}

		for (IMenuItem item : frag.getItems()) {
			if (StringUtils.isEmpty(frag.getInsertAfter()) && StringUtils.isEmpty(frag.getInsertBefore())) {
				log.info("Fragment: Replacing Menu Item: " + item.getName());
				if (!m.replaceItem(m.getItemByName(item.getName()), item)) {
					log.warn("Failed to replace item: " + item.getName() + " in menu " + m.getName());
				}
			} else if (!StringUtils.isEmpty(frag.getInsertAfter())) {
				log.info("Fragment: Insert Menu Item: " + item.getName() + " after " + frag.getInsertAfter());
				m.addItem(item, frag.getInsertAfter(), Insert.after);
			} else if (!StringUtils.isEmpty(frag.getInsertBefore())) {
				log.info("Fragment: Insert Menu Item: " + item.getName() + " before " + frag.getInsertBefore());
				m.addItem(item, frag.getInsertBefore(), Insert.before);
			} else {
				log.info("Fragment: Skipping Menu Item: " + item.getName() + " no instructions??");
			}
		}
	}

	private void updateMenuDetails(@SuppressWarnings("rawtypes") List items) {
		for (Object o : items) {
			if (o instanceof IMenuItem) {
				final IMenuItem i = (IMenuItem) o;
				i.visit(new INodeVisitor<IMenuItem>() {
					@Override
					public void visit(IMenuItem node) {
						try {
							boolean vis = phoenix.menu.IsVisible(i);
							if (!vis) {
								i.visible().set(vis);
							}
						} catch (Throwable t) {
							log.warn("Failed to set visibility for menu item " + i, t);
						}
					}
				});
			}
		}
	}

	@Override
	public void visitConfigurationFile(ConfigurationType type, File file) {
		try {
			log.info("Loading Menu: " + file.getAbsolutePath());

			for (Menu m : MenuBuilder.buildMenus(file, getSystemFiles().getDir())) {
				addMenu(m, type);
			}
		} catch (Exception e) {
			log.error("Error loading menu file: " + file, e);
			Phoenix.fireError("Error in Menu File: " + file + "; " + e.getMessage(), e);
		}
	}

	public synchronized void addMenu(Menu menu, ConfigurationType type) {
		if (menu instanceof Fragment) {
			fragments.add((Fragment) menu);
			return;
		}

		Menu old = getMenu(menu.getName());
		if (old != null) {
			log.info("Replacing " + type + " menu: " + menu.getName() + " with new menu " + menu.getChildCount() + " items");
			removeMenu(old);
		} else {
			log.info("Adding new " + type + " menu: " + menu.getName() + " with new menu " + menu.getChildCount() + " items");
		}
		menus.add(menu);
		indexed.put(menu.getName(), menu);
	}

	private Menu removeMenu(Menu menu) {
		if (menu == null)
			return null;
		String name = menu.getName();
		log.info("Removing menu " + name);
		indexed.remove(name);
		for (int i = 0; i < menus.size(); i++) {
			Menu m = menus.get(i);
			if (name.equals(m.getName())) {
				menus.remove(i);
				return m;
			}
		}
		log.error("Menu: " + name + " does not exist!");
		return null;
	}
}
