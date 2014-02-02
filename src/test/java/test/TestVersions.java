package test;

import phoenix.impl.UtilAPI;

public class TestVersions {
	private static UtilAPI util = new UtilAPI();

	public static void main(String args[]) {
		// SageAPI.setProvider(SageAPI.getRemoteProvider());
		testVersion("6.1", "6.3.4");
		testVersion("6.3", "6.3.4");
		testVersion("6.3.3", "6.3.4");
		testVersion("6.3.4", "6.3.4");
		testVersion("6.3.4-beta3", "6.3.4");
		testVersion("6.3.5", "6.3.4");
		testVersion("6.3.5-beta4", "6.3.4");
		testVersion("6.4", "6.3.4");
		testVersion("6.3.4.1", "6.5");
		testVersion("6.5.9-1", "6.5.9");
		testVersion("6.5.9-2", "6.5.9-3");
		testVersion("6.5.9-3", "6.5.9-2");
	}

	private static void testVersion(String v1, String v2) {
		System.out.printf("%10s >= %10s : %s\n", v1, v2, util.IsAtLeastVersion(v1, v2));
	}
}
