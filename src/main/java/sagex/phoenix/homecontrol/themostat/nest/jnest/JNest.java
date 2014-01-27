package sagex.phoenix.homecontrol.themostat.nest.jnest;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JNest {

	private Credentials credentials = null;
	private String userAgent = "";
	private String uri = "https://home.nest.com/user/login";
	private LoginResponse loginResponse;
	public boolean isLoggedIn = false;
	public static Gson gson;
	
	public JNest () {
		GsonBuilder gsonb = new GsonBuilder();
		gson = gsonb.create();
	}
	
	public void login() throws IOException {
		URL url;
		
		if (credentials == null)
			throw new IOException("Missing Username and Password for Nest");
		
		Properties properties = credentials.toProperties();
		String query = Util.makeQueryString(properties);
		
		url = new URL(uri);
		HttpsURLConnection urlc = (HttpsURLConnection) url.openConnection();
	    urlc.setRequestMethod("POST"); 
	    urlc.setDoInput(true); 
	    urlc.setDoOutput(true);
	    urlc.setRequestProperty("user-agent", userAgent);
	    urlc.setRequestProperty("Content-length",String.valueOf (query.length())); 
	    urlc.setRequestProperty("Content-Type","application/x-www-form-urlencoded"); 
	    DataOutputStream output = new DataOutputStream( urlc.getOutputStream() );  
	    output.writeBytes(query);
	    
	    switch (urlc.getResponseCode()) {
		    case HttpsURLConnection.HTTP_OK :
		    	handleLoginSuccess(urlc);
		    	break;
		    default :
		    	throw new IOException(urlc.getResponseMessage());
	    }
	}
	
	
	public JsonObject getDeviceStatusInfo () throws IOException {
		if (!isLoggedIn)
			throw new IOException("Not logged in");
		
		URL url;
		HttpsURLConnection urlc;
		
		url = new URL(loginResponse.urls.transport_url+"/v2/mobile/"+loginResponse.user);
		urlc = (HttpsURLConnection) url.openConnection();
	    urlc.setRequestMethod("GET"); 
	    urlc.setDoInput(true); 
	    urlc.setDoOutput(false);
	    urlc.setRequestProperty("user-agent", userAgent);
	    urlc.setRequestProperty("Authorization", "Basic " + loginResponse.access_token);
	    urlc.setRequestProperty("X-nl-user-id", loginResponse.userid);
	    urlc.setRequestProperty("X-nl-protocol-version", "1");
	    
	    switch (urlc.getResponseCode()) {
		    case HttpsURLConnection.HTTP_OK :
		    	return handleStatusSuccess(urlc);
		    default :
		    	throw new IOException(urlc.getResponseMessage());
	    }
	}
	
	public boolean setTemperature (double temp, String deviceId) throws IOException {
		if (!isLoggedIn)
			throw new IOException("Not logged in");
	
		URL url;
		HttpsURLConnection urlc;
		String query;
		
		TempChange req = new TempChange();
		req.target_change_pending = true;
		req.target_temperature = temp;
		query = gson.toJson(req);
		url = new URL(loginResponse.urls.transport_url+"/v2/put/shared."+deviceId);
		urlc = (HttpsURLConnection) url.openConnection();
	    urlc.setRequestMethod("POST"); 
	    urlc.setDoInput(true); 
	    urlc.setDoOutput(true);
	    urlc.setRequestProperty("user-agent", userAgent);
	    urlc.setRequestProperty("Authorization", "Basic " + loginResponse.access_token);
	    urlc.setRequestProperty("X-nl-protocol-version", "1");
	    urlc.setRequestProperty("Content-length",String.valueOf (query.length())); 
	    urlc.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
	    DataOutputStream output = new DataOutputStream( urlc.getOutputStream() );  
	    output.writeBytes(query);
	    
	    switch (urlc.getResponseCode()) {
		    case HttpsURLConnection.HTTP_OK :
		    	return true;
		    default :
		    	throw new IOException(urlc.getResponseMessage());
	    }
		
	}
	
	public boolean setFanMode (FanModeEnum fanMode, String deviceId) throws IOException {
		
		if (!isLoggedIn)
			throw new IOException("Not logged in");
	
		URL url;
		HttpsURLConnection urlc;
		String query;
		
		FanMode req = new FanMode();
		req.fan_mode = fanMode;
		query = gson.toJson(req);
		url = new URL(loginResponse.urls.transport_url+"/v2/put/device."+deviceId);
		urlc = (HttpsURLConnection) url.openConnection();
	    urlc.setRequestMethod("POST"); 
	    urlc.setDoInput(true); 
	    urlc.setDoOutput(true);
	    urlc.setRequestProperty("user-agent", userAgent);
	    urlc.setRequestProperty("Authorization", "Basic " + loginResponse.access_token);
	    urlc.setRequestProperty("X-nl-protocol-version", "1");
	    urlc.setRequestProperty("Content-length",String.valueOf (query.length())); 
	    urlc.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
	    DataOutputStream output = new DataOutputStream( urlc.getOutputStream() );  
	    output.writeBytes(query);
	    
	    switch (urlc.getResponseCode()) {
		    case HttpsURLConnection.HTTP_OK :
		    	return true;
		    default :
		    	throw new IOException(urlc.getResponseMessage());
	    }
	}
	
	public boolean setTemperatureMode (TemperatureModeEnum temperatureMode, String deviceId) throws IOException {
		
		if (!isLoggedIn)
			throw new IOException("Not logged in");
	
		URL url;
		HttpsURLConnection urlc;
		String query;
		
		TemperatureMode req = new TemperatureMode();
		req.target_temperature_type = temperatureMode;
		query = gson.toJson(req);
		url = new URL(loginResponse.urls.transport_url+"/v2/put/shared."+deviceId);
		urlc = (HttpsURLConnection) url.openConnection();
	    urlc.setRequestMethod("POST"); 
	    urlc.setDoInput(true); 
	    urlc.setDoOutput(true);
	    urlc.setRequestProperty("user-agent", userAgent);
	    urlc.setRequestProperty("Authorization", "Basic " + loginResponse.access_token);
	    urlc.setRequestProperty("X-nl-protocol-version", "1");
	    urlc.setRequestProperty("Content-length",String.valueOf (query.length())); 
	    urlc.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
	    DataOutputStream output = new DataOutputStream( urlc.getOutputStream() );  
	    output.writeBytes(query);
	    
	    switch (urlc.getResponseCode()) {
		    case HttpsURLConnection.HTTP_OK :
		    	return true;
		    default :
		    	throw new IOException(urlc.getResponseMessage());
	    }
	}
	
	private void handleLoginSuccess(HttpsURLConnection urlc) throws IOException {
	    StringBuffer buffer = Util.getStringBufferFromResponse(urlc);
        loginResponse = gson.fromJson(buffer.toString(), LoginResponse.class);
        isLoggedIn = true;
	}
	
	private JsonObject  handleStatusSuccess(HttpsURLConnection urlc) throws IOException {
		StringBuffer buffer = Util.getStringBufferFromResponse(urlc);
		JsonParser parser = new JsonParser();
		return parser.parse(buffer.toString()).getAsJsonObject();
	}
	
	public Credentials getCredentials() {
		return credentials;
	}

	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}
}
