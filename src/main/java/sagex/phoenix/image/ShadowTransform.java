package sagex.phoenix.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import sagex.phoenix.image.extra.ShadowFactory;

public class ShadowTransform implements IBufferedTransform {
    private ShadowFactory factory = new ShadowFactory();

    public ShadowTransform(int size, float opacity, int color) {
        factory.setOpacity(opacity);
        factory.setSize(size);
        factory.setColor(new Color(color));
        factory.setRenderingHint(ShadowFactory.KEY_BLUR_QUALITY, ShadowFactory.VALUE_BLUR_QUALITY_HIGH);
    }

    public BufferedImage transform(BufferedImage image) {
        BufferedImage img = factory.createShadow(image);
        Graphics2D g = img.createGraphics();
        g.drawImage(image, factory.getSize(), factory.getSize(), null);
        g.dispose();
        return img;
    }

}
