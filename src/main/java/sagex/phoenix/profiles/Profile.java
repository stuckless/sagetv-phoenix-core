package sagex.phoenix.profiles;

import org.apache.commons.lang.builder.ToStringBuilder;

public class Profile {
	private String user;
	private String context;
	private long lastAccessed;

	public Profile(String user) {
		this.user = user;
	}

	public Profile() {
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public long getLastAccessed() {
		return lastAccessed;
	}

	public void setLastAccessed(long lastAccessed) {
		this.lastAccessed = lastAccessed;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
