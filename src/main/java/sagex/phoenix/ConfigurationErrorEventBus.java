package sagex.phoenix;

import org.xml.sax.SAXParseException;
import sagex.phoenix.event.SimpleEventBus;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Global Event Bus for configuration error processing
 *
 * @author sean
 */
public class ConfigurationErrorEventBus extends SimpleEventBus {
    public static final ConfigurationErrorEventBus bus = new ConfigurationErrorEventBus();
    public static final String EVENT_NEW_ERROR = "configurationevent.newerror";
    public static final String EVENT_CLEAR_ERRORS = "configurationevent.clearerrors";
    public static final String ERROR_KEY = "error";

    public static ConfigurationErrorEventBus getBus() {
        return bus;
    }

    public static class ConfigurationErrorItem {
        public String name;
        public long datetime;
        public SAXParseException exception;
    }

    private LinkedList<ConfigurationErrorItem> errors = new LinkedList<ConfigurationErrorEventBus.ConfigurationErrorItem>();

    public ConfigurationErrorEventBus() {
    }

    public void addError(String name, SAXParseException ex) {
        ConfigurationErrorItem ci = new ConfigurationErrorItem();
        ci.name = name;
        ci.datetime = System.currentTimeMillis();
        ci.exception = ex;
        errors.add(ci);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put(ERROR_KEY, ci);
        fireEvent(EVENT_NEW_ERROR, args, false);
    }

    public void clearErrors() {
        errors.clear();
        fireEvent(EVENT_CLEAR_ERRORS, null, false);
    }

    public List<ConfigurationErrorItem> getErrors() {
        return errors;
    }
}
