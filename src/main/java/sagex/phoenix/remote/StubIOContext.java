package sagex.phoenix.remote;

import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Stub IO Context used for testing
 *
 * @author sean
 */
public class StubIOContext implements IOContext {
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private Map<String, String> params = new HashMap<String, String>();
    private PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos));

    public StubIOContext() {
    }

    public StubIOContext(Map<String, String> params) {
        this.params = params;
    }

    @Override
    public String getParameter(String key) {
        return params.get(key);
    }

    @Override
    public void writeHeader(String key, String value) throws IOException {
        // baos.write(String.format("%s: %s\n", key, value).getBytes());
    }

    @Override
    public OutputStream getOutputStream() {
        return baos;
    }

    public String getBuffer() {
        return new String(baos.toByteArray());
    }

    @Override
    public void sendError(int code, String message) {
        System.out.println("ERROR: " + code + "; " + message);
    }

    @Override
    public void setEncoding(String charEncoding) {
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return writer;
    }
}
