package test.junit.lib;

import org.apache.commons.lang.StringEscapeUtils;
import sagex.ISageAPIProvider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class SimpleStubAPI implements ISageAPIProvider {
    public interface IAPI {
        public Object handleAPI(String name, Object[] args) throws Exception;
    }

    public class Airing extends HashMap<String, Object> {
        public HashMap<String, String> METADATA = new HashMap<String, String>();
        private static final long serialVersionUID = 1L;
    }

    public boolean LOG_MISSING_GETPROPERTY = false;
    public boolean LOG_SETPROPERTY = false;
    public boolean LOG_USERRECORD = false;

    HashMap<String, String> props = new HashMap<String, String>();
    public Map<Integer, Airing> airings = new TreeMap<Integer, SimpleStubAPI.Airing>();
    public Map<Integer, Airing> mediafiles = new TreeMap<Integer, SimpleStubAPI.Airing>();

    Map<String, Object> overrideAPIs = new HashMap<String, Object>();
    Map<String, Object> expressionAPIs = new HashMap<String, Object>();

    Map<String, Map<String, Map<String, String>>> stores = new HashMap<String, Map<String, Map<String, String>>>();

    @Override
    public Object callService(String name, Object[] args) throws Exception {
        if (args != null && args.length > 0 && args[0] instanceof Airing) {
            if ("GetMediaFileMetadata".equals(name)) {
                if (!((Airing) args[0]).METADATA.containsKey(args[1])) {
                    log("Missing Metadata: " + args[1] + "; for " + args[0]);
                    return null;
                }
                return ((Airing) args[0]).METADATA.get(args[1]);
            }
            Airing a = (Airing) args[0];
            if (a.containsKey(name)) {
                return a.get(name);
            }
        } else if ("GetProperty".equals(name) || "GetServerProperty".equals(name) || "GetClientProperty".equals(name)) {
            Object o = props.get(args[0]);
            if (o == null)
                o = args[1];
            if (props.containsKey(args[0])) {
                return o;
            }

            if (LOG_MISSING_GETPROPERTY) {
                System.out.println("api.getProperties().put(\"" + args[0] + "\",null); // consider adding property for this");
            }

            return o;
        } else if ("SetProperty".equals(name) || "SetServerProperty".equals(name) || "SetClientProperty".equals(name)) {
            props.put((String) args[0], (String) args[1]);
            if (LOG_SETPROPERTY) {
                log("SetProperty(\"" + args[0] + "\",\"" + args[1] + "\")");
            }
            return null;
        } else if ("GetAiringForID".equals(name)) {
            if (!airings.containsKey(args[0])) {
                log("Missing Airing: " + args[0]);
            }
            return airings.get((Integer) (args[0]));
        } else if ("GetMediaFileForID".equals(name)) {
            if (!mediafiles.containsKey(args[0])) {
                log("Missing MediaFile: " + args[0]);
            }
            return mediafiles.get((Integer) (args[0]));
        } else if ("GetMediaFiles".equals(name)) {
            return mediafiles.values().toArray();
        } else if ("EvaluateExpression".equals(name)) {
            if (expressionAPIs.containsKey(args[0])) {
                return expressionAPIs.get(args[0]);
            } else {
                System.out.println("api.addExpression(\"" + StringEscapeUtils.escapeJava((String) args[0])
                        + "\", null); // consider adding implementing expression with real return value");
                return null;
            }
        }

        // check for overrides
        if (overrideAPIs.containsKey(name)) {
            Object o = overrideAPIs.get(name);
            if (o instanceof IAPI) {
                try {
                    return ((IAPI) o).handleAPI(name, args);
                } catch (Throwable t) {
                    System.out.println(t.getMessage());
                    return null;
                }
            } else {
                return o;
            }
        }

        // check if we've implemented as method
        List sig = new ArrayList();
        if (args != null && args.length > 0) {
            for (Object a : args) {
                if (a != null) {
                    sig.add(a.getClass());
                } else {
                    sig.add(null);
                }
            }
        }

        try {
            Method m = getClass().getDeclaredMethod(name, (Class<?>[]) sig.toArray(new Class<?>[]{}));
            return m.invoke(this, args);
        } catch (InvocationTargetException te) {
            te.printStackTrace();
        } catch (Throwable t) {
            // t.printStackTrace();
        }

        unhandledAPI(name, args);
        return null;
    }

    @Override
    public Object callService(String context, String name, Object[] args) throws Exception {
        return callService(name, args);
    }

    public void unhandledAPI(String name, Object[] args) {
        System.out.printf("api.overrideAPI(\"%s\",null); // consider overriding api; %s\n", name, buildArgString(args));
    }

    public String buildArgString(Object[] args) {
        StringBuffer sb = new StringBuffer();
        if (args != null && args.length > 0) {
            for (Object o : args) {
                sb.append(o).append(": ").append((o != null) ? o.getClass().getName() : null).append(", ");
            }
        }
        return sb.toString();
    }

    public Map<String, String> getProperties() {
        return props;
    }

    public Airing newAiring(int id) {
        Airing a = new Airing();
        a.put("GetAiringID", id);
        a.put("IsAiringObject", true);
        a.put("IsMediaFileObject", false);
        airings.put(id, a);
        return a;
    }

    public Airing newMediaFile(int id) {
        Airing a = new Airing();
        a.put("GetMediaFileID", id);
        a.put("IsAiringObject", true);
        a.put("IsMediaFileObject", true);
        mediafiles.put(id, a);
        return a;
    }

    public void log(String msg) {
        System.out.println("SimpleStubAPI: " + msg);
    }

    public void overrideAPI(String name, Object value) {
        overrideAPIs.put(name, value);
    }

    public void overrideAPI(String name, IAPI api) {
        overrideAPIs.put(name, api);
    }

    public void addExpression(String expression, Object result) {
        expressionAPIs.put(expression, result);
    }

    // --- SageTV API Implementations
    public Object GetAllUserRecords(String store) {
        Map m = stores.get(store);
        if (m != null) {
            return m.values().toArray();
        }
        return null;
    }

    public Object GetUserRecord(String store, String id) {
        Object value = null;
        Map<String, Map<String, String>> ustore = stores.get(store);
        if (ustore != null) {
            value = ustore.get(id);
        }

        if (LOG_USERRECORD) {
            System.out.println("GetUserRecord(" + store + "," + id + "): " + value);
        }

        return value;
    }

    public Object AddUserRecord(String store, String id) {
        if (LOG_USERRECORD) {
            System.out.println("AddUserRecord(" + store + "," + id + ")");
        }
        Map<String, Map<String, String>> ustore = stores.get(store);
        if (ustore == null) {
            ustore = new HashMap<String, Map<String, String>>();
            stores.put(store, ustore);
        }

        Map<String, String> rec = ustore.get(id);
        if (rec == null) {
            rec = new HashMap<String, String>();
            ustore.put(id, rec);
        }

        return rec;
    }

    public void SetUserRecordData(HashMap rec, String fld, String value) {
        if (LOG_USERRECORD) {
            System.out.println("SetUserRecordData(UserRecord, " + fld + "," + value + ")");
        }
        rec.put(fld, value);
    }

    public String GetUserRecordData(HashMap rec, String fld) {
        return (String) rec.get(fld);
    }
}
