package sagex.phoenix.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;

import sagex.phoenix.metadata.MetadataUtil;

public class SagePropertiesConstGenerator {
	public static void main(String args[]) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(new File("src/main/java/sagex/phoenix/metadata/FieldName.java"));
		pw.println("package sagex.phoenix.metadata;");
		pw.println("/** generated file do not edit **/");
		pw.println("public final class FieldName {");
		Map<String,?> keys = MetadataUtil.getPropertyKeys();
		for (String k: keys.keySet()) {
			pw.printf("   public static final String %s = \"%s\";\n", createKey(k), k);
		}
		pw.println("   public static String[] values() {");
		pw.print("      return new String[] {");
		boolean sep=false;
		for (String k: keys.keySet()) {
			if (sep) pw.print(", ");
			pw.print(createKey(k));
			sep=true;
		}
		pw.println("};");
		pw.println("   }");
		pw.println("}");
		pw.flush();
		pw.close();
		System.out.println("FieldName.java has been created");
	}

	private static String createKey(String k) {
		return k.replaceAll("[^A-Za-z0-9]", "");
	}
}
