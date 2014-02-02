package sagex.phoenix.configuration.proxy;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import sagex.phoenix.configuration.ConfigType;
import sagex.phoenix.configuration.Field;
import sagex.phoenix.configuration.Group;
import sagex.phoenix.configuration.StaticOptionsFactory;
import sagex.phoenix.util.Loggers;

public class GroupParser {
	public static Group parseGroup(Class cl) {
		if (!GroupProxy.class.isAssignableFrom(cl)) {
			throw new RuntimeException("Not Configured! Class should be a SubClass of GroupProxy: " + cl.getName());
		}

		AGroup g = (AGroup) cl.getAnnotation(AGroup.class);
		if (g == null) {
			throw new RuntimeException("Missing @Group annotation for " + cl.getName());
		}

		GroupProxy groupProxy = null;
		try {
			groupProxy = (GroupProxy) cl.newInstance();
		} catch (Throwable e) {
			throw new RuntimeException("Unable to create new instance of " + cl.getName(), e);
		}

		// define the group metadata
		Group gr = new Group(g.path());
		gr.setLabel(g.label());
		gr.setDescription(g.description());

		// define the group field metadata
		for (java.lang.reflect.Field f : cl.getDeclaredFields()) {
			if (!FieldProxy.class.isAssignableFrom(f.getType())) {
				throw new RuntimeException("Field: " + f.getName() + " in ProxyGroup: " + cl.getName()
						+ " is not a FieldProxy class!");
			}

			AField annFld = f.getAnnotation(AField.class);
			if (annFld == null) {
				throw new RuntimeException("Missing @Field for field: " + f.getName() + " in GroupProxy: " + cl.getName());
			}

			String groupId = gr.getId();
			Field fld = new Field();
			String id = null;
			if (!annFld.fullKey().equals(AField.USE_PARENT_GROUP)) {
				id = annFld.fullKey();
			} else {
				if (annFld.name().equals(AField.USE_FIELD_NAME)) {
					id = groupId + "/" + f.getName();
				} else {
					id = groupId + "/" + annFld.name();
				}
			}
			fld.setId(id);
			fld.setDescription(annFld.description());
			fld.setLabel(annFld.label());
			fld.setScope(annFld.scope());
			try {
				f.setAccessible(true);
				fld.setDefaultValue(((FieldProxy) f.get(groupProxy)).getDefaultValueAsString());
			} catch (Throwable t) {
				throw new RuntimeException("Failed to get default value for: " + f.getName() + " in ProxyGroup: " + cl.getName(), t);
			}

			if (!StringUtils.isEmpty(annFld.visible())) {
				fld.setIsVisible(annFld.visible());
			}

			if (!StringUtils.isEmpty(annFld.listSeparator())) {
				fld.setListSeparator(annFld.listSeparator());
			}

			if (annFld.list().length() > 0) {
				fld.setOptionFactory(new StaticOptionsFactory(annFld.list()));
				if (StringUtils.isEmpty(fld.getListSeparator())) {
					throw new RuntimeException("Missing listSeparator on group " + cl.getName() + "; Field: " + f.getName());
				}

				// hint this this should be a choice
				fld.setType(ConfigType.CHOICE);
			}

			if (!StringUtils.isEmpty(annFld.hints())) {
				String hints[] = annFld.hints().split("\\s*,\\s*");
				for (String h : hints) {
					fld.getHints().setBooleanHint(h, true);
				}
			}

			// set the config type if it has not been set already
			if (fld.getType() == ConfigType.TEXT) {
				fld.setType(annFld.type());
			}

			// adjust based on return type...
			try {
				Object val = ((FieldProxy) f.get(groupProxy)).getConverter().toType(null);
				if (val != null) {
					if (Number.class.isAssignableFrom(val.getClass())) {
						fld.setType(ConfigType.NUMBER);
					} else if (Boolean.class.isAssignableFrom(val.getClass())) {
						fld.setType(ConfigType.BOOL);
					}
				}
			} catch (Throwable e) {
				// just assume text
				Loggers.LOG.warn("GroupParser: Can't determine type for " + fld.getId());
			}

			gr.addElement(fld);
		}

		if (gr.getChildren().length == 0) {
			throw new RuntimeException("Didn't add any Children to ProxyGroup for class: " + cl.getName());
		}

		return gr;
	}

	public void save() throws IOException {
	}
}
