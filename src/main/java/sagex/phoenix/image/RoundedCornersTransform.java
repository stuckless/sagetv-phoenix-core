package sagex.phoenix.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Creates an image with Rounded corners.
 *
 * @author seans
 */
public class RoundedCornersTransform implements IBufferedTransform {
    public int arcSize = 0;

    public RoundedCornersTransform(int arcSize) {
        this.arcSize = arcSize;
    }

    public BufferedImage transform(BufferedImage in) {
        int width = in.getWidth();
        int height = in.getHeight();

        BufferedImage mask = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = mask.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setColor(Color.WHITE);
        graphics.fillRoundRect(0, 0, width, height, arcSize, arcSize);
        graphics.setComposite(AlphaComposite.SrcIn);
        graphics.drawImage(in, 0, 0, null);
        graphics.dispose();
        return mask;
    }
}
