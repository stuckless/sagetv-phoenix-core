package sagex.phoenix.remote;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import sagex.phoenix.factory.Factory;

public class FactorySerializer implements JsonSerializer<Factory<?>> {
    public FactorySerializer() {
    }

    @Override
    public JsonElement serialize(Factory<?> f, Type arg1, JsonSerializationContext ctx) {
        JsonObject root = new JsonObject();
        root.addProperty("name", f.getName());
        root.addProperty("label", f.getLabel());
        root.addProperty("description", f.getDescription());
        return root;
    }
}
