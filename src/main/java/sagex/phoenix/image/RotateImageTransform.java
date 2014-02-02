package sagex.phoenix.image;

import java.awt.image.BufferedImage;

public class RotateImageTransform implements IBufferedTransform {
	double theta;

	public RotateImageTransform(double theta) {
		this.theta = theta;
	}

	public BufferedImage transform(BufferedImage image) {
		return ImageUtil.createRotatedImage(image, theta);
	}

	public String getTransformKey() {
		return "rotate_" + String.valueOf(theta);
	}

	public String toString() {
		return this.getClass().getName() + ": [" + getTransformKey() + "]";
	}
}
