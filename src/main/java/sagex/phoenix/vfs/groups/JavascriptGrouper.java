package sagex.phoenix.vfs.groups;

import sagex.phoenix.util.PhoenixScriptEngine;
import sagex.phoenix.vfs.IMediaResource;

/**
 * A Grouper that will group using a Sage expression.
 * 
 * @author seans
 * 
 */
public class JavascriptGrouper implements IGrouper {
	private IGrouper grouper = null;
	private PhoenixScriptEngine engine = null;

	public JavascriptGrouper(String expr) throws Throwable {
		engine = new PhoenixScriptEngine();
		engine.addScript(expr);
		grouper = engine.getInterface(IGrouper.class);
	}

	public String getGroupName(IMediaResource res) {
		return grouper.getGroupName(res);
	}
}
