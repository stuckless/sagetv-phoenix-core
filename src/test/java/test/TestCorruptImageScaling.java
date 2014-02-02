package test;

import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class TestCorruptImageScaling {
	public static void main(String args[]) throws Exception {
		InitPhoenix.init(false, true);
		File imageIn = new File("NoCommit/testimages/79d4b5d02165889b4dc98857284b6b9c-164541-6.jpg");
		File imageOut = new File("NoCommit/testimages/79d4b5d02165889b4dc98857284b6b9c-164541-6.jpg_scaled.jpg");
		BufferedImage img = readImage(imageIn);// ImageUtil.getImageAsBufferedImage(imageIn);
		BufferedImage imgNew;
		// imgNew =
		// Phoenix.getInstance().getTransformFactory().applyTransform(img,
		// "{name:scale, width:200}");
		ImageIO.write(img, "JPEG", imageOut);
		System.out.println("Image Written");
	}

	public static BufferedImage readImage(File in) {
		BufferedImage bufferedImage = null;
		int w = 0;
		int h = 0;
		try {
			bufferedImage = ImageIO.read(in);
			w = bufferedImage.getWidth();
			h = bufferedImage.getHeight();
		} catch (IOException e) {
			e.printStackTrace();
		}
		WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, w, h, 4, null);
		ComponentColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
				new int[] { 8, 8, 8, 8 }, true, false, ComponentColorModel.TRANSLUCENT, DataBuffer.TYPE_BYTE);
		BufferedImage dukeImg = new BufferedImage(colorModel, raster, false, null);

		Graphics2D g = dukeImg.createGraphics();
		g.drawImage(bufferedImage, 0, 0, null);

		return dukeImg;
	}
}
