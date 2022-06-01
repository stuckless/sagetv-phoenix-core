package sagex.phoenix.image;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.*;
import javax.imageio.stream.FileCacheImageOutputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOExceptionWithCause;
import org.apache.log4j.Logger;

import sage.ImageUtils;
import sagex.SageAPI;
import sagex.api.Utility;
import sagex.phoenix.Phoenix;
import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.metadata.MetadataConfiguration;
import sagex.phoenix.util.FileUtils;
//import sagex.remote.RemoteObjectRef;
import sagex.util.WaitFor;

public class ImageUtil {
    public static String DEFAULT_IMAGE_FORMAT = "jpg";
    public static String DEFAULT_IMAGE_MIME_TYPE = "image/jpg";
    public static String[] IMAGE_FORMATS = new String[]{"jpg", "png", "gif"};
    public static Pattern IMAGE_EXT_REGEX = Pattern.compile("\\.jpg$|\\.png$|\\.gif$", Pattern.CASE_INSENSITIVE);

    public static final String EXT_JPG = "jpg";
    public static final String EXT_PNG = "png";

    public static final FileFilter ImagesFilter = new FileFilter() {
        public boolean accept(File pathname) {
            if (pathname.isFile()) {
                // remove any 0 length files
                if (pathname.length() == 0) {
                    log.info("Removing 0 byte image: " + pathname.getAbsolutePath());
                    FileUtils.deleteQuietly(pathname);
                    return false;
                }
            }
            Matcher m = ImageUtil.IMAGE_EXT_REGEX.matcher(pathname.getName());
            return m.find();
        }
    };

    private static final Logger log = Logger.getLogger(ImageUtil.class);

    private static File imageCacheDir = null;
    private static File tmpCacheDir = null;

    public static File getImageCacheDir() {
        if (imageCacheDir == null) {
            File dir = new File(Phoenix.getInstance().getUserCacheDir(), "imagetrans");
            FileUtils.mkdirsQuietly(dir);
            imageCacheDir = dir;
            log.info("Created Image Cache Dir: " + dir + "; exists: " + dir.exists());
        }
        return imageCacheDir;
    }

    public static File getCachedImageFile(String imageId, String imageTag, String ext) {
        File cachdir = getImageCacheDir();
        if (log.isDebugEnabled()) {
            log.debug("GetCachedImageFile(): imageId: " + imageId + "; imageTag: " + imageTag + "; ext: " + ext + "; CacheDir: "
                    + cachdir);
        }
        File imageDir = new File(cachdir, imageTag);
        FileUtils.mkdirsQuietly(imageDir);
        String cacheImg = new String(Hex.encodeHex(DigestUtils.md5(imageId + "_" + imageTag))) + "." + ext;
        return new File(imageDir, cacheImg);
    }

    public static String getImageExt(String file) {
        if (file == null)
            return DEFAULT_IMAGE_FORMAT;
        int p = file.lastIndexOf(".");
        if (p == -1)
            return DEFAULT_IMAGE_FORMAT;
        return FilenameUtils.getExtension(file);
    }

    public static BufferedImage readImage(File in) throws IOException {
        return loadAndWaitForImage(in.toURI().toURL());
    }

    public static BufferedImage loadAndWaitForImage(URL in) throws IOException {
        if (in == null)
            return null;

        try {
            BufferedImage img = ImageUtils.fullyLoadImage(in);
            // sometimes sagetv can't load it, or it returns it's NullImage instance
            if (img == null || img == ImageUtils.getNullImage()) {
                throw new RuntimeException("Failed to load Image: " + in);
            }
            return img;
        } catch (Throwable t) {
            log.warn("Failed to load image using sage apis using ImageIO for " + in);
            return ImageIO.read(in);
        }
    }

    public static BufferedImage readImage(URL in) throws IOException {
        return loadAndWaitForImage(in);
    }

    public static void writeImageWithCompression(BufferedImage img, File file) throws IOException {
        String ext = FilenameUtils.getExtension(file.getName());

        img = fixImage(img, ext);

        Iterator<ImageWriter> i = ImageIO.getImageWritersBySuffix(ext);
        boolean imageWritten = false;
        for (; i.hasNext(); ) {
            ImageWriter jpegWriter = i.next();

            try {
                // Set the compression quality to 0.8
                ImageWriteParam param = jpegWriter.getDefaultWriteParam();
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(getDefaultImageCompression());

                // Write the image to a file
                FileImageOutputStream out = new FileImageOutputStream(file);
                jpegWriter.setOutput(out);
                jpegWriter.write(null, new IIOImage(img, null, null), param);
                jpegWriter.dispose();
                imageWritten = true;
                out.flush();
                out.close();
                break;
            } catch (Exception ee) {
                log.error("Failed to save image to stream", ee);
            }
        }

        if (!imageWritten) {
            log.warn("Using Default ImageWriter for " + file);
            ImageIO.write(img, ext, file);
        }
    }

