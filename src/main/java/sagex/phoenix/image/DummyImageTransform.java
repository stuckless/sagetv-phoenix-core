package sagex.phoenix.image;

import java.awt.image.BufferedImage;

/**
 * Dummy transform will simply do nothing except pass the image back, without doing anything.
 * 
 * @author sean
 */
public class DummyImageTransform implements IBufferedTransform {
    public DummyImageTransform() {
    }

    public BufferedImage transform(BufferedImage image) {
    	return image;
    }
}
