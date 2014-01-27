package sagex.phoenix.remote;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletIOContext implements IOContext {
	private HttpServletRequest request;
	private HttpServletResponse response;
	private OutputStream stream;
	public ServletIOContext(HttpServletRequest req, HttpServletResponse resp) {
		this.request=req;
		this.response=resp;
		try {
			this.stream=resp.getOutputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getParameter(String key) {
		return request.getParameter(key);
	}

	@Override
	public void writeHeader(String key, String value) throws IOException {
		response.addHeader(key, value);
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return stream;
	}

	@Override
	public void sendError(int code, String message) throws IOException {
		response.sendError(code, message);
	}
}
