package sagex.phoenix.menu;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;
import sagex.phoenix.util.BaseBuilder;
import sagex.phoenix.util.XmlUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MenuBuilder extends BaseBuilder {
    public static List<Menu> buildMenus(String menuXML, File dtdDir) throws SAXException, FileNotFoundException, IOException {
        return buildMenus(new ByteArrayInputStream(menuXML.getBytes()), dtdDir);
    }

    public static List<Menu> buildMenus(File menuXML, File dtdDir) throws SAXException, FileNotFoundException, IOException {
        return buildMenus(new FileInputStream(menuXML), dtdDir);
    }

    public static List<Menu> buildMenus(InputStream menuXML, File dtdDir) throws SAXException, FileNotFoundException, IOException {
        final File dtd = new File(dtdDir, "menus.dtd");
        EntityResolver er = new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                return new InputSource(new FileInputStream(dtd));
            }
        };

        MenuBuilder builder = new MenuBuilder(menuXML.toString());
        XMLReader reader = XMLReaderFactory.createXMLReader();
        try {
            reader.setFeature("http://xml.org/sax/features/validation", true);
        } catch (SAXException e) {
            e.printStackTrace();
        }
        reader.setContentHandler(builder);
        reader.setErrorHandler(builder);
        reader.setEntityResolver(er);
        reader.parse(new InputSource(menuXML));
        return builder.getMenus();
    }

    private Menu menu;
    private Script script;
    private MenuItem item;
    private SageEvalAction evalAction;

    private SageScreenAction screenAction;
    private ExecuteCommandAction execAction;

    private enum FIELD_STATE {
        MENU, MENUITEM
    }

    private FIELD_STATE fieldState = FIELD_STATE.MENU;
    private String fieldName = null;
    private List<Menu> menus = new ArrayList<Menu>();

    private MenuBuilder(String label) {
        super();
    }

    public List<Menu> getMenus() {
        return menus;
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        if ("menus".equals(name)) {
        } else if ("description".equals(name)) {
        } else if ("menu".equals(name)) {
            menu = new Menu(menu);
            menu.type().setValue(XmlUtil.attr(attributes, "type"));

            menu.label().setValue(XmlUtil.attr(attributes, "label"));
            menu.setName(XmlUtil.attr(attributes, "name", toId(menu.label().get())));
            menu.visible().setValue(XmlUtil.attr(attributes, "visible", "true"));
            menu.icon().setValue(XmlUtil.attr(attributes, "icon"));
            menu.secondaryIcon().setValue(XmlUtil.attr(attributes, "secondaryIcon"));
            menu.background().setValue(XmlUtil.attr(attributes, "background"));
            menu.linkedMenuId().setValue(XmlUtil.attr(attributes, "linkedMenu"));

            fieldState = FIELD_STATE.MENU;
            return;
        } else if ("fragment".equals(name)) {
            menu = new Fragment(menu);
            ((Fragment) menu).setParentMenu(XmlUtil.attr(attributes, "parentMenu"));
            ((Fragment) menu).setInsertBefore(XmlUtil.attr(attributes, "insertBefore"));
            ((Fragment) menu).setInsertAfter(XmlUtil.attr(attributes, "insertAfter"));
            fieldState = FIELD_STATE.MENU;
            return;
        } else if ("script".equals(name)) {
            script = new Script();
            script.setLanguage(XmlUtil.attr(attributes, "language", "JavaScript"));
            return;
        } else if ("menuItem".equals(name)) {
            fieldState = FIELD_STATE.MENUITEM;
            item = new MenuItem(menu);

            item.label().setValue(XmlUtil.attr(attributes, "label"));
            item.setName(XmlUtil.attr(attributes, "name", toId(item.label().get())));
            item.visible().setValue(XmlUtil.attr(attributes, "visible", "true"));
            item.icon().setValue(XmlUtil.attr(attributes, "icon"));
            item.secondaryIcon().setValue(XmlUtil.attr(attributes, "secondaryIcon"));
            item.background().setValue(XmlUtil.attr(attributes, "background"));
            item.linkedMenuId().setValue(XmlUtil.attr(attributes, "linkedMenu"));
            return;
        } else if ("view".equals(name)) {
            fieldState = FIELD_STATE.MENUITEM;
            item = new ViewMenu(menu);
            ViewMenu m = (ViewMenu) item;
            m.setContextVar(XmlUtil.attr(attributes, "contextVar", "VFSMenuMediaFile"));
            m.setPreload(XmlUtil.bool(attributes, "preload", false));
            m.setLimit(NumberUtils.toInt(XmlUtil.attr(attributes, "limit"), 10));

            item.label().setValue(XmlUtil.attr(attributes, "label"));
            item.setName(XmlUtil.attr(attributes, "name", toId(item.label().get())));
            item.visible().setValue(XmlUtil.attr(attributes, "visible", "true"));
            item.icon().setValue(XmlUtil.attr(attributes, "icon"));
            item.secondaryIcon().setValue(XmlUtil.attr(attributes, "secondaryIcon"));
            item.background().setValue(XmlUtil.attr(attributes, "background"));
            item.linkedMenuId().setValue(XmlUtil.attr(attributes, "linkedMenu"));
            return;
        } else if ("eval".equals(name)) {
            // <expression outputVariable="">LaunchMenu("Videos")</expression>
            evalAction = new SageEvalAction();
            evalAction.setOutputVariable(outputVariable(attributes));
            return;
        } else if ("screen".equals(name)) {
            screenAction = new SageScreenAction();
            screenAction.screen().setValue(XmlUtil.attr(attributes, "name"));
            return;
        } else if ("sageCommand".equals(name)) {
            SageCommandAction action = new SageCommandAction();
            action.action().setValue(XmlUtil.attr(attributes, "name"));
            item.addAction(action);
            return;
        } else if ("exec".equals(name)) {
            execAction = new ExecuteCommandAction();
            execAction.setOutputVariable(outputVariable(attributes));
            execAction.setOS(XmlUtil.attr(attributes, "os", null));
            execAction.action().setValue(XmlUtil.attr(attributes, "cmd"));
            execAction.setArgs(XmlUtil.attr(attributes, "args", null));
            execAction.workingDir().setValue(XmlUtil.attr(attributes, "workingDir"));
            return;
        } else if ("field".equals(name)) {
            fieldName = XmlUtil.attr(attributes, "name", null);
        } else {
            error("Unknown Menu Xml Element: " + name);
        }
    }

    private String toId(String text) {
        if (text == null)
            return "0";
        return DigestUtils.md5Hex(text);
    }

    private String outputVariable(Attributes attributes) {
        return XmlUtil.attr(attributes, "outputVariable", null);
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        if ("menu".equals(name) || "fragment".equals(name)) {
            if (menu.getParent() == null) {
                // only add the menu to the "menus" list if it's a parent menu
                // (ie, has no parent)
                menus.add(menu);
            } else {
                // otherwise this is submenu, so add it as an item
                menu.getParent().addItem(menu);
            }
            menu = menu.getParent();
            return;
        }

        if ("script".equals(name)) {
            script.setScript(getData());
            if (item != null) {
                ScriptAction sa = new ScriptAction();
                sa.setScript(script);
                item.addAction(sa);
            } else {
                menu.setScript(script);
            }
            script = null;
            return;
        }

        if ("menuItem".equals(name)) {
            menu.addItem(item);
            item = null;
            return;
        }

        if ("view".equals(name)) {
            menu.addItem(item);
            item = null;
            return;
        }

        if ("eval".equals(name)) {
            evalAction.action().setValue(getData());
            item.addAction(evalAction);
            evalAction = null;
            return;
        }

        if ("screen".equals(name)) {
            item.addAction(screenAction);
            screenAction = null;
            return;
        }

        if ("exec".equals(name)) {
            item.addAction(execAction);
            execAction = null;
            return;
        }

        if ("description".equals(name)) {
            if (fieldState == FIELD_STATE.MENU) {
                menu.description().setValue(getData());
            } else {
                item.description().setValue(getData());
            }
        }

        if ("field".equals(name)) {
            if (fieldState == FIELD_STATE.MENU) {
                menu.field(fieldName, getData());
            } else {
                item.field(fieldName, getData());
            }
        }
    }
}
