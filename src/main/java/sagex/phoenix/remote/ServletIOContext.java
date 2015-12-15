package sagex.phoenix.remote;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class ServletIOContext implements IOContext {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private OutputStream stream = null;
    private PrintWriter writer = null;

    public ServletIOContext(HttpServletRequest req, HttpServletResponse resp) {
        this.request = req;
        this.response = resp;
    }

    @Override
    public String getParameter(String key) {
        return request.getParameter(key);
    }

    @Override
    public void writeHeader(String key, String value) throws IOException {
        response.addHeader(key, value);
    }

    /**
     * Can only call getWriter() OR getOutputStream() not both.
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        if (stream == null) {
            stream = response.getOutputStream();
        }
        return stream;
    }

    @Override
    public void sendError(int code, String message) throws IOException {
        response.sendError(code, message);
    }

    @Override
    public void setEncoding(String charEncoding) {
        response.setCharacterEncoding(charEncoding);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = response.getWriter();
        }
        return writer;
    }
}