    public static void writeImage(BufferedImage img, File file) throws IOException {

        writeImageWithCompression(img, file);

        if (!file.exists())
            throw new IOException("Failed to create image " + file);

        if (file.length() == 0) {
            FileUtils.deleteQuietly(file);
            throw new IOException("File to create image with any data for " + file);
        }
    }

    public static void writeImage(BufferedImage img, File file, int width, int height) throws IOException {

        if (img == null)
            throw new IOException("Null Image going to file " + file);
        img = createScaledImage(img, width, height);
        if (img == null)
            throw new IOException("Failed to scale image going to file " + file);

        log.info("Writing image file " + img + "; size: " + img.getWidth() + "; " + img.getHeight());

        writeImage(img, file);
    }

    /**
     * Writes the image to an output stream
     *
     * @param img
     * @param ext
     * @param os
     */
    public static void writeImage(BufferedImage img, String ext, OutputStream os) {
        try {
            writeImageWithCompression(img, ext, os);
            os.flush();
        } catch (IOException e) {
            log.error("Failed to save image to stream", e);
        }
    }

    /**
     * Fixes image type based on the file extension.  If you write argb images as jpg, things
     * get messed up.
     * @param img
     * @param ext
     * @return
     */
    static BufferedImage fixImage(BufferedImage img, String ext) {
        if ("jpg".equalsIgnoreCase(ext)) {
            // log.info("Adjusting JPG image during Java Colorspace Issue for file " + file);
            // set rgb color for image, because of issues in java jpeg colorspaces
            int w = img.getWidth();
            int h = img.getHeight();
            BufferedImage newImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            int[] rgb = img.getRGB(0, 0, w, h, null, 0, w);
            newImage.setRGB(0, 0, w, h, rgb, 0, w);
            img = newImage;
        }
        return img;
    }

    /**
     * Writes the image to an output stream
     *
     * @param img
     * @param ext
     * @param os
     */
    public static void writeImageWithCompression(BufferedImage img, String ext, OutputStream os) {
        img = fixImage(img, ext);

        try {
            Iterator<ImageWriter> i = ImageIO.getImageWritersBySuffix(ext);
            ImageWriter jpegWriter = i.next();

            // Set the compression quality to 0.8
            ImageWriteParam param = jpegWriter.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(getDefaultImageCompression());

            // Write the image to a file
            FileCacheImageOutputStream out = new FileCacheImageOutputStream(os, getTempImageCacheDir());
            jpegWriter.setOutput(out);
            jpegWriter.write(null, new IIOImage(img, null, null), param);
            jpegWriter.dispose();
            out.flush();
            out.close();
        } catch (IOException e) {
            log.error("Failed to save image to stream", e);
        }
    }

    private static MetadataConfiguration fanartConfig = null;

    private static MetadataConfiguration getFanartConfig() {
        if (fanartConfig == null) {
            fanartConfig = GroupProxy.get(MetadataConfiguration.class);
        }
        return fanartConfig;
    }

    private static float getDefaultImageCompression() {
        return getFanartConfig().getImageCompression();
    }

    private static File getTempImageCacheDir() {
        if (tmpCacheDir != null) {
            tmpCacheDir = new File(getImageCacheDir(), "tmp");
            tmpCacheDir.mkdirs();
        }
        return tmpCacheDir;
    }

