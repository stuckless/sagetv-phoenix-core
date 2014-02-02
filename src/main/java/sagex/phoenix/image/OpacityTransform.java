package sagex.phoenix.image;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class OpacityTransform implements IBufferedTransform {
	private float opacity = 0;

	public OpacityTransform(float opacity) {
		this.opacity = opacity;
	}

	public BufferedImage transform(BufferedImage image) {
		BufferedImage mask = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2d = mask.createGraphics();
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
		g2d.drawImage(image, 0, 0, null);
		g2d.dispose();

		return mask;
	}
}
