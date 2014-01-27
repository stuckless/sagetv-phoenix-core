package sagex.phoenix.vfs;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.Phoenix;
import test.InitPhoenix;

public class VFSOrganizerTest {
	@BeforeClass
	public static void init() throws IOException {
		InitPhoenix.init(true, true);
	}

	@Test
	public void testOrganizing() throws Exception {
		VFSOrganizer o = new VFSOrganizer(Phoenix.getInstance().getVFSDir());
		o.organize(getReader("x-vfs.xml"), "x-vfs-1.xml");
		
		assertEquals(27, o.filters.size());
		assertEquals(6, o.filterGroups.size());
		assertEquals(19, o.tags.size());
		assertEquals(11, o.sorts.size());
		assertEquals(14, o.groups.size());
		assertEquals(10, o.sources.size());
		assertEquals(54, o.views.size());

		// when we organize the second time, with the same file, then nothing should change
		o.organize(getReader("x-vfs.xml"), "x-vfs-2.xml");
		assertEquals(27, o.filters.size());
		assertEquals(6, o.filterGroups.size());
		assertEquals(19, o.tags.size());
		assertEquals(11, o.sorts.size());
		assertEquals(14, o.groups.size());
		assertEquals(10, o.sources.size());
		assertEquals(54, o.views.size());
		
		// now load another vfs and see if it organized it correctly
		o.organize(getReader("new-tags.xml"), "newtags.xml");
		assertEquals(27, o.filters.size());
		assertEquals(6, o.filterGroups.size());
		assertEquals(20, o.tags.size());
		assertEquals(11, o.sorts.size());
		assertEquals(14, o.groups.size());
		assertEquals(10, o.sources.size());
		assertEquals(54, o.views.size());
		
		// if the tv tag got updated
		assertEquals("TV Tag", o.tags.get("tv").attributeValue("label"));
		
		o.writeTo(new OutputStreamWriter(System.out));
	}

	private Reader getReader(String resFile) {
		return new InputStreamReader(this.getClass().getResourceAsStream(resFile));
	}
}
