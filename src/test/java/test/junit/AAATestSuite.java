package test.junit;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

public class AAATestSuite {
	public static void main(String args[]) {
		File dir = new File("src/test/java/test/junit");
		for (File f: dir.listFiles()) {
			if (f.getName().startsWith("Test") && f.getName().endsWith(".java")) {
				System.out.printf("<test name=\"%s\" haltonfailure=\"no\" todir=\"${target}/unittests\"/>\n", getName(f));
			}
		}
	}

	private static String getName(File f) {
		return "test.junit." + FilenameUtils.getBaseName(f.getName());
	}
}
