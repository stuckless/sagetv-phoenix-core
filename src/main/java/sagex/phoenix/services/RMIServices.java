package sagex.phoenix.services;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import sagex.api.Configuration;
import sagex.plugin.impl.SagexConfiguration;

/**
 * Phoenix RMI Services
 * 
 * @author seans
 * 
 */
public class RMIServices {
    private static Logger   log      = Logger.getLogger(RMIServices.class);
    private Registry registry = null;
    private List<String> services = new ArrayList<String>();

    public void start() {
        // todo... only start the server and register the services on the
        // phoennix server
        // do not do this for clients, or extenders
        int port = NumberUtils.toInt(Configuration.GetServerProperty(SagexConfiguration.PROP_RMI_PORT, "1098"), 1098);
        try {
            registry = LocateRegistry.getRegistry(port);
        } catch (Exception e) {
            log.warn("No RMI Registry, will need to create it.", e);
            try {
                registry = LocateRegistry.createRegistry(port);
            } catch (Exception ex) {
                log.warn("Failed to create or get access to the RMI registry! Phoenix Remote Services are disabled!", e);
                return;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Remote> void registerServices(Class<T> cls, T impl) {
        if (registry != null) {
            try {
                T stub = (T) UnicastRemoteObject.exportObject(impl, 0);
                registry.rebind(cls.getName(), stub);
                services.add(cls.getName());
            } catch (Exception e) {
                log.error("Failed to bind service: " + cls.getName() + " in RMI Registry");
            }
        }
    }

    public void unregisterServices(String name) {
        if (registry != null) {
            try {
                registry.unbind(name);
                services.remove(name);
            } catch (Exception e) {
                log.error("Failed to unbind service: " + name + " in RMI Registry");
            }
        }
    }

    public void stop() {
        if (registry != null) {
            for (String s: services) {
                unregisterServices(s);
            }
        }
        
        // TODO: stop registry on port
    }
}
