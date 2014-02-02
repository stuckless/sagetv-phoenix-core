package sagex.phoenix.configuration.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sagex.UIContext;
import sagex.api.Configuration;
import sagex.phoenix.configuration.ConfigScope;
import sagex.phoenix.configuration.IConfigurationProvider;
import sagex.phoenix.profiles.Profile;

public class SageConfigurationProvider implements IConfigurationProvider {
	private Logger log = Logger.getLogger(SageConfigurationProvider.class);

	public String getProperty(ConfigScope scope, String key) {
		if (scope == ConfigScope.CLIENT) {
			return Configuration.GetProperty(UIContext.getCurrentContext(), key, null);
		} else if (scope == ConfigScope.SERVER) {
			return Configuration.GetServerProperty(key, null);
		} else if (scope == ConfigScope.USER) {
			return Configuration.GetServerProperty(getUserKey(key), null);
		} else {
			log.warn("Invalid Configuration Scope: " + scope);
			return Configuration.GetProperty(UIContext.getCurrentContext(), key, null);
		}
	}

	public void setProperty(ConfigScope scope, String key, String val) {
		if (scope == ConfigScope.CLIENT) {
			Configuration.SetProperty(UIContext.getCurrentContext(), key, val);
		} else if (scope == ConfigScope.SERVER) {
			Configuration.SetServerProperty(key, val);
		} else if (scope == ConfigScope.USER) {
			Configuration.SetServerProperty(getUserKey(key), val);
		} else {
			log.warn("Invalid Configuration Scope: " + scope);
			Configuration.SetProperty(UIContext.getCurrentContext(), key, val);
		}
	}

	public Iterator<String> keys(ConfigScope scope) {
		if (scope == ConfigScope.CLIENT || scope == ConfigScope.SERVER) {
			// client server use the same method for getting property keys...
			// but it's not right for server
			// don't know how to get just server properties
			List<String> propList = new ArrayList<String>();
			addKeys(propList, null);
			return propList.iterator();
		} else if (scope == ConfigScope.USER) {
			return ConfigurationUtils.filterKeys(getUserKey(""), keys(ConfigScope.SERVER)).iterator();
		} else {
			log.warn("Invalid Configuration Scope: " + scope);
			return null;
		}
	}

	private String getUserKey(String key) {
		Profile p = phoenix.profile.GetCurrentUserProfile();
		return getUserKey(p.getUser(), key);
	}

	public String getUserKey(String user, String key) {
		return "phoenix/users/" + user + "/" + key;
	}

	public Iterator<String> userKeys(String user) {
		return ConfigurationUtils.filterKeys(getUserKey(user, ""), keys(ConfigScope.SERVER)).iterator();
	}

	private void addKeys(List<String> propList, String parent) {
		if (parent == null) {
			parent = "phoenix";
		}

		String keys[] = Configuration.GetSubpropertiesThatAreLeaves(parent);
		if (keys != null) {
			for (String k : keys) {
				propList.add(parent + "/" + k);
			}
		}

		keys = Configuration.GetSubpropertiesThatAreBranches(parent);
		if (keys != null) {
			for (String k : keys) {
				addKeys(propList, parent + "/" + k);
			}
		}
	}

	public void cloneUserProperties(String srcUser, String destUser) {
		for (Iterator<String> i = userKeys(srcUser); i.hasNext();) {
			String key = i.next();
			String value = Configuration.GetServerProperty(key, null);
			if (!StringUtils.isEmpty(value)) {
				String srcUserKey = getUserKey(srcUser, "");
				key = key.substring(srcUserKey.length());
				String destUserKey = getUserKey(destUser, key);
				Configuration.SetServerProperty(destUserKey, value);
			}
		}
	}
}
