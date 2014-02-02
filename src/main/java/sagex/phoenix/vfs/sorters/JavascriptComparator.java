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
		comparator = engine.getInterface(Comparator.class);
	}

	public int compare(IMediaResource arg0, IMediaResource arg1) {
		return comparator.compare(arg0, arg1);
	}
}
