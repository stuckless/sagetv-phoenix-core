package test.junit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static test.junit.lib.TestUtil.createBufferedImage;
import static test.junit.lib.TestUtil.verifyImageSize;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.phoenix.Phoenix;
import sagex.phoenix.image.AlphaReflectionImageTransform;
import sagex.phoenix.image.CompositeTransform;
import sagex.phoenix.image.IBufferedTransform;
import sagex.phoenix.image.ImageUtil;
import sagex.phoenix.image.JavascriptBufferedTransform;
import sagex.phoenix.image.JustReflectionImageTransform;
import sagex.phoenix.image.OpacityTransform;
import sagex.phoenix.image.OverlayTransform;
import sagex.phoenix.image.PerspectiveImageTransform;
import sagex.phoenix.image.ReflectionImageTransform;
import sagex.phoenix.image.RotateImageTransform;
import sagex.phoenix.image.ScaledImageTransform;
import sagex.phoenix.image.ShadowTransform;
import sagex.phoenix.image.TransformFactory;
import test.InitPhoenix;

public class TestTransformFactory {
	private static TransformFactory factory = null;

	@BeforeClass
	public static void init() throws Exception {
		InitPhoenix.init(true, true);
		factory = Phoenix.getInstance().getTransformFactory();
	}

	@Test
	public void testTransformFactory() throws Exception {
		IBufferedTransform bt = null;

		// test without the {} notation
		bt = factory.createTransform("name: 'scale', width: 200");
		assertNotNull("failed to create scaled transform", bt);
		assertTrue("transform is not a scaled transforam", bt instanceof ScaledImageTransform);

		// test with the {} notation
		bt = factory.createTransform("{name: 'scale', width: 200}");
		assertNotNull("failed to create scaled transform", bt);
		assertTrue("transform is not a scaled transforam", bt instanceof ScaledImageTransform);

		// test composite
		bt = factory.createTransform("[{name: 'scale', width: 200},{name: 'rotate', theta: .90}]");
		assertNotNull("failed to create composite transform", bt);
		assertTrue("transform is not a scaled transforam", bt instanceof CompositeTransform);
		assertEquals("composite should have 2 items", 2, ((CompositeTransform) bt).size());
		assertEquals("hash code is always the same", bt.hashCode(), bt.hashCode());

		// test the caching, ie, 2 calls should produce the exact same transform
		IBufferedTransform bt2 = factory.createTransform("[{name: 'scale', width: 200},{name: 'rotate', theta: .90}]");
		assertEquals("Tranform Caching is broken", bt.hashCode(), bt2.hashCode());
	}

	@Test
	public void testScaledTransform() throws Exception {
		IBufferedTransform bt = factory.createTransform("name: 'scale', width: 200");
		assertNotNull("failed to create scaled transform", bt);
		assertTrue("transform is not a scaled transforam", bt instanceof ScaledImageTransform);

		verifyImageSize(factory.applyTransform(createBufferedImage(400, 800), "{name: scale, width: 200}"), 200, 400);
		verifyImageSize(factory.applyTransform(createBufferedImage(400, 800), "{name: scale, height: 200}"), 100, 200);
		verifyImageSize(factory.applyTransform(createBufferedImage(400, 800), "{name: scale, width: 200, height: 300}"), 200, 300);
	}

	@Test
	public void testRotateTransform() throws Exception {
		IBufferedTransform bt = factory.createTransform("{name: 'rotate', theta: 90}");
		assertNotNull("failed to create rotate transform", bt);
		assertTrue("transform is not a rotate transforam", bt instanceof RotateImageTransform);

		verifyImageSize(factory.applyTransform(createBufferedImage(400, 800), "{name: rotate, theta: 90}"), 800, 400);
		verifyImageSize(factory.applyTransform(createBufferedImage(400, 800), "{name: rotate, theta: 180}"), 400, 800);
		verifyImageSize(factory.applyTransform(createBufferedImage(400, 800), "{name: rotate, theta: 270}"), 800, 400);
		verifyImageSize(factory.applyTransform(createBufferedImage(400, 800), "{name: rotate, theta: 360}"), 400, 800);
	}

	@Test
	public void testCompositeTransform() throws Exception {
		verifyImageSize(
				factory.applyTransform(createBufferedImage(400, 800), "[{name: scale, width:100},{name: rotate, theta: 90}]"), 200,
				100);
	}

