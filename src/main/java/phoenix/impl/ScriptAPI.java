package phoenix.impl;

import java.io.IOException;

import org.apache.log4j.Logger;

import sagex.phoenix.Phoenix;
import sagex.phoenix.tools.annotation.API;
import sagex.phoenix.util.PhoenixScriptEngine;

/**
 * General Purpose Utility Methods
 * 
 * @author seans
 */
@API(group = "script")
public class ScriptAPI {
	private static final Logger log = Logger.getLogger(ScriptAPI.class);

	public Object Eval(String javascript) {
		try {
			PhoenixScriptEngine pe = new PhoenixScriptEngine();
			return pe.evalScript(javascript);
		} catch (Throwable e) {
			log.error("Failed to Execute Script", e);
			log.error("SCRIPT: " + javascript);
			throw new RuntimeException(e);
		}
	}

	public Object Eval(String script, String functionCall) {
		try {
			PhoenixScriptEngine pe = new PhoenixScriptEngine();
			pe.addScript(script);
			return pe.evalScript(functionCall);
		} catch (Throwable e) {
			log.error("Failed to Execute Script", e);
			log.error("SCRIPT: " + script);
			log.error("SCRIPT FUNCTION: " + functionCall);
			throw new RuntimeException(e);
		}
	}

	/**
	 * registers a new client script. This will replace and overwrite any
	 * existing script for that client
	 * 
	 * @param client
	 * @param script
	 * @return
	 * @throws IOException
	 */
	public void RegisterScript(String client, String script) {
		try {
			Phoenix.getInstance().getScriptingServices().registerScript(client, script);
		} catch (IOException e) {
			log.error("Failed to Register Script for " + client, e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the last modified date for script for the given client
	 * 
	 * @param client
	 * @return
	 */
	public long ScriptLastModified(String client) {
		return Phoenix.getInstance().getScriptingServices().getLastModifiedForScript(client);
	}
}
