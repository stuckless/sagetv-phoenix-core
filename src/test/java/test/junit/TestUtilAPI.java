package test.junit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import phoenix.impl.UtilAPI;

public class TestUtilAPI {
	@Test
	public void testEquals() {
		String s1 = "Sean";
		String s2 = "Sean";
		assertTrue(phoenix.api.Equals(s1, s2));
		assertFalse(phoenix.api.Equals("bob", s2));
		assertFalse(phoenix.api.Equals(null, s2));
		assertFalse(phoenix.api.Equals(s1, null));

		String[] a1 = new String[] { "Sean", "Mike" };
		String[] a2 = new String[] { "Sean", "Mike" };
		assertTrue(phoenix.api.Equals(a1, a2));
		assertFalse(phoenix.api.Equals(a1, new String[] { "Sean" }));

		List<String> l1 = new LinkedList<String>();
		l1.add("Sean");
		l1.add("Mike");
		List<String> l2 = new LinkedList<String>();
		l2.add("Sean");
		l2.add("Mike");
		assertTrue(phoenix.api.Equals(l1, l2));
		assertFalse(phoenix.api.Equals(a1, l1));
	}

	@Test
	public void testRelativePath() {
		UtilAPI api = new UtilAPI();
		File mediaFile = new File("/tmp/Videos/Movies/DVD/MyDvd.avi");
		File source = new File("/tmp/Videos/");
		assertEquals("Movies/DVD/MyDvd.avi", phoenix.util.GetRelativePath(source, mediaFile));

		assertNull(api.GetRelativePath("/xyq/Movies", mediaFile));

		assertEquals("Movies/DVD/MyDvd.avi",
				api.GetRelativePath(new File[] { new File("/tmp/MyVideos"), new File("/tmp/Videos") }, mediaFile));
		assertEquals("Movies/DVD/MyDvd.avi", api.GetRelativePath(new File[] { new File("/tmp/MyVideos"), new File("/tmp/Videos") },
				"/tmp/Videos/Movies/DVD/MyDvd.avi"));
	}

}
