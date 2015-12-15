package test.junit;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.metadata.ICastMember;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.trailers.AppleTrailerFolder;
import test.InitPhoenix;

public class TestTrailersVFS {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        InitPhoenix.init(true, true);
    }

    @Test
    public void testTrailers() {
        System.out.println("Creating Trailer Folder");
        AppleTrailerFolder fold = new AppleTrailerFolder("Just Added",
                "http://trailers.apple.com/trailers/home/feeds/just_added.json");
        System.out.println("Getting Items");
        List<IMediaResource> children = fold.getChildren();
        System.out.println("Iterating Items");
        for (IMediaResource r : children) {
            System.out.printf("Title: %s (%s)\n", r.getTitle(), ((IMediaFile) r).getMetadata().getYear());
            System.out.println("Rating: " + ((IMediaFile) r).getMetadata().getRated());
            System.out.println("Date: " + ((IMediaFile) r).getMetadata().getOriginalAirDate());
            for (ICastMember cm : ((IMediaFile) r).getMetadata().getActors()) {
                System.out.println("Actor: " + cm.getName());
            }
            System.out.println("");
        }
        System.out.println("Done");
    }
}
