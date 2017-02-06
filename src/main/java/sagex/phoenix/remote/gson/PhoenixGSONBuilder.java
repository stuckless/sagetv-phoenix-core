package sagex.phoenix.remote.gson;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import sagex.phoenix.menu.Action;
import sagex.phoenix.menu.IMenuItem;
import sagex.phoenix.remote.MediaResourceSerializer;
import sagex.phoenix.util.var.Variable;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.views.ViewFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by seans on 01/01/17.
 */
public class PhoenixGSONBuilder {
    public static Options newOptions() {
        return new Options();
    }

    public static class Options {
        public boolean prettyPrint=false;
        public List<String> skipFields = new ArrayList<>(Arrays.asList("parent","repository","log"));
        public Options prettyPrint(boolean b) {
            prettyPrint=b;
            return this;
        }
        public Options skipField(String name) {
            skipFields.add(name);
            return this;
        }
    }

    static Gson gsonInstance;
    static Gson gsonPrettyInstance;
    static Options defaultOptions = new Options();

    public static Gson getGsonInstance() {
        if (gsonInstance==null) {
            gsonInstance = getNewInstance(defaultOptions);
        }
        return gsonInstance;
    }

    public static Gson getGsonPrettyInstance() {
        if (gsonPrettyInstance==null) {
            gsonPrettyInstance = getNewInstance(newOptions().prettyPrint(true));
        }
        return gsonPrettyInstance;
    }

    public static Gson getNewInstance(Options options) {
        if (options==null) options=defaultOptions;

        final Options finalOptions = options;
        GsonBuilder gb = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return (finalOptions.skipFields.contains(f.getName()));
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> arg0) {
                        return false;
                    }
                });

        if (options.prettyPrint) {
            gb.setPrettyPrinting();
        }

        // for menus
        gb = gb.registerTypeHierarchyAdapter(Variable.class, new VariableSerializer());
        gb = gb.registerTypeAdapter(IMenuItem.class, new InterfaceSerializer<>());
        gb = gb.registerTypeAdapter(Action.class, new InterfaceSerializer<>());

        // for media files
        gb = gb.registerTypeHierarchyAdapter(IMediaResource.class, new MediaResourceSerializer());

        // for view factorie
        gb = gb.registerTypeAdapter(ViewFactory.class, new ViewFactorySerializer());

        return gb.create();
    }
}
