package sagex.phoenix.remote.streaming;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.google.gson.Gson;

import sagex.api.MediaFileAPI;
import sagex.phoenix.Phoenix;
import sagex.phoenix.remote.gson.PhoenixGSONBuilder;
import sagex.remote.SagexServlet.SageHandler;
import sagex.util.WaitFor;

public class PhoenixStreamingHandler implements SageHandler {
    @Override
    public void handleRequest(String[] args, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (args.length < 4) {
                help(resp);
                return;
            }

            // args[] is /sagex[0]/streaming[1]/action[2]/clientid[3]/extra[4]
            String action = args[2];
            String clientid = args[3];
            if ("files".equalsIgnoreCase(action)) {
                if (args.length < 5) {
                    help(resp);
                    return;
                }

                sendFile(req, resp, clientid, args[4]);
            } else if ("request".equalsIgnoreCase(action)) {
                createMediaRequest(clientid, req, resp);
            } else if ("control".equalsIgnoreCase(action)) {
                if ("stop".equalsIgnoreCase(req.getParameter("cmd"))) {
                    Phoenix.getInstance().getMediaStreamer().abortProcess(clientid);
                    sendJson(req, resp, "Client Stopped");
                } else {
                    help(resp);
                }
            } else {
                help(resp);
                return;
            }
        } catch (Throwable t) {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("error", ExceptionUtils.getFullStackTrace(t));
            data.put("args", args);
            sendJson(req, resp, data);
        }
    }

    Map<String, Object> createError(String message, Throwable t) {
        Map<String, Object> data = new HashMap<String, Object>();
        if (t == null) {
            data.put("error", message);
        } else {
            data.put("errorMessage", message);
            data.put("error", ExceptionUtils.getFullStackTrace(t));
        }
        return data;
    }

    private void createMediaRequest(String clientid, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        MediaRequest mreq = new MediaRequest();
        String baseUrl = req.getRequestURL().substring(0, req.getRequestURL().indexOf("/request/")) + "/files/" + clientid + "/";
        mreq.setBaseUrl(baseUrl);
        mreq.setClientId(clientid);
        mreq.setMediaId(req.getParameter("mediafile"));
        mreq.setOutputDir(Phoenix.getInstance().getMediaStreamer().getConfig().getServerConfig().getTempDir() + File.separator
                + clientid);
        File[] files = MediaFileAPI
                .GetSegmentFiles(MediaFileAPI.GetMediaFileForID(NumberUtils.toInt(req.getParameter("mediafile"))));
        if (files != null && files.length > 0) {
            List<String> paths = new ArrayList<String>();
            for (File f : files) {
                paths.add(f.getAbsolutePath());
            }
            mreq.setSources(paths.toArray(new String[]{}));
        } else {
            // use the "id" as the video source, could be url, file, etc
            mreq.setSingleSource(req.getParameter("mediafile"));
        }
        mreq.setProfile(req.getParameter("profile"));
        mreq.setClientScreen(req.getParameter("client_screen"));
        mreq.setNetwork(req.getParameter("network"));
        mreq.setRequestingGenericStreamer(MediaRequest.Encoders.script.name().equals(req.getParameter("encoder")));
        final MediaResponse mresp = Phoenix.getInstance().getMediaStreamer().createRequest(mreq);

        if (!StringUtils.isEmpty(mresp.getShortErrorMessage())) {
            sendJson(req, resp, createError(mresp.getShortErrorMessage(), null));
            return;
        }

        if (mresp.getControlInfo() == null || StringUtils.isEmpty(mresp.getControlInfo().getMediaUrl())) {
            sendJson(req, resp, createError("Missing Media URL in reply", null));
            return;
        }

        if (mresp.getControlInfo().getLockFile() != null) {
            final File file = new File(mresp.getControlInfo().getLockFile());
            WaitFor wait = new WaitFor() {
                @Override
                public boolean isDoneWaiting() {
                    return file.exists();
                }
            };
            wait.waitFor(Phoenix.getInstance().getMediaStreamer().getConfig().getServerConfig().getWaitTimeout() * 1000, 200);
            if (!wait.isDoneWaiting()) {
                Phoenix.getInstance().getMediaStreamer().abortProcess(clientid);
                sendJson(req, resp, createError("Timedout waiting for stream to be created", null));
                return;
            }
        }

        sendJson(req, resp, mresp);
    }

    public void sendFile(HttpServletRequest req, HttpServletResponse resp, String client, String filename) {
        File f = new File(Phoenix.getInstance().getMediaStreamer().getConfig().getServerConfig().getTempDir(), client
                + java.io.File.separator + filename);
        try {
            if (!f.exists()) {
                resp.sendError(404, "No Such File: " + f.getAbsolutePath());
            }
            OutputStream os = resp.getOutputStream();
            FileInputStream fis = new FileInputStream(f);
            IOUtils.copy(fis, os);
            fis.close();
            os.flush();
        } catch (Exception e) {
            try {
                resp.sendError(500, "Failed to get file " + f.getAbsolutePath());
            } catch (IOException e1) {
                e.printStackTrace();
                e1.printStackTrace();
            }
        }
    }

    public static void sendJson(HttpServletRequest req, HttpServletResponse resp, Object data) throws IOException {
        Map<String, Object> reply = new HashMap<String, Object>();
        reply.put("reply", data);

        PrintWriter pw = resp.getWriter();
        resp.addHeader("Content-Type", "text/plain");
        createGson(req).toJson(reply, pw);
        pw.flush();
    }

    public static Gson createGson(HttpServletRequest req) {
        boolean pretty=false;
        if (req.getParameter("_prettyprint") != null) {
            pretty=true;
        }
        return PhoenixGSONBuilder.getNewInstance(PhoenixGSONBuilder.newOptions().prettyPrint(pretty));
    }

    private void help(HttpServletResponse resp) throws IOException {
        PrintWriter w = resp.getWriter();
        w.println("<html>");
        w.println("<h1>Phoenix Media Streamer</h1>");
        w.println("<h2>Options Are</h2>");
        w.println("<ul>");
        w.println("<li>/files/CLIENTID/file.ext");
        w.println("<li>/request/CLIENTID?mediafile=SAGE_ID&network=wifi|mobile&profile=low|normal|hd&client_screen=WxH&encoder=native|script");
        w.println("<li>/control/CLIENTID?cmd=stop");
        w.println("</ul>");
        w.println("</html>");
    }
}
