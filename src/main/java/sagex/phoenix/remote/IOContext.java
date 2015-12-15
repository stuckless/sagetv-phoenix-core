package sagex.phoenix.remote;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public interface IOContext {
    public String getParameter(String key);

    public void writeHeader(String key, String value) throws IOException;

    public OutputStream getOutputStream() throws IOException;

    public PrintWriter getWriter() throws IOException;

    public void sendError(int code, String message) throws IOException;

    public void setEncoding(String charEncoding);
}
