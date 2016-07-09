package sagex.phoenix.vfs.sorters;

import java.util.Comparator;

import javax.script.ScriptException;

import sagex.phoenix.util.PhoenixScriptEngine;
import sagex.phoenix.vfs.IMediaResource;

public class JavascriptComparator implements Comparator<IMediaResource> {
    private Comparator<IMediaResource> comparator = null;
    private PhoenixScriptEngine engine = null;

    @SuppressWarnings("unchecked")
    public JavascriptComparator(String script) throws ScriptException {
        engine = new PhoenixScriptEngine();
        engine.addScript(script);
        comparator = engine.getInvokable().getInterface(Comparator.class);
    }

    public int compare(IMediaResource arg0, IMediaResource arg1) {
        if (comparator==null) {
            System.out.println("JS Engine didn't create Comparator interface");
            try {
                return (Integer)engine.getInvokable().invokeFunction("compare", arg0, arg1);
            } catch (ScriptException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            return 0;
        } else {
            return comparator.compare(arg0, arg1);
        }
    }
}
