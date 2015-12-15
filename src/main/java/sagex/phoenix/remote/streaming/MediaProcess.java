package sagex.phoenix.remote.streaming;

public class MediaProcess {
    protected MediaRequest request;
    protected MediaControlInfo controlInfo;
    protected MediaStreamerManager mediaManager;
    protected Process process;

    public MediaProcess(MediaStreamerManager manager, MediaRequest req) {
        this.request = req;
        this.mediaManager = manager;
        controlInfo = new MediaControlInfo();
    }

    public MediaRequest getRequest() {
        return request;
    }

    public void start() throws Exception {
    }

    public MediaControlInfo getControlInfo() {
        return controlInfo;
    }

    public MediaStreamerManager getMediaManager() {
        return mediaManager;
    }

    public Process getProcess() {
        return process;
    }

    public void abort() {
        if (process != null) {
            process.destroy();
        }
    }
}
