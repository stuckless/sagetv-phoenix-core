package sagex.phoenix.menu;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import sagex.phoenix.util.var.DynamicVariable;

public class XmlMenuSerializer {
    public XmlMenuSerializer() {
    }

    public void serialize(Menu menu, OutputStream os) throws IOException {
        Document doc = createDocument();

        serializeMenu(menu, doc.getRootElement());

        writeDocument(doc, os);
    }

    private Document createDocument() {
        Document doc = DocumentHelper.createDocument();
        doc.setDocType(DocumentFactory.getInstance().createDocType("menus", null, "menus.dtd"));
        doc.addElement("menus");
        return doc;
    }

    private void writeDocument(Document doc, OutputStream os) throws IOException {
        XMLWriter writer = new XMLWriter(os, OutputFormat.createPrettyPrint());
        writer.write(doc);
        os.flush();
    }

    public void serializeFragment(IMenuItem item, String parentMenu, String insertBefore, String insertAfter, OutputStream os)
            throws IOException {
        Document doc = createDocument();
        Element menus = doc.getRootElement();
        Element fragment = menus.addElement("fragment");
        fragment.addAttribute("parentMenu", parentMenu);
        if (!StringUtils.isEmpty(insertBefore)) {
            fragment.addAttribute("insertBefore", insertBefore);
        }
        if (!StringUtils.isEmpty(insertAfter)) {
            fragment.addAttribute("insertAfter", insertAfter);
        }

        if (item instanceof Menu) {
            serializeMenu((Menu) item, fragment);
        } else {
            serializeMenuItem(item, fragment);
        }

        writeDocument(doc, os);
    }

    private void serializeMenu(Menu menu, Element parent) {
        Element el = parent.addElement("menu").addAttribute("name", menu.getName());
        if (menu.type().getValue() != null) {
            el.addAttribute("type", menu.type().getValue());
        }
        serializeCommon(menu, el);

        List<IMenuItem> items = menu.getItems();
        if (items.size() > 0) {
            for (IMenuItem mi : items) {
                if (mi instanceof ViewMenu) {
                    serializeViewMenu((ViewMenu) mi, el);
                } else if (mi instanceof Menu) {
                    serializeMenu((Menu) mi, el);
                } else {
                    serializeMenuItem(mi, el);
                }
            }
        }
    }

    private void serializeViewMenu(ViewMenu menu, Element parent) {
        Element el = parent.addElement("view").addAttribute("name", menu.getName());
        if (menu.type().getValue() != null) {
            el.addAttribute("type", menu.type().getValue());
        }
        el.addAttribute("preload", String.valueOf(menu.isPreloaded()));
        el.addAttribute("contextVar", menu.getContextVar());
        serializeCommon(menu, el);

        if (menu.getActions().size() > 0) {
            for (Action a : menu.getActions()) {
                serializeAction(menu, a, el);
            }
        }
    }

    private void serializeMenuItem(IMenuItem mi, Element parent) {
        Element el = parent.addElement("menuItem");
        if (mi.getName() != null) {
            el.addAttribute("name", mi.getName());
        }
        serializeCommon(mi, el);

        if (mi.getActions().size() > 0) {
            for (Action a : mi.getActions()) {
                serializeAction(mi, a, el);
            }
        }
    }

    public void serializeAction(IMenuItem mi, Action a, Element parent) {
        if (a instanceof SageScreenAction) {
            serializeAction(mi, (SageScreenAction) a, parent);
        } else if (a instanceof SageCommandAction) {
            serializeAction(mi, (SageCommandAction) a, parent);
        } else if (a instanceof SageEvalAction) {
            serializeAction(mi, (SageEvalAction) a, parent);
        } else if (a instanceof ExecuteCommandAction) {
            serializeAction(mi, (ExecuteCommandAction) a, parent);
        } else if (a instanceof ScriptAction) {
            serializeAction(mi, (ScriptAction) a, parent);
        } else {
            throw new UnsupportedOperationException("Action not supported: " + a);
        }
    }

    public void serializeAction(IMenuItem mi, ScriptAction a, Element parent) {
        Element el = parent.addElement("script");
        if (a.getScript() != null) {
            if (!StringUtils.isEmpty(a.getScript().getLanguage())) {
                el.addAttribute("language", a.getScript().getLanguage());
            }

            if (!StringUtils.isEmpty(a.getScript().getScript())) {
                el.addCDATA(a.getScript().getScript());
            }
        }
    }

    public void serializeAction(IMenuItem mi, SageCommandAction a, Element parent) {
        parent.addElement("sageCommand").addAttribute("name", a.action().getValue());
    }

    public void serializeAction(IMenuItem mi, SageEvalAction a, Element parent) {
        Element el = parent.addElement("eval");
        if (a.outputVariable != null) {
            el.addAttribute("outputVariable", a.outputVariable);
        }
        el.addCDATA(a.action().getValue());
    }

    public void serializeAction(IMenuItem mi, SageScreenAction a, Element parent) {
        parent.addElement("screen").addAttribute("name", a.action().getValue());
    }

    public void serializeAction(IMenuItem mi, ExecuteCommandAction a, Element parent) {
        Element el = parent.addElement("exec");
        if (a.getOS() != null) {
            el.addAttribute("os", a.getOS());
        }
        if (a.getOutputVariable() != null) {
            el.addAttribute("outputVariable", a.getOutputVariable());
        }
        if (a.getArgs() != null) {
            el.addAttribute("args", a.getArgs());
        }
        if (a.workingDir().getValue() != null) {
            el.addAttribute("workingDir", a.workingDir().getValue());
        }
        el.addAttribute("cmd", a.action().getValue());
    }

    public void serializeCommon(IMenuItem item, Element el) {
        if (item.background().getValue() != null) {
            el.addAttribute("background", item.background().getValue());
        }
        if (item.icon().getValue() != null) {
            el.addAttribute("icon", item.icon().getValue());
        }
        if (item.label().getValue() != null) {
            el.addAttribute("label", item.label().getValue());
        }
        if (item.linkedMenuId().getValue() != null) {
            el.addAttribute("linkedMenu", item.linkedMenuId().getValue());
        }
        if (item.secondaryIcon().getValue() != null) {
            el.addAttribute("secondaryIcon", item.secondaryIcon().getValue());
        }
        if (item.visible().getValue() != null) {
            el.addAttribute("visible", item.visible().getValue());
        }
        if (item.isDefault().getValue() != null) {
            el.addAttribute("isDefault", item.isDefault().getValue());
        }

        if (item.description().getValue() != null) {
            Element desc = el.addElement("description");
            desc.addCDATA(item.description().getValue());
        }

        if (((MenuItem) item).getFields().size() > 0) {
            serializeFields(((MenuItem) item).getFields(), el);
        }
    }

    private void serializeFields(Map<String, DynamicVariable<String>> fields, Element parent) {
        Element field = parent.addElement("field");
        for (Map.Entry<String, DynamicVariable<String>> me : fields.entrySet()) {
            field.addAttribute("name", me.getKey());
            field.addText(me.getValue().getValue());
        }
    }
}
