package sagex.phoenix.util.url;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;

public class MockUrlUtils {

	public MockUrlUtils() {
		// TODO Auto-generated constructor stub
	}

	public static IUrl mockUrlResource(Object resolveBase, String res) throws IOException {
		IUrl url = mock(IUrl.class);
		doReturn(resolveBase.getClass().getResource(res).openStream()).when(url).getInputStream((ICookieHandler) any(), anyBoolean());
		return url;
	}
}
