package test;

import java.io.IOException;

import sagex.SageAPI;

public class TestScaling {
	public static void main(String args[]) throws IOException {
		SageAPI.setProvider(SageAPI.getRemoteProvider());
		String image = "Terminator_1920x1080.png";
		System.out.println("Doing");

		// File f = ImageUtil.getCachedImageFile(image, "test");
		// BufferedImage bimg = ImageIO.read(new File(image));
		// BufferedImage oimg = ImageUtil.createScaledImage(bimg, -1, 100);
		// BufferedImage oimg = ImageUtil.createReflection(bimg);
		// ImageUtil.saveImage(oimg, f);

		phoenix.api.CreateScaledImage(image, -1, 4000);

		System.out.println("Done");
	}
}
