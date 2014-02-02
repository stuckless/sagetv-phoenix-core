package phoenix.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sagex.api.Configuration;
import sagex.api.Global;
import sagex.api.Utility;
import sagex.phoenix.tools.annotation.API;
import sagex.phoenix.util.PropertiesUtils;

/**
 * Manages the extender names as configured in the Sage.properties
 * sagex/uicontexts/ entries
 * 
 * @author sean
 */
@API(group = "client")
public class ClientAPI {
	private static final String SAGEX_CONTEXTS = "sagex/uicontexts";
	private Logger log = Logger.getLogger(this.getClass());

	/**
	 * Returns a Set of the UI Context Names for the currently connected
	 * clients. This call polls the currently active clients each time it is
	 * called.
	 * 
	 * @return
	 */
	public Set<String> GetConnectedClients() {
		Set<String> clients = new TreeSet<String>();
		String cs[] = Global.GetUIContextNames();
		if (cs != null) {
			for (String c : cs) {
				clients.add(c);
			}
		}
		cs = Global.GetConnectedClients();
		if (cs != null) {
			for (String c : cs) {
				clients.add(c);
			}
		}

		return clients;
	}

	/**
	 * Returns the extender/client names. It may be empty, but it will never be
	 * null. The key is the client id (/IP:PORT) or UI Contenxt Name.
	 * 
	 * @return
	 * @deprecated Client Names are stored in Sage.properties under
	 *             sagex/uicontexts
	 */
	public Map LoadWebServerExtenderNames() {
		Properties clientNames = new Properties();
		File clientPropFile = new File("webserver/extenders.properties");
		// check to see if can load from the server...
		String data = Utility.GetFileAsString(clientPropFile);
		if (data != null) {
			log.info("Loading Remote copy of the extender properties...");
			// load read-only copy from the server
			try {
				PropertiesUtils.load(clientNames, new ByteArrayInputStream(data.getBytes()));
			} catch (IOException e) {
				log.warn("Failed to load the remote properties");
			}
		}
		return clientNames;
	}

	/**
	 * Returns the name for the given client id or ui context name
	 * 
	 * @return
	 */
	public String GetName(String ctx) {
		if (ctx == null)
			return null;
		return Configuration.GetServerProperty(getContextKeyName(ctx), ctx);
	}

	/**
	 * Set the human readable name for the given client id or context.
	 * 
	 * @param ctx
	 * @param name
	 */
	public synchronized void SetName(String ctx, String name) {
		if (ctx == null)
			return;
		Configuration.SetServerProperty(getContextKeyName(ctx), name);
	}

	/**
	 * Returns true if the client id or context is currently connected. It's a
	 * fairly expensive operation, since it checks this id against all connected
	 * ids.
	 * 
	 * @param ctx
	 */
	public boolean IsConnected(String ctx) {
		if (ctx == null)
			return false;
		return GetConnectedClients().contains(ctx);
	}

	/**
	 * Removes a client from the extender names
	 * 
	 * @param ctx
	 */
	public synchronized void Remove(String ctx) {
		if (ctx == null)
			return;
		Configuration.RemoveServerProperty(getContextKeyName(ctx));
	}

	private String getContextKeyName(String extender) {
		return SAGEX_CONTEXTS + "/" + extender + "/name";
	}

	/**
	 * Returns the configured Extender names from the Sage.propertues
	 * sagex/uicontexts
	 * 
	 * @return
	 */
	public Map GetExtenderNames() {
		Map<String, String> names = new HashMap<String, String>();
		// names.putAll(LoadWebServerExtenderNames());
		String keys[] = Configuration.GetServerSubpropertiesThatAreBranches(SAGEX_CONTEXTS);
		if (keys != null) {
			for (String k : keys) {
				String name = Configuration.GetServerProperty(getContextKeyName(k), null);
				if (!StringUtils.isEmpty(name)) {
					names.put(k, name);
				}
			}
		}
		return names;
	}
}
