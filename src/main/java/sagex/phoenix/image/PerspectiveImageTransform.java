package sagex.phoenix.image;

import java.awt.image.BufferedImage;

public class PerspectiveImageTransform implements IBufferedTransform {
    double scalex;
    double shifty;
    
    public PerspectiveImageTransform(double scalex, double shifty) {
        this.scalex=scalex;
        this.shifty=shifty;
    }

    public BufferedImage transform(BufferedImage image) {
        return ImageUtil.createPerspective(image, scalex, shifty);
    }

    public String getTransformKey() {
        return "perspective_"+String.valueOf(scalex)+"_" + String.valueOf(shifty);
    }
    
    public String toString() {
        return this.getClass().getName() + ": [" + getTransformKey() + "]";
    }
}
