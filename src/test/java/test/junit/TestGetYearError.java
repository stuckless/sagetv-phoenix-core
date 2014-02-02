package test.junit;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.easymock.IAnswer;
import org.junit.BeforeClass;
import org.junit.Test;

import sagex.ISageAPIProvider;
import sagex.SageAPI;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.proxy.AiringMetadataProxy;
import sagex.phoenix.metadata.proxy.SageMediaFileMetadataProxy;
import test.InitPhoenix;

public class TestGetYearError {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		InitPhoenix.init(true, true);
	}

	@Test
	public void testYear() throws Exception {
		final Map<String, String> simpleMetadataMap = new HashMap<String, String>();
		ISageAPIProvider prov = createNiceMock(ISageAPIProvider.class);
		expect(prov.callService(eq("GetShowYear"), (Object[]) anyObject())).andAnswer(new IAnswer<Object>() {
			public Object answer() throws Throwable {
				return simpleMetadataMap.get(((Object[]) getCurrentArguments()[1])[0]);
			}
		}).anyTimes();

		expect(prov.callService(eq("GetMediaFileMetadata"), (Object[]) anyObject())).andAnswer(new IAnswer<Object>() {
			public Object answer() throws Throwable {
				return simpleMetadataMap.get(((Object[]) getCurrentArguments()[1])[0]);
			}
		}).anyTimes();

		expect(prov.callService(eq("IsMediaFileObject"), (Object[]) anyObject())).andAnswer(new IAnswer<Object>() {
			public Object answer() throws Throwable {
				return true;
			}
		}).anyTimes();

		replay(prov);
		SageAPI.setProvider(prov);

		simpleMetadataMap.put("mediafile1", "2010");
		simpleMetadataMap.put("mediafile2", "ten");
		simpleMetadataMap.put("mediafile3", "");
		simpleMetadataMap.put("mediafile4", null);

		IMetadata md = AiringMetadataProxy.newInstance("mediafile1");
		assertEquals(phoenix.metadata.GetYear("mediafile1"), 2010);

		md = AiringMetadataProxy.newInstance("mediafile2");
		assertEquals(phoenix.metadata.GetYear("mediafile2"), 0);

		md = AiringMetadataProxy.newInstance("mediafile3");
		assertEquals(phoenix.metadata.GetYear("mediafile3"), 0);

		md = AiringMetadataProxy.newInstance("mediafile4");
		assertEquals(phoenix.metadata.GetYear("mediafile4"), 0);

		md = SageMediaFileMetadataProxy.newInstance("mediafile1");
		assertEquals(phoenix.metadata.GetYear("mediafile1"), 2010);

		md = SageMediaFileMetadataProxy.newInstance("mediafile2");
		assertEquals(phoenix.metadata.GetYear("mediafile2"), 0);

		md = SageMediaFileMetadataProxy.newInstance("mediafile3");
		assertEquals(phoenix.metadata.GetYear("mediafile3"), 0);

		md = SageMediaFileMetadataProxy.newInstance("mediafile4");
		assertEquals(phoenix.metadata.GetYear("mediafile4"), 0);
	}

	@Test
	public void testYearUsingStub() throws Exception {
		final Map<String, String> simpleMetadataMap = new HashMap<String, String>();
		ISageAPIProvider prov = new ISageAPIProvider() {
			@Override
			public Object callService(String context, String name, Object[] args) throws Exception {
				return callService(name, args);
			}

			@Override
			public Object callService(String name, Object[] args) throws Exception {
				if ("GetShowYear".equals(name)) {
					return simpleMetadataMap.get(args[0]);
				} else if ("GetMediaFileMetadata".equals(name)) {
					return simpleMetadataMap.get(args[0]);
				} else if ("IsMediaFileObject".equals(name)) {
					return simpleMetadataMap.containsKey(args[0]);
				} else {
					System.out.println("Unhandled: " + name);
					return null;
				}
			}
		};
		SageAPI.setProvider(prov);

		simpleMetadataMap.put("mediafile1", "2010");
		simpleMetadataMap.put("mediafile2", "ten");
		simpleMetadataMap.put("mediafile3", "");
		simpleMetadataMap.put("mediafile4", null);

		IMetadata md = AiringMetadataProxy.newInstance("mediafile1");
		assertEquals(phoenix.metadata.GetYear("mediafile1"), 2010);

		md = AiringMetadataProxy.newInstance("mediafile2");
		assertEquals(phoenix.metadata.GetYear("mediafile2"), 0);

		md = AiringMetadataProxy.newInstance("mediafile3");
		assertEquals(phoenix.metadata.GetYear("mediafile3"), 0);

		md = AiringMetadataProxy.newInstance("mediafile4");
		assertEquals(phoenix.metadata.GetYear("mediafile4"), 0);

		md = SageMediaFileMetadataProxy.newInstance("mediafile1");
		assertEquals(phoenix.metadata.GetYear("mediafile1"), 2010);

		md = SageMediaFileMetadataProxy.newInstance("mediafile2");
		assertEquals(phoenix.metadata.GetYear("mediafile2"), 0);

		md = SageMediaFileMetadataProxy.newInstance("mediafile3");
		assertEquals(phoenix.metadata.GetYear("mediafile3"), 0);

		md = SageMediaFileMetadataProxy.newInstance("mediafile4");
		assertEquals(phoenix.metadata.GetYear("mediafile4"), 0);
	}

}
