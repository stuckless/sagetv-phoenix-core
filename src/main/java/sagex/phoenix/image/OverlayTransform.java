package sagex.phoenix.image;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import sagex.phoenix.util.Loggers;

public class OverlayTransform implements IBufferedTransform {
    private float opacity = 0;
    private String filename;
    private int x;
    private int y;

    public OverlayTransform(String filename, float opacity, int x, int y) {
        this.filename = filename;
        this.opacity = opacity;
        this.x = x;
        this.y = y;
    }

    public BufferedImage transform(BufferedImage image) {
        BufferedImage mask = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = mask.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        BufferedImage imgOver;
        try {
            imgOver = ImageUtil.readImage(new File(filename));
            g2d.drawImage(imgOver, x, y, null);
            g2d.dispose();
        } catch (IOException e) {
            Loggers.LOG.warn("Failed to process image overlay using file " + filename, e);
        }

        return mask;
    }
}
