package sagex.phoenix.image;

import java.awt.image.BufferedImage;

public class ScaledImageTransform implements IBufferedTransform {
    private int width=-1;
    private int height = -1;
    
    public ScaledImageTransform(int w, int h) {
        this.width=w;
        this.height=h;
    }

    public BufferedImage transform(BufferedImage image) {
        return ImageUtil.createScaledImage(image, width, height);
    }

    public String getTransformKey() {
        return width + "x" + height;
    }
    
    public String toString() {
        return "ScaledImageTransform: [" + getTransformKey() + "]";
    }
}
