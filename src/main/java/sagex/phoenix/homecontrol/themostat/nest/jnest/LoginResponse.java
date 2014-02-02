package sagex.phoenix.homecontrol.themostat.nest.jnest;

public class LoginResponse {
	public boolean is_superuser;
	public boolean is_staff;
	public String access_token;
	public String userid;
	public String expires_in;
	public String email;
	public String user;
	public URLs urls;
	public Limits limits;

	public LoginResponse() {
	}
}
