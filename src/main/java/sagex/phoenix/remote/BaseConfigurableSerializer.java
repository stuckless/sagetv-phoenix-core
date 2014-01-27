package sagex.phoenix.remote;

import java.lang.reflect.Type;

import sagex.phoenix.factory.BaseConfigurable;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.util.HasLabel;
import sagex.phoenix.util.HasName;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class BaseConfigurableSerializer implements JsonSerializer<BaseConfigurable> {
	public BaseConfigurableSerializer() {
	}

	@Override
	public JsonElement serialize(BaseConfigurable bc, Type arg1, JsonSerializationContext ctx) {
		JsonObject root = new JsonObject();
		root.addProperty("_class", bc.getClass().getName());
		JsonObject metadata = new JsonObject();
		root.add("_metadata", metadata);
		
		if (bc instanceof HasName) {
			root.addProperty("name", ((HasName)bc).getName());
		}

		if (bc instanceof HasLabel) {
			root.addProperty("label", ((HasLabel)bc).getLabel());
		}
		
		for (String s: bc.getOptionNames()) {
			// add option/value
			ConfigurableOption opt = bc.getOption(s);
			root.addProperty(s, opt.getString(null));
			
			// add metadata
			JsonObject copt = new JsonObject();
			copt.addProperty("label", opt.getLabel());
			copt.addProperty("type", opt.getDataType().toString());
			metadata.addProperty(s, opt.getString(null));			
			metadata.add(s, copt);
		}
		return root;
	}
}
