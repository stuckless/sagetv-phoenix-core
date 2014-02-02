package sagex.phoenix.homecontrol.themostat.nest.jnest;

import java.util.Properties;

public class Credentials {

	private String userName;
	private String password;

	public Credentials(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Properties toProperties() {
		Properties properties = new Properties();
		properties.setProperty("username", userName);
		properties.setProperty("password", password);
		return properties;
	}

}
