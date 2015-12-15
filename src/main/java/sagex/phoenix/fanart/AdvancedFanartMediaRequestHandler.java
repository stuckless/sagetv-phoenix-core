package sagex.phoenix.fanart;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import sagex.api.MediaFileAPI;
import sagex.phoenix.Phoenix;

/**
 * Exposes Fanart over HTTP
 *
 * @author seans
 */
public class AdvancedFanartMediaRequestHandler {
    public static final String PARAM_MEDIAFILE = "mediafile";
    public static final String PARAM_TITLE = "title";
    public static final String PARAM_SEASON = "season";
    public static final String PARAM_EPISODE = "episode";
    public static final String PARAM_MEDIATYPE = "mediatype";
    public static final String PARAM_ARTIFACTTYPE = "artifact";
    public static final String PARAM_ARTIFACT_TITLE = "artifacttitle";
    public static final String PARAM_TRANS_OVERWRITE = "overwrite";
    public static final String PARAM_TRANS_TRANSFORM = "transform";
    public static final String PARAM_TRANS_TAG = "tag";
    public static final String PARAM_TRANS_SCALEX = "scalex";
    public static final String PARAM_TRANS_SCALEY = "scaley";

    /**
     * if usedefault=true, then use the default poster, etc, when no fanart is
     * found.
     */
    public static final String PARAM_USE_DEFAULT = "usedefault";

    /**
     * Default fanart dir
     */
    static final String DEFAULT_FANART = "defaultfanart";

    private Logger log = Logger.getLogger(this.getClass());

    public AdvancedFanartMediaRequestHandler() {
    }

    public void processRequest(HttpServletRequest req, HttpServletResponse resp) {
        if (log.isDebugEnabled()) {
            log.debug("Fanart Request: " + req.getPathInfo() + "?" + req.getQueryString());
        }
        String path = null;

        int mediaFile;
        String mediaTitle;

        mediaFile = NumberUtils.toInt(req.getParameter(PARAM_MEDIAFILE), 0);
        mediaTitle = req.getParameter(PARAM_TITLE);

        if ("episode".equalsIgnoreCase(req.getParameter(PARAM_ARTIFACTTYPE))) {
            if (mediaFile == 0) {
                error(HttpServletResponse.SC_NOT_FOUND, "Episode Fanart must use mediafile parameter", req, resp);
                return;
            }
            path = phoenix.fanart.GetEpisode(MediaFileAPI.GetMediaFileForID(mediaFile), true);
        } else {
            Map<String, String> metadata = new HashMap<String, String>();
            String season = req.getParameter(PARAM_SEASON);
            if (!StringUtils.isEmpty(season)) {
                metadata.put("SeasonNumber", season);
            }
            String episode = req.getParameter(PARAM_EPISODE);
            if (!StringUtils.isEmpty(episode)) {
                metadata.put("EpisodeNumber", episode);
            }

            path = getFanartArtifact(mediaFile, req.getParameter(PARAM_MEDIATYPE), mediaTitle,
                    req.getParameter(PARAM_ARTIFACTTYPE), req.getParameter(PARAM_ARTIFACT_TITLE), metadata);
        }

        if (path == null && requiresDefault(req)) {
            // find a default for the given artifact type and return that path.
            path = resolveDefaultFanart(req, req.getParameter(PARAM_MEDIATYPE), req.getParameter(PARAM_ARTIFACTTYPE));
            if (path != null) {
                // TODO: Create a Temp File by loading the original, and then
                // applying the title string,
                // if required, and then make a new image file and use that as
                // the path.
            }
        }

        if (path == null) {
            error(HttpServletResponse.SC_NOT_FOUND, "Fanart not found", req, resp);
            return;
        }

        File f = new File(path);
        if (!f.exists()) {
            error(HttpServletResponse.SC_NOT_FOUND, "Fanart not found: " + f, req, resp);
            return;
        }

        // check the transformation
        boolean overwrite = BooleanUtils.toBoolean(req.getParameter(PARAM_TRANS_OVERWRITE));
        String transform = req.getParameter(PARAM_TRANS_TRANSFORM);
        String tag = req.getParameter(PARAM_TRANS_TAG);
        int scalex = NumberUtils.toInt(req.getParameter(PARAM_TRANS_SCALEX), -1);
        int scaley = NumberUtils.toInt(req.getParameter(PARAM_TRANS_SCALEY), -1);

        if (scalex > 0 || scaley > 0) {
            // note this kills an existing transform
            transform = "{name:scale, width:" + scalex + ",height:" + scaley + "}";
        }

        if (StringUtils.isEmpty(transform)) {
            // no transformat, just send back the file
            sendFile(f, resp);
            return;
        }

        if (StringUtils.isEmpty(tag)) {
            tag = "webfanart";
        }

        // apply the transformation
        File imageNew = applyTransform(path + "_" + transform, tag, f, transform, overwrite);
        if (imageNew == null) {
            error(HttpServletResponse.SC_NOT_FOUND, "Transform Failed: " + f, req, resp);
            return;
        }

        sendFile(imageNew, resp);
    }

    protected String getFanartArtifact(int mediaFile, String mediaType, String mediaTitle, String artifactType,
                                       String artifactTitle, Map<String, String> metadata) {
        return phoenix.fanart.GetFanartArtifact(mediaFile, mediaType, mediaTitle, artifactType, artifactTitle, metadata);
    }

    private File applyTransform(String string, String tag, File f, String transform, boolean overwrite) {
        return phoenix.image.CreateImageAsFile(string, tag, f, transform, overwrite);
    }

    protected String resolveDefaultFanart(HttpServletRequest req, String mediaType, String artifact) {
        StringBuilder sb = new StringBuilder("default");
        if (!StringUtils.isEmpty(mediaType)) {
            sb.append("_").append(mediaType.toLowerCase());
        }

        if (!StringUtils.isEmpty(artifact)) {
            sb.append("_").append(artifact.toLowerCase());
        }

        sb.append(".jpg");

        String filename = sb.toString();
        File defFile = new File(Phoenix.getInstance().getUserPath(DEFAULT_FANART), filename);
        if (!defFile.exists()) {
            // check phoenix core ares
            defFile = new File(Phoenix.getInstance().getPhoenixRootDir(), DEFAULT_FANART + "/" + filename);
        }

        if (defFile.exists()) {
            return defFile.getAbsolutePath();
        }

        return null;
    }

    protected boolean requiresDefault(HttpServletRequest req) {
        return "true".equalsIgnoreCase(req.getParameter(PARAM_USE_DEFAULT));
    }

    protected void sendFile(File f, HttpServletResponse resp) {
        try {
            resp.setContentType("image/jpg");
            OutputStream os = resp.getOutputStream();
            InputStream is = new FileInputStream(f);
            IOUtils.copy(is, os);
            is.close();
            os.flush();
        } catch (Exception e) {
            log.warn("Failed to send back fanart file: " + f, e);
            error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), null, resp);
        }

    }

    protected void error(int errCode, String msg, HttpServletRequest req, HttpServletResponse resp) {
        log.warn("Fanart Failed: " + errCode + "; " + msg);
        try {
            resp.sendError(errCode, msg);
        } catch (IOException e) {
            log.warn("Failed to send error response!", e);
        }
    }
}
