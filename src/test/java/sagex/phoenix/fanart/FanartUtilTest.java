package sagex.phoenix.fanart;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import sagex.phoenix.image.ImageUtil;
import sagex.phoenix.progress.IProgressMonitor;
import sagex.phoenix.progress.LogProgressMonitor;
import test.InitPhoenix;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import static test.junit.lib.Utils.*;

/**
 * Created by seans on 24/12/16.
 */
public class FanartUtilTest {

    @BeforeClass
    public static void init() {
        try {
            InitPhoenix.init(false, true, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testScreenScaling() throws Exception {
        File dest = File.createTempFile("tmp-scaled-",".jpg");
        //dest.deleteOnExit();
        FanartUtil.applyScreenScalingOnSourceImage(this.getClass().getClassLoader().getResource("big-poster.jpg"), dest, "1920x1080");
        BufferedImage img = ImageUtil.readImage(dest);
        assertNotNull("Dest Image was null and should not be",img);
        assertEquals(1080,img.getHeight());
        assertEquals(2000*1080/3000,img.getWidth());


        File fanartDir = makeDir("TmpImages");
        File f1 = createImageFile(new File(fanartDir, "back.jpg"), 3000, 2000);
        FanartUtil.applyScreenScalingOnSourceImage(f1, f1, "1920x1080");
        verifyImageSize(f1, 1920, 1280);

        f1 = createImageFile(new File(fanartDir, "back_small.jpg"), 640, 480);
        FanartUtil.applyScreenScalingOnSourceImage(f1, f1, "1920x1080");
        verifyImageSize(f1, 640, 480);

    }

    @Test
    public void testApplyScalingToAllFanart() throws IOException {
        File fanartDir = makeDir("TmpFanart");
        File f1 = createImageFile(new File(fanartDir, "Movies/Movie1/poster1.jpg"), 2000, 3000);
        File f2 = createImageFile(new File(fanartDir, "Movies/Movie2/poster1.jpg"), 2000, 3000);
        File f3 = createImageFile(new File(fanartDir, "Movies/Movie3/Posters/poster1.jpg"), 2000, 3000);
        IProgressMonitor monitor = new LogProgressMonitor(Logger.getLogger(FanartUtilTest.class));
        FanartUtil.applyScreenScalingToAllImageFiles(fanartDir, "640x480", monitor);
        verifyImageSize(f1, 320, 480);
        verifyImageSize(f2, 320, 480);
        verifyImageSize(f3, 320, 480);
    }
}