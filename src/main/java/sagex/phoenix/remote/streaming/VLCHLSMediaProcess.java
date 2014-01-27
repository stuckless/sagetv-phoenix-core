package sagex.phoenix.remote.streaming;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import sagex.phoenix.remote.streaming.GenericCommandMediaProcess.Env;
import sagex.phoenix.util.Loggers;
import sagex.phoenix.util.TextReplacement;

public class VLCHLSMediaProcess extends MediaProcess {
	public VLCHLSMediaProcess(MediaStreamerManager manager, MediaRequest req) {
		super(manager, req);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void putEnv(Map map, String name, String value) {
		if (!StringUtils.isEmpty(value)) {
			map.put(name,value);
		}
	}

	// 
	// vlc -I dummy --mms-caching 0 "$1" vlc://quit --sout=#transcode{vcodec=h264,vb=256}:std{access=livehttp{seglen=10,delsegs=true,numsegs=5,index=/home/sls/WWW/mystream.m3u8,index-url=http://192.168.1.12:8000/mystream-########.ts},mux=ts{use-key-frames},dst=/home/sls/WWW/mystream-########.ts}
	@Override
	public synchronized void start() throws Exception {
		mediaManager.log("VLC Process Manager starting...");
		MediaStreamerConfig config = mediaManager.getConfig();
		List<String> cmd = new ArrayList<String>();
		cmd.add(config.getVLCConfig().getCommand());
		cmd.add("-I");
		cmd.add("dummy");
		cmd.add("--mms-caching");
		cmd.add("0");
		
		if (!StringUtils.isEmpty(config.getVLCConfig().getExtraArgs().get())) {
			try {
				// split the args and add them.
				cmd.addAll(Arrays.asList(config.getVLCConfig().getExtraArgs().get().split("\\s*")));
			} catch (Exception e) {
				mediaManager.log(e);
			}
		}
		
		// add in the file(s)
		for (String f: getRequest().getSources()) {
			cmd.add(f);
		}
		
		cmd.add("vlc://quit");

		StringBuilder sout = new StringBuilder("--sout=");

		String profileId = request.getNetwork() + "_" + request.getProfile();
		String vlcProfile = config.getVLCConfig().getProfile(profileId);
		if (vlcProfile == null) {
			getMediaManager().log("Missing Profile for " + profileId + "; using default profile");
			vlcProfile = config.getVLCConfig().getDefault_profile().get();
		}
		
		Map<String, String> env = new HashMap<String, String>();
		putEnv(env, Env.BASE_URL, getRequest().getBaseUrl());
		putEnv(env, Env.CLIENT_ID, getRequest().getClientId());
		putEnv(env, Env.MEDIA_ID, getRequest().getMediaId());
		putEnv(env, Env.PLAYLIST_FILE, getIndexFile());
		putEnv(env, Env.OUTPUT_DIR, getRequest().getOutputDir());
		vlcProfile = TextReplacement.replaceVariables(vlcProfile, env);
		sout.append(vlcProfile);
		
		cmd.add(sout.toString());
		
		// set the mediaurl in the control info
		controlInfo.setMediaUrl(getIndexUrl());
		controlInfo.setLockFile(getIndexFile());
		
		String cmdString = Arrays.toString(cmd.toArray());
		mediaManager.log("VLC Command: " + cmdString);
		
		// write the command to the log
		File logFile = getOutputLogFile();
		try {
			FileUtils.writeStringToFile(logFile, Arrays.toString(cmd.toArray()) + "\n");
			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.redirectErrorStream(true);
			pb.redirectOutput(Redirect.to(logFile));
			process = pb.start();
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder("===== ERROR =======\n");
			sb.append(cmdString).append("\n");
			sb.append(ExceptionUtils.getFullStackTrace(e)).append("\n");
			FileUtils.writeStringToFile(logFile, sb.toString());
		}
		
		mediaManager.log("VLC Process Started");
	}

	private File getOutputLogFile() {
		return new File(getRequest().getOutputDir(), getStreamName() + ".out.log");
	}

	private String getSegmentUrl() {
		return request.getBaseUrl() + getDestFilenameForSegment();
	}

	private String getIndexUrl() {
		return request.getBaseUrl() + getStreamName() + ".m3u8";
	}

	private String getIndexFile() {
		return new File(getRequest().getOutputDir(), getStreamName() + ".m3u8").getAbsolutePath();
	}

	private String getFullDestFilenameForSegment() {
		File f = new File(getRequest().getOutputDir(), getDestFilenameForSegment());
		return f.toString();
	}
	
	private String getDestFilenameForSegment() {
		return getStreamName() + "-########.ts";
	}

	private String getStreamName() {
		return getRequest().getMediaId();
	}
}
