package sagex.phoenix.remote.services;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import sagex.SageAPI;
import sagex.phoenix.Phoenix;
import sagex.phoenix.util.PhoenixScriptEngine;
import sagex.util.ILog;
import sagex.util.LogProvider;

public class ScriptingServiceFactory {
    // if the script has an initialize method, then call it, when we load the
    // script
    public static interface HasOnLoad {
        public void __onLoad();
    }

    private static class ScriptingServicePackage {
        private String name;
        private File file;
        private PhoenixScriptEngine engine;
        private long lastModified;
    }

    private ILog log = LogProvider.getLogger(ScriptingServiceFactory.class);
    private File serviceDir = null;
    private Map<String, ScriptingServicePackage> servicePackages = null;

    public ScriptingServiceFactory() {
    }

    public void initialize() {
        serviceDir = new File(Phoenix.getInstance().getPhoenixUserDir(), "services/js");
        if (!serviceDir.exists()) {
            serviceDir.mkdirs();
        }
        servicePackages = new HashMap<String, ScriptingServicePackage>();
    }

    public Object callService(String client, String serviceName, Object... args) throws ScriptException, IOException,
            NoSuchMethodException {
        return callService(null, client, serviceName, args);
    }

    public Object callService(String context, String client, String serviceName, Object... args) throws ScriptException,
            IOException, NoSuchMethodException {
        ScriptingServicePackage sp = servicePackages.get(client);
        if (sp == null) {
            sp = new ScriptingServicePackage();
            sp.name = client;
            sp.engine = new PhoenixScriptEngine();
            sp.file = new File(serviceDir, client + ".js");
            if (!sp.file.exists()) {
                throw new RuntimeException("Invalid Service Package: " + client + "; Missing Service File: "
                        + sp.file.getAbsolutePath());
            }
            servicePackages.put(client, sp);
        }

        if (sp.file.lastModified() > sp.lastModified) {
            FileReader fr = null;
            try {
                fr = new FileReader(sp.file);
                sp.engine.addScript(IOUtils.toString(fr));
                try {
                    HasOnLoad onload = sp.engine.getInterface(HasOnLoad.class);
                    log.debug("Calling onload for script " + sp.file);
                    onload.__onLoad();
                } catch (Throwable t) {
                    // just means that script doesn't have an onload
                }
            } finally {
                fr.close();
            }
            sp.lastModified = sp.file.lastModified();
        }

        if (context != null) {
            SageAPI.setUIContext(context);
        }

        Invocable inv = (Invocable) sp.engine.getEngine();
        return inv.invokeFunction(serviceName, args);
    }

    /**
     * Invokes a function in the given client scripting engine
     *
     * @param sp
     * @param function
     * @param args
     * @return
     * @throws NoSuchMethodException
     * @throws ScriptException
     */
    public Object invoke(ScriptingServicePackage sp, String function, Object... args) throws NoSuchMethodException, ScriptException {
        Invocable inv = (Invocable) sp.engine;
        try {
            return inv.invokeFunction(function, args);
        } catch (NoSuchMethodException e) {
            log.warn("No Such Function " + function + " in script " + sp.file + " with args " + args, e);
            throw e;
        } catch (ScriptException e) {
            log.warn("Script Execution Error when calling " + function + " in script " + sp.file + " with args " + args, e);
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Registers a new script into the engine for the client
     *
     * @param client
     * @param script
     * @throws IOException
     */
    public void registerScript(String client, String script) throws IOException {
        File f = new File(serviceDir, client + ".js");
        FileUtils.writeStringToFile(f, script);
    }

    /**
     * Returns the last modified data for the given client script
     *
     * @param client
     * @return
     */
    public long getLastModifiedForScript(String client) {
        ScriptingServicePackage sp = servicePackages.get(client);
        if (sp == null)
            return 0;
        if (sp.file == null || !sp.file.exists())
            return 0;
        return sp.file.lastModified();
    }

    /**
     * Return an Interface instance that can map the inteface method call to the
     * javascript method call.
     *
     * @param interfaceDef
     * @param client
     * @param interfaceMethodName
     * @param jsMethodName
     * @return
     */
    public <T> T newInterfaceHandler(Class<T> interfaceDef, String client, String interfaceMethodName, String jsMethodName) {
        return (T) Proxy.newProxyInstance(interfaceDef.getClassLoader(), new Class<?>[]{interfaceDef},
                new JSMethodInvocationHandler(servicePackages.get(client).engine, interfaceMethodName, jsMethodName));
    }
}