    // API
    public static BufferedImage createScaledImage(BufferedImage imageSrc, int scaleWidth, int scaleHeight) {
        int origWidth = imageSrc.getWidth();
        int origHeight = imageSrc.getHeight();

        if (scaleWidth == -1) {
            // scale to height
            float div = (float) imageSrc.getHeight() / scaleHeight;
            scaleWidth = (int) (imageSrc.getWidth() / div);
        }

        if (scaleHeight == -1) {
            // scale to width
            float div = (float) imageSrc.getWidth() / scaleWidth;
            scaleHeight = (int) (imageSrc.getHeight() / div);
        }

        // don't do anything if the scaling is larger than the original
        if (scaleWidth >= origWidth && scaleHeight >= origHeight) {
            return imageSrc;
        }

        log.debug(String.format("Scaling Image from: %sx%s to %sx%s", imageSrc.getWidth(), imageSrc.getHeight(), scaleWidth,
                scaleHeight));

        if (SageAPI.isRemote()) {
            log.debug("Sage is Remote using internal scaling");
            return internal_scale(imageSrc, scaleWidth, scaleHeight);
        } else {
            if (imageSrc.getType() == BufferedImage.TYPE_INT_RGB) {
                return ImageUtils.createBestOpaqueScaledImage(imageSrc, scaleWidth, scaleHeight);
            } else {
                return ImageUtils.createBestScaledImage(imageSrc, scaleWidth, scaleHeight);
            }
        }
    }

    static BufferedImage internal_scale(BufferedImage imageToScale, int dWidth, int dHeight) {
        BufferedImage scaledImage = null;
        if (imageToScale != null) {

            scaledImage = new java.awt.image.BufferedImage(dWidth,
                    dHeight, imageToScale.getType());
            java.awt.Graphics2D g2 = scaledImage.createGraphics();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                    java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION,
                    java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2.setComposite(java.awt.AlphaComposite.Src);
            g2.drawImage(imageToScale, 0, 0, dWidth, dHeight, null);

            g2.dispose();
        }
        return scaledImage;
    }
    // API
    public static BufferedImage createJustReflection(BufferedImage img, float reflectionAlphaStart, float reflectionAlphaEnd) {
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();
        float opacityStart = 1.0f - reflectionAlphaStart;
        float opacityEnd = 1.0f - reflectionAlphaEnd;

        BufferedImage buffer = createJustReflectedImage(img, imgWidth, imgHeight);
        BufferedImage alphaMask = createGradientMask(imgWidth, imgHeight, opacityStart, opacityEnd);

        applyAlphaMask(buffer, alphaMask, 0);

        return buffer;/* .getSubimage(0, 0, imgWidth, imgHeight * 3 / 2) */
    }

    // API
    public static BufferedImage createReflection(BufferedImage img, float reflectionAlphaStart, float reflectionAlphaEnd) {
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();
        float opacityStart = 1.0f - reflectionAlphaStart;
        float opacityEnd = 1.0f - reflectionAlphaEnd;

        BufferedImage buffer = createReflectedImage(img, imgWidth, imgHeight);
        BufferedImage alphaMask = createGradientMask(imgWidth, imgHeight, opacityStart, opacityEnd);

        applyAlphaMask(buffer, alphaMask, imgHeight);

        return buffer;/* .getSubimage(0, 0, imgWidth, imgHeight * 3 / 2) */
    }

    // API
    public static BufferedImage createReflection(BufferedImage img) {
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();

        BufferedImage buffer = createReflectedImage(img, imgWidth, imgHeight);
        BufferedImage alphaMask = createGradientMask(imgWidth, imgHeight);

        applyAlphaMask(buffer, alphaMask, imgHeight);

        return buffer;/* .getSubimage(0, 0, imgWidth, imgHeight * 3 / 2) */
    }

    public static BufferedImage createReflectedPicture(BufferedImage img, BufferedImage alphaMask) {
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();

        BufferedImage buffer = createReflectedImage(img, imgWidth, imgHeight);

        applyAlphaMask(buffer, alphaMask, imgHeight);

        return buffer;/* .getSubimage(0, 0, imgWidth, imgHeight * 3 / 2) */
    }

    private static void applyAlphaMask(BufferedImage buffer, BufferedImage alphaMask, int imgHeight) {

        Graphics2D g2 = buffer.createGraphics();
        g2.setComposite(AlphaComposite.DstOut);
        g2.drawImage(alphaMask, null, 0, imgHeight);
        g2.dispose();
    }

