package sagex.phoenix.event;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import sage.SageTVEventListener;
import sagex.api.SystemMessageAPI;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Writes Phoenix System Messages to the SageTV SystemMessages
 * <p/>
 * variable 'eventcode' will contain the event's code<br/>
 * variable 'typename' will contain the event's subject<br/>
 *
 * @author seans
 */
public class SageSystemMessageListener implements SageTVEventListener {
    public static interface Field {
        public static final String CODE = "code";
        public static final String SEVERITY = "severity";
        public static final String MESSAGE = "message";
        public static final String LABEL = "typename";
    }

    public static int DEBUG = -1, STATUS = 0, INFO = 1, WARNING = 2, ERROR = 3;
    private Logger log = Logger.getLogger(SageSystemMessageListener.class);

    @Override
    public void sageEvent(String name, Map args) {
        try {
            // You may also use other user-defined message codes
            // which should be greater than 9999. To give
            // those messages a 'type name' which will be visible
            // by the user; you can defined a message variable
            // with the name 'typename' and then that will be displayed.
            SystemMessageAPI.PostSystemMessage((Integer) args.get(Field.CODE), (Integer) args.get(Field.SEVERITY),
                    (String) args.get(Field.MESSAGE), getMessageVariable(args));
        } catch (Throwable t) {
            log.warn("Failed to post system message: " + sagex.phoenix.util.StringUtils.mapToString(args), t);
        }
    }

    private Properties getMessageVariable(Map eventArgs) {
        Properties props = new Properties();
        try {
            if (eventArgs != null && eventArgs.size() > 0) {
                for (Iterator i = eventArgs.entrySet().iterator(); i.hasNext(); ) {
                    Map.Entry me = (Map.Entry) i.next();
                    if (me.getKey() != null && me.getValue() != null) {
                        props.put(me.getKey(), me.getValue());
                    } else {
                        log.info("Skipping Event Variable, since it's null: " + me.getKey() + "; " + me.getValue());
                    }
                }
            }

            return props;
        } catch (Exception e) {
            log.warn("Failed to add properties for event: " + sagex.phoenix.util.StringUtils.mapToString(eventArgs));
        }
        return props;
    }

    /**
     * Create a system message event
     *
     * @param code
     * @param serverity
     * @param label
     * @param msg
     * @param t
     * @return
     */
    public static Map createEvent(int code, int serverity, String label, String msg, Throwable t) {
        Map args = new HashMap();
        args.put(Field.CODE, code);
        args.put(Field.SEVERITY, serverity);
        args.put(Field.LABEL, label);
        args.put(Field.MESSAGE, msg);
        if (t != null) {
            args.put("exception", ExceptionUtils.getStackTrace(t));
        }
        return args;
    }
}
