package sagex.phoenix.image;

import java.awt.image.BufferedImage;

public class GradientImageTransform implements IBufferedTransform {
    int imgWidth;
    int imgHeight;
    float opacityStart;
    float opacityEnd;
    
    public GradientImageTransform(int imgWidth, int imgHeight, float opacityStart, float opacityEnd) {
        this.imgWidth=imgWidth;
        this.imgHeight=imgHeight;
        this.opacityStart=opacityStart;
        this.opacityEnd=opacityEnd;
    }

    public BufferedImage transform(BufferedImage image) {
        return ImageUtil.createGradientMask(imgWidth, imgHeight, opacityStart, opacityEnd);
    }

    public String getTransformKey() {
        return getKey(imgWidth, imgHeight, opacityStart, opacityEnd);
    }
    
    public static String getKey(int imgWidth, int imgHeight, float opacityStart, float opacityEnd) {
        return "gradient_" + String.valueOf(imgWidth) + "_" + String.valueOf(imgHeight) + "_" + String.valueOf(opacityStart) + "_" + String.valueOf(opacityEnd);
    }
    
    public String toString() {
        return this.getClass().getName() + ": [" + getTransformKey() + "]";
    }
}