    private static BufferedImage createReflectedImage(BufferedImage img, int imgWidth, int imgHeight) {

        BufferedImage buffer = new BufferedImage(imgWidth, imgHeight * 5 / 3, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = buffer.createGraphics();

        g.drawImage(img, null, null);
        g.translate(0, imgHeight * 2);

        AffineTransform reflectTransform = AffineTransform.getScaleInstance(1.0, -1.0);
        g.drawImage(img, reflectTransform, null);

        g.dispose();

        return buffer;
    }

    private static BufferedImage createJustReflectedImage(BufferedImage img, int imgWidth, int imgHeight) {

        BufferedImage buffer = new BufferedImage(imgWidth, imgHeight * 2 / 3, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = buffer.createGraphics();

		/* g.drawImage(img, null, null); */

        g.translate(0, imgHeight);
        AffineTransform reflectTransform = AffineTransform.getScaleInstance(1.0, -1.0);
        g.drawImage(img, reflectTransform, null);

        g.dispose();

        return buffer;
    }

    public static BufferedImage createGradientMask(int imgWidth, int imgHeight) {
        return createGradientMask(imgWidth, imgHeight, 0.7f, 1.0f);
    }

    // API
    public static BufferedImage createGradientMask(int imgWidth, int imgHeight, float opacityStart, float opacityEnd) {
        BufferedImage gradient = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = gradient.createGraphics();
        GradientPaint painter = new GradientPaint(0.0f, 0.0f, new Color(1.0f, 1.0f, 1.0f, opacityStart), 0.0f, imgHeight / 2.0f,
                new Color(1.0f, 1.0f, 1.0f, opacityEnd));
        g.setPaint(painter);
        g.fill(new Rectangle2D.Double(0, 0, imgWidth, imgHeight));

        g.dispose();

        return gradient;
    }

    // API
    public static BufferedImage createPerspective(BufferedImage img, double scalex, double shifty) {

        double w = img.getWidth();
        double h = img.getHeight();
        double deltah = w * shifty;

        w = w * scalex;
        h = h + Math.abs(deltah);

        int imgWidth = (int) w;
        int imgHeight = (int) h;

        BufferedImage buffer = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = buffer.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        AffineTransform perspectiveTransform = new AffineTransform(scalex, shifty, 0.0, 1.0, 0.0, Math.max(-deltah, 0.0));

        g.drawImage(img, perspectiveTransform, null);

        g.dispose();

        return buffer;
    }

    // API
    public static BufferedImage createRotatedImage(BufferedImage img, double theta) {
        theta = theta * Math.PI / 180.0;

        double sin = Math.abs(Math.sin(theta));
        double cos = Math.abs(Math.cos(theta));
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();
        int newWidth = (int) Math.floor(imgWidth * cos + imgHeight * sin);
        int newHeight = (int) Math.floor(imgHeight * cos + imgWidth * sin);

        BufferedImage buffer = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = buffer.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        g.translate((newWidth - imgWidth) / 2, (newHeight - imgHeight) / 2);
        g.rotate(theta, imgWidth / 2d, imgHeight / 2d);
        g.drawImage(img, null, null);

        g.dispose();

        return buffer;
    }

    public static BufferedImage getImageAsBufferedImage(Object image) throws IOException {
        if (image == null) {
            return null;
        }

        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        } else if (image instanceof String) {
            return readImage(new File((String) image));
        } else if (image instanceof File) {
            return readImage((File) image);
        } else if (image instanceof URL) {
            return readImage((URL) image);
        } else {
            log.warn("Using SageAPI to convert the sage image into a BufferedImage");
            return Utility.GetImageAsBufferedImage(image);
        }
    }

    public static void ClearImageDiskCache() {
        File cache = getImageCacheDir();
        if (cache != null) {
            try {
                org.apache.commons.io.FileUtils.cleanDirectory(cache);
            } catch (IOException e) {
                log.warn("Failed to clean Image Cache: " + cache);
            }
        }
    }

    public static long getCacheSize() {
        return org.apache.commons.io.FileUtils.sizeOfDirectory(getImageCacheDir());
    }


    /**
     * Returns the ImageSize for the given File Image without loading the ENTIRE image
     *
     * @param image
     * @return
     */
    public static Dimension getImageSize(URL image) {
        try (InputStream is = image.openStream()){
            return getImageSize(is);
        } catch (IOException e) {
            return null;
        }
    }


    /**
     * Returns the ImageSize for the given File Image without loading the ENTIRE image
     *
     * @param image
     * @return
     */
    public static Dimension getImageSize(File image) {
        try (InputStream is = new FileInputStream(image)){
            return getImageSize(is);
        } catch (IOException e) {
            return null;
        }
    }


    /**
     * Gets the Image Size without having to load the entire image
     * @param image
     * @return
     */
    public static Dimension getImageSize(InputStream image) {
        try(ImageInputStream in = ImageIO.createImageInputStream(image)){
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(in);
                    return new Dimension(reader.getWidth(0), reader.getHeight(0));
                } finally {
                    reader.dispose();
                }
            }
            return null;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
}
