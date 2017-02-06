package sagex.phoenix.remote.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Google can't serialize gneric list??
 * http://stackoverflow.com/a/15557654
 * Created by seans on 01/01/17.
 */
public class InterfaceSerializer<T> implements JsonSerializer<T> {
    public JsonElement serialize(T link, Type type,
                                 JsonSerializationContext context) {
        // Odd Gson quirk
        // not smart enough to use the actual type rather than the interface
        return context.serialize(link, link.getClass());
    }
}