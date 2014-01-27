package sagex.phoenix.util;

import java.io.File;
import java.io.IOException;

import javax.script.ScriptException;

import org.apache.commons.io.FileUtils;

/**
 * A script engine that manages a
 * script file.  ie, it will take care of reloading the file and recreating
 * the environment in the event that the script file changes.
 * 
 * You just need to call getObjectInstance() any time you need to access the
 * typed object instance.
 * 
 * @author seans
 *
 * @param <T>
 */
public class PhoenixManagedScriptEngine<T> extends PhoenixScriptEngine {
	protected File file;
	protected long lastModified = -1000;
	protected T instance;
	protected Class<T> instanceType;
	
	public PhoenixManagedScriptEngine(File file, Class<T> interfaceType) {
		this.file=file;
		this.instanceType = interfaceType;
	}

	/**
	 * initialized the scripting engine if it has changed
	 */
	public void init() {
		if (file.lastModified()>lastModified) {
			lastModified=file.lastModified();
			setupEnvironment();
		}
	}
	
	/**
	 * Subsclassed can override this to add additional environment to the script
	 * engine.  You must call super() first.
	 */
    protected void setupEnvironment() {
    	init(file);
    	addScript(file);
    	instance = getInterface(instanceType);
	}

    /**
     * Adds a script file to the engine.
     * 
     * @param file
     * @throws RuntimeException
     */
	protected void addScript(File file) throws RuntimeException {
    	try {
			addScript(FileUtils.readFileToString(file));
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * The Object instance for the managed type and script that was passed
	 * in the constructor of this class.
	 * @return
	 */
	public T getObjectInstance() {
		init();
		return instance;
	}
}
