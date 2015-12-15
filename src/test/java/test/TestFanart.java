package test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import sagex.phoenix.fanart.FanartUtil;
import sagex.phoenix.fanart.IFanartSupport;
import sagex.phoenix.fanart.LocalFanartSupport;
import sagex.phoenix.fanart.PhoenixFanartSupport;
import sagex.phoenix.fanart.SageMCFanartSupport;
import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;

public class TestFanart {
    public static void main(String args[]) {
        IFanartSupport supports[] = new IFanartSupport[]{new SageMCFanartSupport(), new PhoenixFanartSupport(),
                new LocalFanartSupport()};

        for (IFanartSupport support : supports) {
            System.out.println("BeginTesting: " + support.getClass().getName());
            testFolder(support, new File("Terminator.avi"), MediaType.MOVIE, MediaArtifactType.BACKGROUND, "Terminator?",
                    "/centralFanart");
            testFolder(support, new File("Terminator.avi"), MediaType.MOVIE, MediaArtifactType.POSTER, "Terminator?",
                    "/centralFanart");
            testFolder(support, new File("Terminator.avi"), MediaType.MOVIE, MediaArtifactType.BANNER, "Terminator?",
                    "/centralFanart");
            System.out.println("---");
            testFolder(support, new File("Terminator.avi"), MediaType.TV, MediaArtifactType.BACKGROUND, "Terminator?",
                    "/centralFanart");
            testFolder(support, new File("Terminator.avi"), MediaType.TV, MediaArtifactType.POSTER, "Terminator?", "/centralFanart");
            testFolder(support, new File("Terminator.avi"), MediaType.TV, MediaArtifactType.BANNER, "Terminator?", "/centralFanart");
            System.out.println("---");
            testFolder(support, new File("Terminator.avi"), MediaType.MUSIC, MediaArtifactType.BACKGROUND, "Terminator?",
                    "/centralFanart");
            testFolder(support, new File("Terminator.avi"), MediaType.MUSIC, MediaArtifactType.POSTER, "Terminator?",
                    "/centralFanart");
            testFolder(support, new File("Terminator.avi"), MediaType.MUSIC, MediaArtifactType.BANNER, "Terminator?",
                    "/centralFanart");
            System.out.println("---");
            System.out.println("  EndTesting: " + support.getClass().getName());
            System.out.println("");
        }

        // CentralFanartFolderSupportV2 f = new CentralFanartFolderSupportV2();
        // f.SetFanartArtifactForTitleWithFile(new File("target/testfile.avi"),
        // new File("target/MyFile.jpg"), MediaType.MOVIE,
        // MediaArtifactType.BACKGROUND, "OhYeah", "target");

        Map<String, String> md = new HashMap<String, String>();
        dumpFanartPath(MediaType.MOVIE, MediaArtifactType.BACKGROUND, "Indiana Jones", "/tmp/Fanart/", md);
        md.put("SeasonNumber", "1");
        dumpFanartPath(MediaType.TV, MediaArtifactType.POSTER, "House", "/tmp/Fanart/", md);
        md.put("SeasonNumber", "2");
        dumpFanartPath(MediaType.TV, MediaArtifactType.POSTER, "House", "/tmp/Fanart/", md);
        dumpFanartPath(MediaType.MUSIC, MediaArtifactType.BANNER, "Alabama", "/tmp/Fanart/", md);

        File f = new File("../../src/test/menus/TestMenu.xml");
        dumpLocalFanartPath(MediaType.TV, MediaArtifactType.POSTER, f);
        dumpLocalFanartPath(MediaType.TV, MediaArtifactType.BANNER, f);
        dumpLocalFanartPath(MediaType.TV, MediaArtifactType.BACKGROUND, f);

        f = new File("../../src/test/menus");
        dumpLocalFanartPath(MediaType.TV, MediaArtifactType.POSTER, f);
        dumpLocalFanartPath(MediaType.TV, MediaArtifactType.BANNER, f);
        dumpLocalFanartPath(MediaType.TV, MediaArtifactType.BACKGROUND, f);

    }

    private static void dumpLocalFanartPath(MediaType mediaType, MediaArtifactType artifactType, File localFile) {
        File f = FanartUtil.getLocalFanartForFile(localFile, mediaType, artifactType, true);
        System.out.println("");
        System.out.println("        File: " + localFile.getAbsolutePath());
        System.out.println("       Image: " + f.getAbsolutePath());
        System.out.println("        Type: " + mediaType);
        System.out.println("    Artifact: " + artifactType);
    }

    private static void dumpFanartPath(MediaType mediaType, MediaArtifactType artifactType, String title, String centralFolder,
                                       Map<String, String> metadata) {
        File f = FanartUtil.getCentralFanartArtifact(mediaType, artifactType, title, centralFolder, metadata);
        System.out.println("");
        System.out.println("       Title: " + title);
        System.out.println("        Type: " + mediaType);
        System.out.println("    Artifact: " + artifactType);
        System.out.println("      Folder: " + f.getAbsolutePath());
    }

    private static void testFolder(IFanartSupport support, File file, MediaType mediaType, MediaArtifactType artifactType,
                                   String title, String centralFanart) {
        System.out.println("");
        System.out.println("        File: " + file.getName());
        System.out.println("       Title: " + title);
        System.out.println("        Type: " + mediaType);
        System.out.println("    Artifact: " + artifactType);
        System.out.println("");
    }
}
