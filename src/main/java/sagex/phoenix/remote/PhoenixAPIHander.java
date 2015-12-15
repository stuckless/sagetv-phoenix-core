package sagex.phoenix.remote;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.math.NumberUtils;

import sagex.UIContext;
import sagex.phoenix.util.Loggers;
import sagex.remote.SagexServlet.SageHandler;
import sagex.remote.json.JSONArray;
import sagex.remote.json.JSONObject;

/**
 * Serlvet class that manages the Phoenix API REST Calls.
 *
 * @author sean
 */
public class PhoenixAPIHander implements SageHandler {
    /**
     * Phoenix token to identify that the commands are a batch of commands
     * identified by this json array structure * * {@value}
     */
    public static final String PARAM_BATCH = "batch";

    /**
     * Phoenix Command either in the form of c=phoenix.umb.CreateView(viewnam)
     * or c=phoenix.umb.CreateView&1=viewname * * {@value}
     */
    public static final String PARAM_COMMAND = "c";

    /**
     * Signature for command to call... maybe null.
     */
    public static final String PARAM_SIGNATURE = "s";

    /**
     * SageTV UI context in which to execute the command * * {@value}
     */
    public static final String PARAM_CONTEXT = "context";

    /**
     * Server Side reference name in which to store the resulting object * *
     * {@value}
     */
    public static final String PARAM_REFERENCE = "ref";

    /**
     * The length of time in MS that the server reference should be stored
     * before it is expired. By default references are never expired. * *
     * {@value}
     */
    public static final String PARAM_REFERENCE_EXPIRY = "refexpiry";

    /**
     * A Hint to be passed the serializers when serializing lists. Not all
     * serializers will honor this hint but some will, such as the VFS
     * serializer. A depth of 1 means to serialize only the current folder
     * items. By default depth is 0 which means serialize all folders and sub
     * folders. * {@value}
     */
    public static final String PARAM_SERIALIZATION_DEPTH = "depth";

    private RemoteAPI api = new RemoteAPI();

    public PhoenixAPIHander() {
    }

    @Override
    public void handleRequest(String[] args, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (!StringUtils.isEmpty(req.getParameter(PARAM_BATCH))) {
                processBatch(args, req, resp);
                return;
            }

            // normal request
            String cmd = req.getParameter(PARAM_COMMAND);
            if (StringUtils.isEmpty(cmd)) {
                throw new IOException("Missing " + PARAM_COMMAND + " Command String");
            }

            ServletIOContext io = new ServletIOContext(req, resp);
            Command c = new Command(io, cmd);
            for (int i = 1; i < 99; i++) {
                String arg = req.getParameter(String.valueOf(i));
                if (StringUtils.isEmpty(arg))
                    break;
                c.getArgs().add(arg);
            }

            c.setSignature(parseSignature(req.getParameter(PARAM_SIGNATURE)));

            // check for ui context
            String uictx = req.getParameter(PARAM_CONTEXT);
            if (!StringUtils.isEmpty(uictx)) {
                c.setContext(new UIContext(uictx));
            }

            // check for reference store and expiry
            String ref = req.getParameter(PARAM_REFERENCE);
            if (!StringUtils.isEmpty(ref)) {
                c.setReferenceName(ref);
                c.setReferenceExpiry(NumberUtils.toLong(req.getParameter(PARAM_REFERENCE_EXPIRY), 0));
            }

            // set the serialization depth
            RemoteContext.get().setSerializeDepth(NumberUtils.toInt(req.getParameter(PARAM_SERIALIZATION_DEPTH), 0));

            RemoteContext.get().setData("start", NumberUtils.toInt(req.getParameter("start"), 0));
            RemoteContext.get().setData("end", NumberUtils.toInt(req.getParameter("end"), -1));
            if (((Integer) RemoteContext.get().getData("end")) > 0) {
                RemoteContext.get().setData("useranges", true);
            }

            Loggers.LOG.debug("REMOTEAPI: Ranges: " + RemoteContext.get().getData("start") + "; "
                    + RemoteContext.get().getData("end") + "; " + RemoteContext.get().getData("useranges"));

            api.callAPI(c);
        } catch (Exception e) {
            resp.sendError(404, "ERROR: " + ExceptionUtils.getFullStackTrace(e));
        } finally {
            // reset the context for the next use
            RemoteContext.get().reset();
        }
    }

    private Class[] parseSignature(String sig) throws Exception {
        if (StringUtils.isEmpty(sig))
            return null;
        String sigs[] = sig.split("\\s*,\\s*");
        Class classes[] = new Class[sigs.length];
        for (int i = 0; i < sigs.length; i++) {
            Class t = null;
            String s = sigs[i];
            if ("boolean".equals(s)) {
                t = Boolean.TYPE;
            } else if ("int".equals(s)) {
                t = Integer.TYPE;
            } else if ("float".equals(s)) {
                t = Float.TYPE;
            } else if ("double".equals(s)) {
                t = Double.TYPE;
            } else if ("char".equals(s)) {
                t = Character.TYPE;
            } else if ("byte".equals(s)) {
                t = Byte.TYPE;
            } else if ("long".equals(s)) {
                t = Long.TYPE;
            } else if ("String".equals(s)) {
                t = String.class;
            } else {
                t = Class.forName(sigs[i]);
            }
            classes[i] = t;
        }
        return classes;
    }

    private void processBatch(String[] args, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        JSONArray arr = new JSONArray(req.getParameter(PARAM_BATCH));
        int s = arr.length();
        ServletIOContext io = new ServletIOContext(req, resp);

        io.getOutputStream().write("{\"reply\": [".getBytes());

        for (int i = 0; i < s; i++) {
            try {
                if (i > 0) {
                    io.getOutputStream().write(",".getBytes());
                }
                JSONObject jo = arr.optJSONObject(i);
                String cmd = jo.optString(PARAM_COMMAND);
                if (StringUtils.isEmpty(cmd)) {
                    throw new IOException("Missing " + PARAM_COMMAND + " Command String");
                }

                Command c = new Command(io, cmd);
                for (int j = 1; j < 99; j++) {
                    String arg = jo.optString(String.valueOf(j));
                    if (StringUtils.isEmpty(arg))
                        break;
                    c.getArgs().add(arg);
                }

                c.setSignature(parseSignature(jo.optString(PARAM_SIGNATURE)));

                // check for ui context
                String uictx = jo.optString(PARAM_CONTEXT);
                if (!StringUtils.isEmpty(uictx)) {
                    c.setContext(new UIContext(uictx));
                }

                // check for reference store and expiry
                String ref = jo.optString(PARAM_REFERENCE);
                if (!StringUtils.isEmpty(ref)) {
                    c.setReferenceName(ref);
                    c.setReferenceExpiry(jo.optLong(PARAM_REFERENCE_EXPIRY, 0));
                }

                // set the serialization depth
                RemoteContext.get().setSerializeDepth(jo.optInt(PARAM_SERIALIZATION_DEPTH, 0));

                api.callAPI(c);
            } catch (Throwable t) {
                JSONObject jo = new JSONObject();
                jo.put("error", t.getMessage());
                jo.put("exception", ExceptionUtils.getStackTrace(t));
                io.getOutputStream().write(jo.toString().getBytes());
            }
        }
        io.getOutputStream().write("]}".getBytes());

    }
}
