package sagex.phoenix.remote.streaming.config;

import sagex.phoenix.Phoenix;
import sagex.phoenix.configuration.proxy.AField;
import sagex.phoenix.configuration.proxy.AGroup;
import sagex.phoenix.configuration.proxy.FieldProxy;
import sagex.phoenix.configuration.proxy.GroupProxy;

@AGroup(label = "VLC Options", path = "phoenix/streaming/vlc", description = "VLC Streaming Options for Phoenix")
public class VLCConfig extends GroupProxy {
    @AField(label = "Command", description = "Full Path to vlc 2.x executable")
    private FieldProxy<String> command = new FieldProxy<String>("/usr/bin/cvlc");

    @AField(label = "Extra Command Args", description = "Space Separated list of extra commands to pass to VLC (advanced use only) (optional)")
    private FieldProxy<String> extraArgs = new FieldProxy<String>("");

    @AField(label = "WIFI Low Profile", description = "VLC Profile for Low Detail over WIFI")
    private FieldProxy<String> wifi_low = new FieldProxy<String>(
            "#transcode{width=176,height=144,fps=15,vcodec=h264,vb=256,venc=x264{aud,profile=baseline,level=30,keyint=30,ref=1},acodec=mp3,ab=24,channels=2}:std{access=livehttp{seglen=5,delsegs=false,numsegs=0,index=${PMS_PLAYLIST_FILE},index-url=${PMS_BASE_URL}${PMS_MEDIA_ID}-########.ts},mux=ts{use-key-frames},dst=${PMS_OUTPUT_DIR}/${PMS_MEDIA_ID}-########.ts}");

    @AField(label = "WIFI Normal Profile", description = "VLC Profile for Normal Detail over WIFI")
    private FieldProxy<String> wifi_normal = new FieldProxy<String>(
            "#transcode{width=480,height=360,fps=25,vcodec=h264,vb=500,venc=x264{aud,profile=baseline,level=30,keyint=30,ref=1},acodec=mp3,ab=96,channels=2}:std{access=livehttp{seglen=5,delsegs=false,numsegs=0,index=${PMS_PLAYLIST_FILE},index-url=${PMS_BASE_URL}${PMS_MEDIA_ID}-########.ts},mux=ts{use-key-frames},dst=${PMS_OUTPUT_DIR}/${PMS_MEDIA_ID}-########.ts}");

    @AField(label = "WIFI HD Profile", description = "VLC Profile for HD Detail over WIFI")
    private FieldProxy<String> wifi_hd = new FieldProxy<String>(
            "#transcode{width=1280,height=720,fps=30,vcodec=h264,vb=1000,venc=x264{aud,profile=baseline,level=30,keyint=30,ref=1},acodec=mp3,ab=128,channels=2}:std{access=livehttp{seglen=5,delsegs=false,numsegs=0,index=${PMS_PLAYLIST_FILE},index-url=${PMS_BASE_URL}${PMS_MEDIA_ID}-########.ts},mux=ts{use-key-frames},dst=${PMS_OUTPUT_DIR}/${PMS_MEDIA_ID}-########.ts}");

    @AField(label = "Mobile Low Profile", description = "VLC Profile for Low Detail over Mobile (3g)")
    private FieldProxy<String> mobile_low = new FieldProxy<String>(
            "#transcode{width=176,height=144,fps=15,vcodec=h264,vb=256,venc=x264{aud,profile=baseline,level=30,keyint=30,ref=1},acodec=mp3,ab=24,channels=2}:std{access=livehttp{seglen=5,delsegs=false,numsegs=0,index=${PMS_PLAYLIST_FILE},index-url=${PMS_BASE_URL}${PMS_MEDIA_ID}-########.ts},mux=ts{use-key-frames},dst=${PMS_OUTPUT_DIR}/${PMS_MEDIA_ID}-########.ts}");

