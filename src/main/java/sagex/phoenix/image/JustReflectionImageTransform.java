package sagex.phoenix.image;

import java.awt.image.BufferedImage;

public class JustReflectionImageTransform implements IBufferedTransform {
	float reflectionAlphaStart;
	float reflectionAlphaEnd;

	public JustReflectionImageTransform(float reflectionAlphaStart, float reflectionAlphaEnd) {
		this.reflectionAlphaEnd = reflectionAlphaEnd;
		this.reflectionAlphaStart = reflectionAlphaStart;
	}

	public BufferedImage transform(BufferedImage image) {
		return ImageUtil.createJustReflection(image, reflectionAlphaStart, reflectionAlphaEnd);
	}

	public String getTransformKey() {
		return "just_reflection_" + String.valueOf(reflectionAlphaStart) + "_" + String.valueOf(reflectionAlphaEnd);
	}

	public String toString() {
		return "JustReflectionImageTransform: [" + getTransformKey() + "]";
	}
}
