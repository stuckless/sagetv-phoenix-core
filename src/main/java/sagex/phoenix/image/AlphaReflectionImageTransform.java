package sagex.phoenix.image;

import java.awt.image.BufferedImage;

public class AlphaReflectionImageTransform implements IBufferedTransform {
	float reflectionAlphaStart;
	float reflectionAlphaEnd;

	public AlphaReflectionImageTransform(float reflectionAlphaStart, float reflectionAlphaEnd) {
		this.reflectionAlphaEnd = reflectionAlphaEnd;
		this.reflectionAlphaStart = reflectionAlphaStart;
	}

	public BufferedImage transform(BufferedImage image) {
		return ImageUtil.createReflection(image, reflectionAlphaStart, reflectionAlphaEnd);
	}

	public String getTransformKey() {
		return "reflection_" + String.valueOf(reflectionAlphaStart) + "_" + String.valueOf(reflectionAlphaEnd);
	}

	public String toString() {
		return this.getClass().getName() + ": [" + getTransformKey() + "]";
	}
}
