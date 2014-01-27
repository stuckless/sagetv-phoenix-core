package sagex.phoenix.remote.streaming;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

public class MediaStreamerManager {
	public static Logger log = Logger.getLogger(MediaStreamerManager.class);
	
	public Map<String, MediaProcess> processes = new HashMap<String, MediaProcess>();
	private MediaStreamerConfig config;
	private MediaProcessFactory factory;
	
	public MediaStreamerManager(MediaStreamerConfig config, MediaProcessFactory processFactory) {
		this.config = config;
		this.factory = processFactory;
	}

	public MediaResponse createRequest(MediaRequest req) {
		MediaResponse resp = new MediaResponse();
		resp.setRequest(req);
		
		// will start a new media streaming service and return the result into the media response
		
		// check if we already have a media process running for this client.
		MediaProcess proc = processes.get(req.getClientId());
		if (proc!=null) {
			if (!proc.getRequest().equals(req)) {
				// we have a client process, so kill it.
				abortProcess(req.getClientId());
				resp.setOldRequest(proc.getRequest());
			}
		}
		
		// make sure the process can access the dirs, etc.
		File dir = new File(req.getOutputDir());
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		log("Using Client output dir " + dir);
		
		// clean the directory contents before we start processing
		try {
			FileUtils.cleanDirectory(dir);
		} catch (Throwable e1) {
			log(e1);
		}
		
		// create a new process and start writing the streamable files
		proc = factory.newProcess(this, req);
		try {
			log("About to create a new process for request: " + req);
			proc.start();
			resp.setControlInfo(proc.getControlInfo());
			processes.put(req.getClientId(), proc);
		} catch (Exception e) {
			log(e);
			resp.setError("Failed to create stream", ExceptionUtils.getFullStackTrace(e));
		}
		
		return resp;
	}

	public MediaStreamerConfig getConfig() {
		return config;
	}

	public MediaProcess getMediaProcess(String clientId) {
		return processes.get(clientId);
	}
	
	public void abortProcess(String clientId) {
		log("Aborting Media Process: " + clientId);
		MediaProcess p = getMediaProcess(clientId);
		if (p!=null) {
			processes.remove(clientId);
			p.abort();
		}
	}
	
	public void log(String msg) {
		log.debug("MediaStreamer: " + msg);
	}
	
	public void log(Throwable e) {
		log.warn("Streamer Error",e);
	}
}
