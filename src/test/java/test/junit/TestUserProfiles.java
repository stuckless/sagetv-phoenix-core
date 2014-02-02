package test.junit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.SageAPI;
import sagex.phoenix.configuration.ConfigScope;
import sagex.phoenix.configuration.impl.SageConfigurationProvider;
import sagex.phoenix.profiles.Profile;
import sagex.stub.StubSageAPI;
import test.InitPhoenix;

public class TestUserProfiles {

	@BeforeClass
	public static void init() throws IOException {
		InitPhoenix.init(true, true);
	}

	@Test
	public void testUserProfile() {
		Profile prof = phoenix.profile.GetCurrentUserProfile();
		assertNotNull(prof);
		assertEquals("default", prof.getUser());
		assertNotNull(prof.getContext());
		System.out.println("User: " + prof.getUser() + "; Context: " + prof.getContext());

		Profile profs[] = phoenix.profile.GetUserProfiles();
		assertNotNull(profs);
		int size = profs.length;
		assertEquals(true, size > 0);

		prof = phoenix.profile.AddUserProfile("bob");
		assertNotNull(prof);
		assertEquals("bob", prof.getUser());
		assertEquals(size + 1, phoenix.profile.GetUserProfiles().length);

		prof = phoenix.profile.GetUserProfile("bob");
		assertNotNull(prof);
		assertEquals("bob", prof.getUser());
		assertEquals(size + 1, phoenix.profile.GetUserProfiles().length);

		phoenix.profile.SetCurrentUserProfile(prof);
		String profUser = prof.getUser();
		assertEquals("bob", profUser);
		assertEquals(size + 1, phoenix.profile.GetUserProfiles().length);

		prof = phoenix.profile.GetCurrentUserProfile();
		assertNotNull(prof);
		assertEquals(profUser, prof.getUser());
		assertNotNull(prof.getContext());
		System.out.println("User: " + prof.getUser() + "; Context: " + prof.getContext());

		phoenix.profile.RemoveUserProfile(prof);
		assertEquals(size, phoenix.profile.GetUserProfiles().length);
	}

	@Test
	public void testUserProperties() {
		SageAPI.setProvider(new StubSageAPI());
		SageConfigurationProvider c = new SageConfigurationProvider();
		c.setProperty(ConfigScope.USER, "test", "testval");

		Profile prof = phoenix.profile.GetCurrentUserProfile();
		String user = prof.getUser();

		assertEquals("User Configuration Properties Failed!", "testval", c.getProperty(ConfigScope.USER, "test"));
	}

	@Test
	public void testUserKeys() {
		SageAPI.setProvider(new StubSageAPI());
		SageConfigurationProvider c = new SageConfigurationProvider();
		c.setProperty(ConfigScope.USER, "test", "testval");
		c.setProperty(ConfigScope.USER, "test2", "testval2");
		c.setProperty(ConfigScope.USER, "test3/test4", "testval34");

		// Since sage confiuration provider relies on sage apis... it hard to
		// test this.
		// Iterator<String> keys = c.keys(ConfigScope.USER);
		// int s = 0;
		// for (;keys.hasNext();) {
		// keys.next();
		// s++;
		// }
		// assertEquals(3,s);
		//
		// Profile prof = phoenix.profile.AddUserProfile("bob2");
		// phoenix.profile.SetCurrentUserProfile(prof);
		// keys = c.keys(ConfigScope.USER);
		// s = 0;
		// for (;keys.hasNext();) {
		// keys.next();
		// s++;
		// }
		// assertEquals(0,s);
	}
}
