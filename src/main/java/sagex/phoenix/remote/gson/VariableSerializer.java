package sagex.phoenix.remote.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import sagex.phoenix.util.var.Variable;

import java.lang.reflect.Type;

/**
 * Created by seans on 01/01/17.
 */
public class VariableSerializer implements JsonSerializer<Variable> {
    @Override
    public JsonElement serialize(Variable variable, Type type, JsonSerializationContext jsonSerializationContext) {
        return jsonSerializationContext.serialize(variable.get());
    }
}
