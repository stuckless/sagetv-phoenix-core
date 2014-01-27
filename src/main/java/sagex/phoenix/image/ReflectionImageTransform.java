package sagex.phoenix.image;

import java.awt.image.BufferedImage;

public class ReflectionImageTransform implements IBufferedTransform {
    public ReflectionImageTransform() {
    }

    public BufferedImage transform(BufferedImage image) {
        return ImageUtil.createReflection(image);
    }

    public String getTransformKey() {
        return "reflection";
    }
    
    public String toString() {
        return this.getClass().getName() + ": [" + getTransformKey() + "]";
    }
}
