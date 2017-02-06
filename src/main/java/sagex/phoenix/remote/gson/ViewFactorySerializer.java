package sagex.phoenix.remote.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.vfs.views.ViewFactory;

import java.lang.reflect.Type;

/**
 * Created by seans on 01/01/17.
 */
public class ViewFactorySerializer implements JsonSerializer<ViewFactory> {
    @Override
    public JsonElement serialize(ViewFactory view, Type type, JsonSerializationContext ctx) {
        JsonObject o = new JsonObject();
        for (ConfigurableOption c: view.getOptions()) {
            o.add(c.getName(), ctx.serialize(c.value()));
        }
        if (!view.getFolderSources().isEmpty()) {
            //for ()
        }
        return o;
    }
}
