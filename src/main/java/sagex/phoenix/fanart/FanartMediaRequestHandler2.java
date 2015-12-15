package sagex.phoenix.fanart;

import sagex.remote.media.SageMediaRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Alternate http fanart {@link SageMediaRequestHandler} handler that uses the
 * {@link AdvancedFanartMediaRequestHandler}
 *
 * @author seans
 */
public class FanartMediaRequestHandler2 implements SageMediaRequestHandler {
    private AdvancedFanartMediaRequestHandler handler = new AdvancedFanartMediaRequestHandler();

    public FanartMediaRequestHandler2() {
    }

    public void processRequest(HttpServletRequest req, HttpServletResponse resp, Object sageMediaFile) throws Exception {
        handler.processRequest(req, resp);
    }
}
