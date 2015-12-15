package sagex.phoenix.remote.streaming;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.*;

public class GenericCommandMediaProcess extends MediaProcess {
    public static interface Prop {
        // fromt he control file
        public static final String control_media_url = "media_url";
        public static final String control_media_file = "media_file";
    }

    public static interface Env {
        public static final String BASE_URL = "PMS_BASE_URL";
        public static final String CLIENT_ID = "PMS_CLIENT_ID";
        public static final String MEDIA_ID = "PMS_MEDIA_ID";
        public static final String PLAYLIST_FILE = "PMS_PLAYLIST_FILE";
        public static final String CONTROL_FILE = "PMS_CONTROL_FILE";
        public static final String CLIENT_NETWORK = "PMS_CLIENT_NETWORK";
        public static final String PROFILE_NAME = "PMS_PROFILE_NAME";
        public static final String CLIENT_SCREEN = "PMS_CLIENT_SCREEN";
        public static final String OUTPUT_DIR = "PMS_OUTPUT_DIR";
    }

    public GenericCommandMediaProcess(MediaStreamerManager manager, MediaRequest req) {
        super(manager, req);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void putEnv(Map map, String name, String value) {
        if (!StringUtils.isEmpty(value)) {
            map.put(name, value);
        }
    }

    @Override
    public synchronized void start() throws Exception {
        MediaStreamerConfig config = mediaManager.getConfig();
        String scrptName = config.getScriptConfig().getCommand();
        mediaManager.log("Generic Media Process Manager starting using command/script: " + scrptName);
        List<String> cmd = new ArrayList<String>();
        cmd.add(scrptName);

        // add in the file(s)
        for (String f : getRequest().getSources()) {
            cmd.add(f);
        }

        mediaManager.log("Generic Command: " + Arrays.toString(cmd.toArray()));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        pb.redirectOutput(Redirect.to(getOutputLogFile()));

        pb.directory(new File(getRequest().getOutputDir()));
        Map<String, String> env = pb.environment();
        putEnv(env, Env.BASE_URL, getRequest().getBaseUrl());
        putEnv(env, Env.CLIENT_ID, getRequest().getClientId());
        putEnv(env, Env.MEDIA_ID, getRequest().getMediaId());
        putEnv(env, Env.PLAYLIST_FILE, getIndexFile());
        putEnv(env, Env.CONTROL_FILE, getControlFile());
        putEnv(env, Env.CLIENT_NETWORK, getRequest().getNetwork());
        putEnv(env, Env.CLIENT_SCREEN, getRequest().getClientScreen());
        putEnv(env, Env.PROFILE_NAME, getRequest().getProfile());
        putEnv(env, Env.OUTPUT_DIR, getRequest().getOutputDir());

        process = pb.start();

        controlInfo.setMediaUrl(getIndexUrl());
        controlInfo.setLockFile(getIndexFile());

        File f = new File(getControlFile());
        if (f.exists()) {
            Properties props = new Properties();
            props.load(new FileInputStream(f));
            if (props.containsKey(Prop.control_media_url)) {
                controlInfo.setMediaUrl(props.getProperty(Prop.control_media_url));
                controlInfo.setLockFile(props.getProperty(Prop.control_media_file));
            }
        }

        mediaManager.log("Generic Process Started");
    }

    private String getIndexUrl() {
        return request.getBaseUrl() + request.getMediaId() + ".m3u8";
    }

    private File getOutputLogFile() {
        return new File(getRequest().getOutputDir(), getRequest().getMediaId() + ".out.log");
    }

    private String getIndexFile() {
        return new File(getRequest().getOutputDir(), getRequest().getMediaId() + ".m3u8").getAbsolutePath();
    }

    private String getControlFile() {
        return new File(getRequest().getOutputDir(), getRequest().getMediaId() + ".control.properties").getAbsolutePath();
    }

    @Override
    public void abort() {
        // when we about, we call the command again, with DESTROY as the first
        // arg
        MediaStreamerConfig config = mediaManager.getConfig();
        String scrptName = config.getScriptConfig().getCommand();
        List<String> cmd = new ArrayList<String>();
        cmd.add(scrptName);
        cmd.add("DESTROY");

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        pb.redirectOutput(Redirect.to(getOutputLogFile()));

        pb.directory(new File(getRequest().getOutputDir()));
        Map<String, String> env = pb.environment();
        env.put(Env.BASE_URL, getRequest().getBaseUrl());
        env.put(Env.CLIENT_ID, getRequest().getClientId());
        env.put(Env.MEDIA_ID, getRequest().getMediaId());
        env.put(Env.PLAYLIST_FILE, getIndexFile());
        env.put(Env.CONTROL_FILE, getControlFile());

        try {
            process = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaManager.log("Generic Process Destroyed");
    }
}
