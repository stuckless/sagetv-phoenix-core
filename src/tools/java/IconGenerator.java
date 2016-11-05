import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Created by seans on 03/11/16.
 */
public class IconGenerator {
    public static void generate(File out) throws IOException {
        StringBuilder file = new StringBuilder();
        file.append("package sagex.phoenix.image;\n");
        file.append("import java.util.Map;\n");
        file.append("import java.util.HashMap;\n");
        file.append("import java.util.List;\n");
        file.append("import java.util.ArrayList;\n");
        file.append("import java.util.Collections;\n");
        file.append("public class MaterialIcons {\n");
        file.append("   public static final Map<String,String> ICONS = new HashMap<String,String>();\n");
        file.append("   public static final String ICON_NAMES[];\n");
        file.append("   static {\n");
        Scanner scanner = new Scanner(IconGenerator.class.getClassLoader().getResourceAsStream("codepoints"));
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            String parts[]=line.split("\\s+");
            if (parts.length==2) {
                String key=parts[0].trim();
                String value=parts[1].trim();
                value = "\\u" + value.toUpperCase();
                file.append(String.format("       ICONS.put(\"%s\",\"%s\");\n",key, value));
            } else {
                System.err.println("Skipping: [" + line + "]; Parts: " + parts.length);
            }
        }

        file.append("      List<String> names = new ArrayList<String>(ICONS.keySet());\n");
        file.append("      Collections.sort(names);\n");
        file.append("      ICON_NAMES = names.toArray(new String[]{});\n");


        file.append("   }\n");
        file.append("}\n");

        Files.write(Paths.get(out.toURI()), file.toString().getBytes("UTF-8"));
        //System.out.println(file);
    }

    public static void main(String args[]) throws IOException {
        File out = new File("src/main/java");
        if (!out.exists()) throw new RuntimeException("Must be run from project root;  Current root is: " + new File(".").getAbsolutePath());
        out = new File(out, "sagex/phoenix/image/");
        out.mkdirs();
        out = new File(out, "MaterialIcons.java");
        generate(out);
    }
}
