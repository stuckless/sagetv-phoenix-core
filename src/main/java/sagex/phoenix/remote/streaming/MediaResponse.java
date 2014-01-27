package sagex.phoenix.remote.streaming;

import java.io.Serializable;

public class MediaResponse implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private MediaRequest oldRequest = null;
	private MediaControlInfo controlInfo;
	private MediaRequest request;

	private String shortErrorMessage;
	private String longErrorMessage;
	
	public MediaResponse() {
	}

	public MediaRequest getOldRequest() {
		return oldRequest;
	}

	public void setOldRequest(MediaRequest oldRequest) {
		this.oldRequest = oldRequest;
	}

	public void setControlInfo(MediaControlInfo controlInfo) {
		this.controlInfo = controlInfo;
	}
	
	public MediaControlInfo getControlInfo() {
		return controlInfo;
	}

	public void setRequest(MediaRequest req) {
		this.request=req;
	}

	public MediaRequest getRequest() {
		return request;
	}

	public void setError(String shortErorrMessage, String longErrorMessage) {
		this.shortErrorMessage=shortErorrMessage;
		this.longErrorMessage = longErrorMessage;
	}

	public String getShortErrorMessage() {
		return shortErrorMessage;
	}

	public void setShortErrorMessage(String shortErrorMessage) {
		this.shortErrorMessage = shortErrorMessage;
	}

	public String getLongErrorMessage() {
		return longErrorMessage;
	}

	public void setLongErrorMessage(String longErrorMessage) {
		this.longErrorMessage = longErrorMessage;
	}

}
