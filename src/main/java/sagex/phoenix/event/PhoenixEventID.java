package sagex.phoenix.event;

/**
 * All Global Phoenix Events should be registered here
 *
 * @author sean
 */
public interface PhoenixEventID {
    /**
     * Event to send a system message. The system message may be posted to the
     * core sagetv message system. Args: code, typename, message, severity *
     * {@value}
     */
    public static final String SystemMessageEvent = "phoenix.SystemMessage";

    /**
     * Event to force the VFS to reload its configuration. No Args. * {@value}
     */
    public static final String VFS_Reload = "phoenix.vfs.reload";
}
