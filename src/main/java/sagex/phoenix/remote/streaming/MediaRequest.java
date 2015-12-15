package sagex.phoenix.remote.streaming;

import java.io.Serializable;
import java.util.Arrays;

public class MediaRequest implements Serializable {
    public static enum Profiles {
        low, normal, hd
    }

    public static enum Networks {
        mobile, wifi
    }

    public static enum Encoders {
        vlc, script
    }

    ;

    private static final long serialVersionUID = 1L;

    protected String clientId;

    private String[] sources;

    private String outputDir;

    private String mediaId;

    private String baseUrl;

    private boolean requestingGenericStreamer = false;

    private String profile;

    private String clientScreen;

    private String network;

    public MediaRequest() {
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String[] getSources() {
        return sources;
    }

    public void setSources(String[] sources) {
        this.sources = sources;
    }

    public void setSingleSource(String source) {
        this.sources = new String[]{source};
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public String toString() {
        return "MediaRequest [" + (clientId != null ? "clientId=" + clientId + ", " : "")
                + (sources != null ? "sources=" + Arrays.toString(sources) + ", " : "")
                + (outputDir != null ? "outputDir=" + outputDir + ", " : "") + (mediaId != null ? "mediaId=" + mediaId + ", " : "")
                + (baseUrl != null ? "baseUrl=" + baseUrl + ", " : "") + "requestingGenericStreamer=" + requestingGenericStreamer
                + ", " + (profile != null ? "profile=" + profile + ", " : "")
                + (clientScreen != null ? "clientScreen=" + clientScreen + ", " : "")
                + (network != null ? "network=" + network : "") + "]";
    }

    public boolean isRequestingGenericStreamer() {
        return requestingGenericStreamer;
    }

    public void setRequestingGenericStreamer(boolean requestingGenericStreamer) {
        this.requestingGenericStreamer = requestingGenericStreamer;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public void setClientScreen(String screen) {
        this.clientScreen = screen;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getClientScreen() {
        return clientScreen;
    }

    public String getProfile() {
        return profile;
    }

    public String getNetwork() {
        return network;
    }
}
