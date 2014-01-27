package sagex.phoenix.configuration;

public enum ConfigScope { 
	SERVER, CLIENT, USER;
	
	public static ConfigScope toConfigScope(String scope) {
		if (scope==null||scope.trim().length()==0) return CLIENT;
		for (ConfigScope c: values()) {
			if (c.name().equalsIgnoreCase(scope)) return c;
		}
		return CLIENT;
	}
}