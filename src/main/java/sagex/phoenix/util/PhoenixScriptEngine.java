package sagex.phoenix.util;

import java.io.File;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

/**
 * Creates a scripting engine that is capable of executing scripts (ie,
 * javascript, python, groovy, etc) while allowing access to Phoenix and the
 * SageTV apis.
 *
 * @author seans
 */
public class PhoenixScriptEngine {
    protected ScriptEngineManager manager = new ScriptEngineManager();
    protected ScriptEngine engine = null;
    protected Invocable invokable = null;

    /**
     * Creates a Phoenix Scripting Engine that gets an engine based on the
     * extension of the file. ie, a file.js would return a JavaScript engine;
     * file.groovy would return a Groovy engine, etc. The contents of the file
     * is NOT added to the script engine. You still need to call addScript() to
     * add a script to the script engine.
     *
     * @param file
     */
    public PhoenixScriptEngine(File file) {
        init(file);
    }

    /**
     * Initializes the engine using a file (ie, the script type of the file).
     * The contents of the file is NOT added to the ScriptEngine. This just
     * initializes the engine type based on the file type
     *
     * @param file
     */
    protected void init(File file) {
        engine = manager.getEngineByExtension(FilenameUtils.getExtension(file.getName()));
        if (engine instanceof Invocable) {
            invokable = (Invocable) engine;
        }
        addGlobals(engine);
    }

    /**
     * Initialized the engine using specific script type
     *
     * @param scriptType
     */
    protected void init(String scriptType) {
        engine = manager.getEngineByName(scriptType);
        if (engine instanceof Invocable) {
            invokable = (Invocable) engine;
        }
        addGlobals(engine);
    }

    /**
     * Creates a Phoenix Scripting Engine that gets an engine based on the
     * specified named scripting engine, ie, "JavaScript", "Python", etc.
     *
     * @param scriptType
     */
    public PhoenixScriptEngine(String scriptType) {
        init(scriptType);
    }

    public PhoenixScriptEngine() {
        this("JavaScript");
    }

    public void addScript(String script) throws ScriptException {
        engine.eval(script);
    }

    public Object evalScript(String script) throws ScriptException {
        return engine.eval(script);
    }

    public <T> T getInterface(Class<T> type) {
        if (invokable != null) {
            return invokable.getInterface(type);
        }
        return null;
    }

    public ScriptEngine getEngine() {
        return engine;
    }

    public ScriptEngineManager getManager() {
        return manager;
    }

    protected void addGlobals(ScriptEngine engine) {
        Bindings b = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        b.put("MediaFileAPI", new sagex.api.MediaFileAPI());
        b.put("Global", new sagex.api.Global());
        b.put("Utility", new sagex.api.Utility());
        b.put("PlaylistAPI", new sagex.api.PlaylistAPI());
        b.put("AiringAPI", new sagex.api.AiringAPI());
        b.put("AlbumAPI", new sagex.api.AlbumAPI());
        b.put("CaptureDeviceAPI", new sagex.api.CaptureDeviceAPI());
        b.put("CaptureDeviceInputAPI", new sagex.api.CaptureDeviceInputAPI());
        b.put("ChannelAPI", new sagex.api.ChannelAPI());
        b.put("Configuration", new sagex.api.Configuration());
        b.put("Database", new sagex.api.Database());
        b.put("FavoriteAPI", new sagex.api.FavoriteAPI());
        b.put("MediaPlayerAPI", new sagex.api.MediaPlayerAPI());
        b.put("SeriesInfoAPI", new sagex.api.SeriesInfoAPI());
        b.put("ShowAPI", new sagex.api.ShowAPI());
        b.put("TranscodeAPI", new sagex.api.TranscodeAPI());
        b.put("TVEditorialAPI", new sagex.api.TVEditorialAPI());
        b.put("WidgetAPI", new sagex.api.WidgetAPI());
        b.put("Version", new sagex.api.Version());
        b.put("SageAPI", new sagex.SageAPI());
        b.put("phoenix", new phoenix.api());
        b.put("log", Logger.getLogger("sagex.phoenix.scripts"));
    }
}