	@Test
	public void testGradientTransform() throws Exception {
		verifyImageSize(factory.applyTransform(null, "{name: gradient, width: 500, height: 600, opacityStart:0, opacityEnd:1}"),
				500, 600);
	}

	@Test
	public void testReflectionTransform() throws Exception {
		IBufferedTransform bt = factory.createTransform("{name: reflection}");
		assertNotNull("failed to create reflection transform", bt);
		assertTrue("transform is not a reflection transforam", bt instanceof ReflectionImageTransform);

		bt = factory.createTransform("{name: reflection, alphaStart:0, alphaEnd:1.0}");
		assertNotNull("failed to create alpha relfection transform", bt);
		assertTrue("transform is not a alpha reflection transforam", bt instanceof AlphaReflectionImageTransform);

		bt = factory.createTransform("{name: just_reflection, alphaStart:0, alphaEnd:1.0}");
		assertNotNull("failed to create alpha relfection transform", bt);
		assertTrue("transform is not a alpha reflection transforam", bt instanceof JustReflectionImageTransform);
	}

	@Test
	public void testPerspectiveTransform() throws Exception {
		IBufferedTransform bt = factory.createTransform("{name: perspective, scalex:10, shifty:20}");
		assertNotNull("failed to create perspective transform", bt);
		assertTrue("transform is not a reflection transforam", bt instanceof PerspectiveImageTransform);
	}

	@Test
	public void testTransformRegistry() throws Exception {
		IBufferedTransform bt = factory.createTransform("{name: scale, width: 200, id:poster_med}");
		assertNotNull("failed to create transform", bt);

		IBufferedTransform bt2 = factory.createTransform("{id: poster_med}");
		assertEquals("tranform by json id: failed", bt.hashCode(), bt2.hashCode());

		IBufferedTransform bt3 = factory.getTransform("poster_med");
		assertEquals("get transform by id failed", bt.hashCode(), bt3.hashCode());
	}

	@Test
	public void testJavascriptTransform() throws Exception {
		File jsDir = Phoenix.getInstance().getJavascriptImageTransformDir();
		File js = new File(jsDir, "testjs.js");

		String transform = "function transform(image, args) { return util.newImage(args.getInt('width'), args.getInt('height')); }";
		FileWriter fw = new FileWriter(js);
		fw.write(transform);
		fw.flush();
		fw.close();

		IBufferedTransform bt = factory.createTransform("{name: testjs, width:10, height:15}");
		assertNotNull("failed to create javascript transform", bt);
		assertTrue("Not a javascript transform", bt instanceof JavascriptBufferedTransform);

		Object image = bt.transform(null);
		verifyImageSize((BufferedImage) image, 10, 15);
	}

	@Test
	public void testShadowTransform() throws Exception {
		BufferedImage bi = ImageUtil.readImage(new File("src/test/images/test.png"));
		IBufferedTransform bt = factory.createTransform("{name: shadow, size: 20, opacity: .8, color: 0xffffff}");

		assertNotNull("failed to create perspective transform", bt);
		assertTrue("transform is not a shadow transforam", bt instanceof ShadowTransform);

		BufferedImage out = bt.transform(bi);
		File outDir = new File("target/images/");
		outDir.mkdirs();
		ImageUtil.writeImage(out, new File("target/images/test-shadow.png"));

	}

	@Test
	public void testOpacityTransform() throws Exception {
		BufferedImage bi = ImageUtil.readImage(new File("src/test/images/test.png"));
		IBufferedTransform bt = factory.createTransform("{name: opacity, opacity: .2}");

		assertNotNull("failed to create opacity transform", bt);
		assertTrue("transform is not a opacity transforam", bt instanceof OpacityTransform);

		BufferedImage out = bt.transform(bi);
		File outDir = new File("target/images/");
		outDir.mkdirs();
		ImageUtil.writeImage(out, new File("target/images/test-opacity.png"));
	}

	@Test
	public void testOverlayTransform() throws Exception {
		BufferedImage bi = ImageUtil.readImage(new File("src/test/images/test.png"));
		IBufferedTransform bt = factory
				.createTransform("{name: overlay, image: 'src/test/images/test_small.png', opacity:.5, x:100, y:100}");

		assertNotNull("failed to create overlay transform", bt);
		assertTrue("transform is not a overlay transforam", bt instanceof OverlayTransform);

		BufferedImage out = bt.transform(bi);
		File outDir = new File("target/images/");
		outDir.mkdirs();
		ImageUtil.writeImage(out, new File("target/images/test-overlay.png"));
	}
}
