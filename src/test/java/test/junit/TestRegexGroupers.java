package test.junit;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.SageAPI;
import sagex.phoenix.db.ParseException;
import sagex.phoenix.vfs.groups.Grouper;
import sagex.phoenix.vfs.groups.RegexTitleGrouper;
import sagex.phoenix.vfs.sage.SageMediaFile;
import test.InitPhoenix;
import test.junit.lib.SimpleStubAPI;
import test.junit.lib.SimpleStubAPI.Airing;


public class TestRegexGroupers {
	@BeforeClass
	public static void init() throws IOException {
		InitPhoenix.init(true, true);
	}
	
	@Test
	public void testRegexTitleFilter() throws ParseException {
		SimpleStubAPI api = new SimpleStubAPI();
		int id=1;
		Airing mf = api.newMediaFile(id++);
		mf.put("GetMediaTitle", "House");
		mf.put("IsTVFile", true);
		mf.put("GetShowTitle","House");
		mf.put("GetShowEpisode","Pilot");
		mf.METADATA.put("Title", "House");
		mf.METADATA.put("MediaType", "TV");
		mf.METADATA.put("SeasonNumber", "2");

		SageAPI.setProvider(api);
		
		RegexTitleGrouper rgrouper = new RegexTitleGrouper();
		Grouper grouper = new Grouper(rgrouper);
		grouper.getOption(RegexTitleGrouper.OPT_REGEX).value().setValue(".");
		grouper.setChanged(true);
		
		String grp = grouper.getGroupName(new SageMediaFile(null, mf));
		assertEquals("H", grp);
	}
}
