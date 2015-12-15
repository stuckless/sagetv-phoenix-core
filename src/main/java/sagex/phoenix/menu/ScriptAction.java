package sagex.phoenix.menu;

import sagex.phoenix.util.PhoenixScriptEngine;

import javax.script.ScriptException;

public class ScriptAction extends Action {
    private Script script = null;
    private PhoenixScriptEngine engine = null;

    public ScriptAction() {
    }

    public void setScript(Script script) {
        this.script = script;
        this.engine = new PhoenixScriptEngine(script.getLanguage());
    }

    @Override
    public boolean invoke() {
        try {
            engine.evalScript(script.getScript());
            return true;
        } catch (ScriptException e) {
            log.error("ScriptException in script: " + script);
            throw new RuntimeException(script.getScript(), e);
        } catch (Throwable t) {
            log.error("Failed to invlude script: " + script, t);
            return false;
        }
    }

    public Script getScript() {
        return script;
    }
}
