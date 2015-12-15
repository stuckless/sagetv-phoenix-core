package test;

import sagex.SageAPI;
import sagex.api.MediaFileAPI;
import sagex.phoenix.fanart.SageFanartUtil;

import java.io.File;

public class TestSetMetadataForJeff {
    public static void main(String args[]) {
        SageAPI.setProvider(SageAPI.getRemoteProvider());

        Object mediafile = MediaFileAPI.GetMediaFileForFilePath(new File(
                "/home/FileServer/Media/Videos/VideoCollection/TV/30Rock/Season 1/30.Rock.S01E19.Corporate.Crush.avi"));
        System.out.println("Title: " + MediaFileAPI.GetMediaTitle(mediafile));

        String title = MediaFileAPI.GetMediaFileMetadata(mediafile, "MediaTitle");
        // if (StringUtils.isEmpty(title)) {
        // System.out.println("Setting Title");
        SageFanartUtil.SetMediaFileMetadata(mediafile, "MediaTitle", "30 RocknRoll");
        // }
        System.out.println("Title: " + MediaFileAPI.GetMediaFileMetadata(mediafile, "MediaTitle"));

    }
}