    @AField(label = "Mobile Normal Profile", description = "VLC Profile for Normal Detail over Mobile (3g)")
    private FieldProxy<String> mobile_normal = new FieldProxy<String>(
            "#transcode{width=480,height=360,fps=25,vcodec=h264,vb=256,venc=x264{aud,profile=baseline,level=30,keyint=30,ref=1},acodec=mp3,ab=96,channels=2}:std{access=livehttp{seglen=5,delsegs=false,numsegs=0,index=${PMS_PLAYLIST_FILE},index-url=${PMS_BASE_URL}${PMS_MEDIA_ID}-########.ts},mux=ts{use-key-frames},dst=${PMS_OUTPUT_DIR}/${PMS_MEDIA_ID}-########.ts}");

    @AField(label = "Mobile HD Profile", description = "VLC Profile for HD Detail over Mobile (3g)")
    private FieldProxy<String> mobile_hd = new FieldProxy<String>(
            "#transcode{width=1280,height=720,fps=25,vcodec=h264,vb=512,venc=x264{aud,profile=baseline,level=30,keyint=30,ref=1},acodec=mp3,ab=96,channels=2}:std{access=livehttp{seglen=5,delsegs=false,numsegs=0,index=${PMS_PLAYLIST_FILE},index-url=${PMS_BASE_URL}${PMS_MEDIA_ID}-########.ts},mux=ts{use-key-frames},dst=${PMS_OUTPUT_DIR}/${PMS_MEDIA_ID}-########.ts}");

    @AField(label = "Default Profile", description = "VLC Profile when no other profile is found")
    private FieldProxy<String> default_profile = new FieldProxy<String>(
            "#transcode{width=480,height=360,fps=25,vcodec=h264,vb=500,venc=x264{aud,profile=baseline,level=30,keyint=30,ref=1},acodec=mp3,ab=96,channels=2}:std{access=livehttp{seglen=5,delsegs=false,numsegs=0,index=${PMS_PLAYLIST_FILE},index-url=${PMS_BASE_URL}${PMS_MEDIA_ID}-########.ts},mux=ts{use-key-frames},dst=${PMS_OUTPUT_DIR}/${PMS_MEDIA_ID}-########.ts}");

    public VLCConfig() {
        super();
        init();

    }

    public String getCommand() {
        return command.get();
    }

    public void setCommand(String command) {
        this.command.set(command);
    }

    public FieldProxy<String> getWifi_low() {
        return wifi_low;
    }

    public void setWifi_low(FieldProxy<String> wifi_low) {
        this.wifi_low = wifi_low;
    }

    public FieldProxy<String> getWifi_normal() {
        return wifi_normal;
    }

    public void setWifi_normal(FieldProxy<String> wifi_normal) {
        this.wifi_normal = wifi_normal;
    }

    public FieldProxy<String> getWifi_hd() {
        return wifi_hd;
    }

    public void setWifi_hd(FieldProxy<String> wifi_hd) {
        this.wifi_hd = wifi_hd;
    }

    public FieldProxy<String> getMobile_low() {
        return mobile_low;
    }

    public void setMobile_low(FieldProxy<String> mobile_low) {
        this.mobile_low = mobile_low;
    }

    public FieldProxy<String> getMobile_normal() {
        return mobile_normal;
    }

    public void setMobile_normal(FieldProxy<String> mobile_normal) {
        this.mobile_normal = mobile_normal;
    }

    public FieldProxy<String> getMobile_hd() {
        return mobile_hd;
    }

    public void setMobile_hd(FieldProxy<String> mobile_hd) {
        this.mobile_hd = mobile_hd;
    }

    public FieldProxy<String> getDefault_profile() {
        return default_profile;
    }

    public void setDefault_profile(FieldProxy<String> default_profile) {
        this.default_profile = default_profile;
    }

    public String getProfile(String key) {
        return Phoenix.getInstance().getConfigurationManager().getServerProperty("phoenix/streaming/vlc/" + key, null);
    }

    public FieldProxy<String> getExtraArgs() {
        return extraArgs;
    }

    public void setExtraArgs(FieldProxy<String> extraArgs) {
        this.extraArgs = extraArgs;
    }
}
