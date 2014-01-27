package sagex.phoenix.fanart;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import sagex.phoenix.Phoenix;
import test.InitPhoenix;

public class AdvancedFanartMediaRequestHandlerTest {
	@BeforeClass
	public static void init() throws IOException {
		InitPhoenix.init(true, true);
	}
	
	@Test
	public void testProcessRequest_DefaultFanartPosterForMovie() {
		AdvancedFanartMediaRequestHandler handler = spy(new AdvancedFanartMediaRequestHandler());
		doNothing().when(handler).sendFile(any(File.class), any(HttpServletResponse.class));
		doNothing().when(handler).error(anyInt(), anyString(), any(HttpServletRequest.class), any(HttpServletResponse.class));
		HttpServletRequest req = mock(HttpServletRequest.class);
		HttpServletResponse resp = mock(HttpServletResponse.class);

		doReturn("true").when(req).getParameter(AdvancedFanartMediaRequestHandler.PARAM_USE_DEFAULT);
		doReturn("movie").when(req).getParameter(AdvancedFanartMediaRequestHandler.PARAM_MEDIATYPE);
		doReturn("poster").when(req).getParameter(AdvancedFanartMediaRequestHandler.PARAM_ARTIFACTTYPE);
		doReturn("1").when(req).getParameter(AdvancedFanartMediaRequestHandler.PARAM_MEDIAFILE);
		handler.processRequest(req, resp);
		
		ArgumentCaptor<File> capture = ArgumentCaptor.forClass(File.class);
		verify(handler).sendFile(capture.capture(), any(HttpServletResponse.class));
		assertEquals("default_movie_poster.jpg", capture.getValue().getName());
	}

	@Test
	public void testProcessRequest_DefaultFanartPosterForMovieNotUsed() {
		AdvancedFanartMediaRequestHandler handler = spy(new AdvancedFanartMediaRequestHandler());
		doNothing().when(handler).sendFile(any(File.class), any(HttpServletResponse.class));
		doNothing().when(handler).error(anyInt(), anyString(), any(HttpServletRequest.class), any(HttpServletResponse.class));
		HttpServletRequest req = mock(HttpServletRequest.class);
		HttpServletResponse resp = mock(HttpServletResponse.class);

		doReturn("movie").when(req).getParameter(AdvancedFanartMediaRequestHandler.PARAM_MEDIATYPE);
		doReturn("poster").when(req).getParameter(AdvancedFanartMediaRequestHandler.PARAM_ARTIFACTTYPE);
		doReturn("1").when(req).getParameter(AdvancedFanartMediaRequestHandler.PARAM_MEDIAFILE);
		handler.processRequest(req, resp);
		
		ArgumentCaptor<Integer> capture = ArgumentCaptor.forClass(Integer.class);
		verify(handler).error(capture.capture(), anyString(), any(HttpServletRequest.class), any(HttpServletResponse.class));
		assertEquals(HttpServletResponse.SC_NOT_FOUND, capture.getValue().intValue());
	}
	
	@Test
	public void testProcessRequest_DefaultFanartMissing() {
		AdvancedFanartMediaRequestHandler handler = spy(new AdvancedFanartMediaRequestHandler());
		doNothing().when(handler).sendFile(any(File.class), any(HttpServletResponse.class));
		doNothing().when(handler).error(anyInt(), anyString(), any(HttpServletRequest.class), any(HttpServletResponse.class));
		HttpServletRequest req = mock(HttpServletRequest.class);
		HttpServletResponse resp = mock(HttpServletResponse.class);

		doReturn("true").when(req).getParameter(AdvancedFanartMediaRequestHandler.PARAM_USE_DEFAULT);
		doReturn("movie").when(req).getParameter(AdvancedFanartMediaRequestHandler.PARAM_MEDIATYPE);
		doReturn("banner").when(req).getParameter(AdvancedFanartMediaRequestHandler.PARAM_ARTIFACTTYPE);
		doReturn("1").when(req).getParameter(AdvancedFanartMediaRequestHandler.PARAM_MEDIAFILE);
		handler.processRequest(req, resp);
		
		ArgumentCaptor<Integer> capture = ArgumentCaptor.forClass(Integer.class);
		verify(handler).error(capture.capture(), anyString(), any(HttpServletRequest.class), any(HttpServletResponse.class));
		assertEquals(HttpServletResponse.SC_NOT_FOUND, capture.getValue().intValue());
	}
	
	@Test
	public void testProcessRequest_DefaultFanartFromUserData() throws IOException {
		// but we want to test that it reads from the userdata area.
		File file = new File(Phoenix.getInstance().getUserPath(AdvancedFanartMediaRequestHandler.DEFAULT_FANART), "default_movie_poster.jpg");
		FileUtils.touch(file);
		
		AdvancedFanartMediaRequestHandler handler = spy(new AdvancedFanartMediaRequestHandler());
		doNothing().when(handler).sendFile(any(File.class), any(HttpServletResponse.class));
		doNothing().when(handler).error(anyInt(), anyString(), any(HttpServletRequest.class), any(HttpServletResponse.class));
		HttpServletRequest req = mock(HttpServletRequest.class);
		HttpServletResponse resp = mock(HttpServletResponse.class);

		doReturn("true").when(req).getParameter(AdvancedFanartMediaRequestHandler.PARAM_USE_DEFAULT);
		doReturn("movie").when(req).getParameter(AdvancedFanartMediaRequestHandler.PARAM_MEDIATYPE);
		doReturn("poster").when(req).getParameter(AdvancedFanartMediaRequestHandler.PARAM_ARTIFACTTYPE);
		doReturn("1").when(req).getParameter(AdvancedFanartMediaRequestHandler.PARAM_MEDIAFILE);
		handler.processRequest(req, resp);
		
		ArgumentCaptor<File> capture = ArgumentCaptor.forClass(File.class);
		verify(handler).sendFile(capture.capture(), any(HttpServletResponse.class));
		assertEquals("default_movie_poster.jpg", capture.getValue().getName());
		assertEquals("Should be 0 byte file, since we just created it", 0, capture.getValue().length());
	}
}
