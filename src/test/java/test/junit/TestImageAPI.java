package test.junit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static test.junit.lib.TestUtil.createImageFile;
import static test.junit.lib.TestUtil.verifyImageSize;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.image.IBufferedTransform;
import sagex.phoenix.image.ImageUtil;
import sagex.phoenix.image.TransformFactory;
import test.InitPhoenix;

public class TestImageAPI {
    @BeforeClass
    public static void init() throws Exception {
        InitPhoenix.init(true, true);
    }

    @Test
    public void testCreateImage() throws Exception {
        File src = createImageFile("test_image.jpg", 200, 800);
        Object image = phoenix.image.CreateImage("myimage", "test", src, "{name: scale, width: 100}", true);
        verifyImageSize(image, 100, 400);

        File f = ImageUtil.getCachedImageFile("myimage", "test", "jpg");
        assertTrue("cached file not created.", f.exists());

        long tstamp = f.lastModified();

        // run it again, and this time we should have the same image
        image = phoenix.image.CreateImage("myimage", "test", src, "{name: scale, width: 150}", false);
        f = ImageUtil.getCachedImageFile("myimage", "test", "jpg");
        verifyImageSize(image, 100, 400);
        assertTrue("created a new imaged and it should not have", tstamp == f.lastModified());

        // run it again, and this time we should have a new image
        // (overwrite=true)
        image = phoenix.image.CreateImage("myimage", "test", src, "{name: scale, width: 150}", true);
        f = ImageUtil.getCachedImageFile("myimage", "test", "jpg");
        verifyImageSize(image, 150, 600);

        // touch the image file and run the test again, this time it should be
        // updated
        src.setLastModified(System.currentTimeMillis() + (60 * 1000 * 60 * 24));
        tstamp = src.lastModified();
        image = phoenix.image.CreateImage("myimage", "test", src, "{name: scale, width: 100}", false);
        f = ImageUtil.getCachedImageFile("myimage", "test", "jpg");
        assertFalse("file not modified", tstamp == f.lastModified());
        assertEquals("dir should only have a single entry", 1, f.getParentFile().listFiles().length);

        // another transformation using the same file and tag, but different
        // transform
        // should result using the old cached image
        image = phoenix.image.CreateImage("myimage2", "test", src, "{name: scale, width: 150}", true);
        verifyImageSize(image, 150, 600);
        assertEquals("dir should only have 2 entries", 2, f.getParentFile().listFiles().length);
        Object image2 = phoenix.image.GetImage("myimage2", "test");
        verifyImageSize(image2, 150, 600);

        // get other permutations of CreateImage
        image = phoenix.image.CreateImage(src, "{name: scale, width: 100}", true);
        verifyImageSize(image, 100, 400);

        image = phoenix.image.CreateImage("poster_small", src, "{name: scale, width: 100}", true);
        verifyImageSize(image, 100, 400);
    }

    @Test
    public void testRegisterTransform() throws IOException {
        File src = createImageFile("register_transform.jpg", 200, 800);
        phoenix.api.RegisterImageTransform("{name: scale, width: 100, id:mytransform}");

        Object image = phoenix.api.CreateImage("register_transform2", "register_transform2", src, "{id:mytransform}", true);
        verifyImageSize(image, 100, 400);
    }

    @Test
    public void testCachedImages() {
        File f = ImageUtil.getCachedImageFile("id", "tag", "jpg");
        assertEquals("incorrect parent file", "tag", f.getParentFile().getName());
        assertEquals("incorrect cached filename", DigestUtils.md5Hex("id" + "_" + "tag"),
                f.getName().substring(0, f.getName().indexOf(".")));
    }

    @Test
    public void testScaledImage() throws IOException {
        File src = createImageFile("test_scale.jpg", 200, 800);
        Object image = phoenix.api.CreateScaledImage(src, 100, -1);
        verifyImageSize(image, 100, 400);
    }

    @Test
    public void testRotateImage() throws IOException {
        File src = createImageFile("test_rotate.jpg", 200, 800);
        Object image = phoenix.api.CreateRotatedImage(src, 90);
        verifyImageSize(image, 800, 200);
    }

    @Test
    public void testReflectionImage() {
        File src = createImageFile("test_reflection.jpg", 200, 800);
        Object image = phoenix.api.CreateReflection(src);
        assertNotNull(image);
    }

    @Test
    public void testJustReflectionImage() {
        File src = createImageFile("test_justreflection.jpg", 200, 800);
        Object image = phoenix.api.CreateJustReflection(src, 1, 0);
        assertNotNull(image);
    }

    @Test
    public void testSamplJSTransform() throws Exception {
        File src = createImageFile("test_javascript.jpg", 200, 800);
        TransformFactory tf = new TransformFactory(new File("../../src/main/STVs/Phoenix/ImageTransforms"));
        IBufferedTransform bt = tf.createTransform("{name: resize, width: 100, height: 500}");
        BufferedImage bi = bt.transform(ImageUtil.readImage(src));
        verifyImageSize(bi, 100, 500);
    }
}
