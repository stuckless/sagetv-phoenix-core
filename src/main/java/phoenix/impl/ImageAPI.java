package phoenix.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import sagex.phoenix.Phoenix;
import sagex.phoenix.image.ImageUtil;
import sagex.phoenix.tools.annotation.API;
import sagex.phoenix.util.ElapsedTimer;

/**
 * Set of Image APIs for performing transformations on images. Typically in all
 * cases, transformed images will be cached to the filesystem, if at all
 * possible, so that repeat calls to the same image transformation will simply
 * reuse the cached image file. <br>
 * <br>
 * Transformations are basically json (javascript object notation) structures
 * that define one or more transformations. The followsing transformations exist
 * <ul>
 * <li>{name: scale, width: 150, height: -1}
 * <li>{name: reflection}
 * <li>{name: reflection, alphaStart:0, alphaEnd:1.0}
 * <li>{name: just_reflection, alphaStart:0, alphaEnd:1.0}
 * <li>{name: perspective, scalex:10, shifty:20}
 * <li>{name: gradient, width: 500, height: 600, opacityStart:0, opacityEnd:1}
 * <li>{name: rotate, theta: .90}
 * <li>{name: shadow, size: 10, opacity: .8, color: 0xff00ff}
 * <li>{name: opacity, opacity: .8}
 * <li>{name: overlay, image: 'src/test/images/test_small.jpg', opacity:0.5,
 * x:100, y:100}
 * <li>{name: rounded, arcSize:10}
 * <li>{name: dummy}
 * </ul>
 * 
 * Because a transformation is a json structure, you can pass in an array of
 * transformations as the transformation string. ie, <b>[{name: scale, width:
 * 100}, {name: reflection}]</b>. If you pass in a json array as the
 * transformation, then each transformation will be applied to the buffer and
 * then the end result will be cached to the filesystem. <br>
 * <br>
 * 
 * A transform can also accept an 'id' field as well. If an 'id' is present,
 * then that transform will automatically be registered and can then be accessed
 * by that id. ie, if you used the following transform {name:scale, height: 100,
 * id: poster_thumb}, then later you simply reference that transform using
 * {id:poster_thumb}. This is useful if you want to register transforms for
 * specific purposes and then simply refer to them by id later in your stv code. <br>
 * <br>
 * 
 * When creating a transformation, you can pass 'tag'. A tag is simply a
 * "grouping" which ends up being a directory in the image cache. Using a tag
 * can be useful if you want to group together a bunch of transformations for a
 * given purpose. You can then quickly identify them in their cached folder, and
 * easily remove the directory if you want to purge the cache.
 * 
 * @author seans
 */
@API(group = "image")
public class ImageAPI {
	private static final Logger log = Logger.getLogger(ImageAPI.class);

	// Used to parse the image filename from a meta image
	// "MetaImage[C:\\Program Files\\SageTV\\SageTV\\STVs\\SageTV3\\VideoArt.jpg#0 380x380 javaImage=false javaMem=0 jref=0]";
	private static final Pattern sageMetaImagePattern = Pattern.compile("\\[([^\\]]+)#[0-9]+\\s.[0-9]+");

	// another kind of metaimage
	// "MetaImage[MediaFileThumbnail[MediaFile[id=3748404 A[3748411,3748405,\"Futurama - S02E16 - The Deep South\",0@0720.00:00,29] host=mediaserver encodedBy= format=MPEG2-TS 0:29:53 8018 kbps [#0 Video[H.264 1280x720 progressive]#1 Audio[AAC 48000 Hz 2 channels  MAIN idx=1 id=1100 at=ADTS-MPEG2]{MediaType=TV, UserRating=7, EpisodeNumber=16, SeasonNumber=2, EpisodeTitle=The Deep South, MediaProviderDataID=tvdb:73871, MediaTitle=Futurama, OriginalAirDate=2000-04-16}] \\var\\media\\tv\\Futurama-TheDeepSouth-3676462-0.ts, Seg0[Mon 7/20 0:00:06.635-Mon 7/20 0:30:00.000]]]#0 400x588 javaImage=false javaMem=0 jref=0]"
	private static final Pattern sageMetaImagePatternMediaFile = Pattern.compile("id=([0-9]+)\\s");

	/**
	 * Given an array of images, Call Sage's LoadImage on them. This will force
	 * sage to load them into it's cache.
	 * 
	 * This api call has been depecrated, since the UI should try to load their
	 * own images. There is the potential with this API to overload the UI
	 * memory with too many images.
	 * 
	 * @deprecated UI Should try to load thier own images.
	 * 
	 * @param imageArray
	 *            Array or List of images
	 */
	public void LoadImages(Object imageArr) {
	}

	/**
	 * Given an array of images, Scale and Cache them.
	 * 
	 * @param imageArray
	 *            Array or List of images
	 */
	public void CreateScaledImages(Object imageArr, int w, int h) {
		final Object imageArray = imageArr;
		final int width = w;
		final int height = h;
		if (imageArray == null)
			return;
		Thread t = new Thread() {
			@Override
			public/* BuiltAPITool: Ignore Method */void run() {
				log.debug("Begin Scaling Image Array");
				if (imageArray instanceof List) {
					for (Object image : ((List) imageArray)) {
						try {
							CreateScaledImage(image, width, height);
						} catch (Exception e) {
							log.error("Failed to scale Image: " + image);
						}
					}
				} else if (imageArray.getClass().isArray()) {
					for (Object image : ((Object[]) imageArray)) {
						try {
							CreateScaledImage(image, width, height);
						} catch (Exception e) {
							log.error("Failed to scale Image: " + image);
						}
					}
				}
				log.debug("End Scaling Image Array");
			}
		};
		t.start();
	}

	/**
	 * Given an Array/List of images, apply the same transformation to image
	 * element. The operation happens in a background thread.
	 * 
	 * @param imageTag
	 *            common tag/name to apply to each transformation
	 * @param imageArr
	 *            image array
	 * @param jsonTransform
	 *            json transform
	 * @param overwriteImages
	 *            if true, it will overwrite previously cached images
	 */
	public void CreateImages(String imageTag, Object imageArr, String jsonTransform, boolean overwriteImages) {
		jsonTransform = toValidTransform(jsonTransform);

		final Object imageArray = imageArr;
		final String tag = imageTag;
		final String transform = jsonTransform;
		final boolean overwrite = overwriteImages;

		if (imageArray == null)
			return;

		Thread t = new Thread() {
			@Override
			public/* BuiltAPITool: Ignore Method */void run() {
				log.debug("Begin Transforming Image Array Using transform: " + transform);
				if (imageArray instanceof List) {
					for (Object image : ((List) imageArray)) {
						try {
							CreateImage(tag, image, transform, overwrite);
						} catch (Exception e) {
							log.error("Failed to transform Image: " + image + "; transform: " + transform);
						}
					}
				} else if (imageArray.getClass().isArray()) {
					for (Object image : ((Object[]) imageArray)) {
						try {
							CreateImage(tag, image, transform, overwrite);
						} catch (Exception e) {
							log.error("Failed to transform Image: " + image + "; transform: " + transform);
						}
					}
				}
				log.debug("End Transforming Image Array using transform: " + transform);
			}
		};
		t.start();
	}

	/**
	 * Creates a new Image by applying the given transform. The resulting image
	 * will be cached and can get retrieved by using GetImage(id, tag).
	 * 
	 * If overwrite is set to false, then a cached version of the image will be
	 * used. If a cached version does not exist, then it will created and
	 * cached.
	 * 
	 * id and tag are meant to be used together to uniquely identify a cached
	 * instance. ie, the id may be image filename, while the tag may represent
	 * the "type" of transformation that is applied. The image tag is combined
	 * with the image id to make the cached entry unique.
	 * 
	 * The transform is the transform, or series of transforms that you want to
	 * apply to an image using javascript notation (json). ie, {name: scale,
	 * width: 100, height: 200}
	 * 
	 * When a tag is provided, the image cache will put all images from the same
	 * tag in the same directory. This makes for easy housecleaning. For
	 * example, if you use a tag, "poster_small" for all your poster thumbnails,
	 * then you can easily remove all the poster thumbnails, by removing the
	 * poster_small dir from the image cache.
	 * 
	 * @param id
	 *            image id (ie, filename, virtual filename, or some unique id)
	 * @param tag
	 *            imae tag (ie, image group, poster, poster_small, background,
	 *            etc) used with the image id to make the id + tag uqique
	 * @param image
	 *            image to transform
	 * @param transform
	 *            json transform to apply
	 * @param overwrite
	 *            true if you want to overwrite an existing cached instance
	 * @return Sage image
	 */
	public Object CreateImage(String id, String tag, Object image, String transform, boolean overwrite) {
		return cacheImageTransform(id, tag, image, transform, overwrite);
	}

	/**
	 * Same as CreateImage except that the returned image will be a File.
	 * 
	 * @param id
	 *            image id (ie, filename, virtual filename, or some unique id)
	 * @param tag
	 *            imae tag (ie, image group, poster, poster_small, background,
	 *            etc) used with the image id to make the id + tag uqique
	 * @param image
	 *            image to transform
	 * @param transform
	 *            json transform to apply
	 * @param overwrite
	 *            true if you want to overwrite an existing cached instance
	 * @return Transformed Image File, or NULL.
	 */
	public File CreateImageAsFile(String id, String tag, Object image, String transform, boolean overwrite) {
		return createCachedImageTransform(id, tag, image, transform, overwrite);
	}

	/**
	 * Same as CreateImage(id, tag, image, transform, overwrite) except that the
	 * id and tag will automatically be generated from the image and transform.
	 * 
	 * @param image
	 * @param transform
	 * @param overwrite
	 * @return
	 */
	public Object CreateImage(Object image, String transform, boolean overwrite) {
		return cacheImageTransform(imageToId(image), tagFromTransform(transform), image, transform, overwrite);
	}

	/**
	 * Same as CreateImage(id, tag, image, transform, overwrite) except the
	 * image id will be automatically generated from the image object
	 * 
	 * @param tag
	 * @param image
	 * @param transform
	 * @param overwrite
	 * @return
	 */
	public Object CreateImage(String tag, Object image, String transform, boolean overwrite) {
		return cacheImageTransform(imageToId(image), tag, image, transform, overwrite);
	}

	/**
	 * Gets a previously generated/cached transformation for the given image
	 * object with the specified tag.
	 * 
	 * For example, if a bunch of images were previously created using a tag of
	 * "poster_thumb" and a specific transform, then you can recall that cached
	 * image using only the image and it's tag.
	 * 
	 * @param image
	 *            Original image path
	 * @param tag
	 *            tag for the transform that was applied
	 * @return
	 */
	public Object GetImage(Object image, String tag) {
		return GetImage(imageToId(image), tag);
	}

	/**
	 * Gets an image by it's virtual id and tag. For example you could have
	 * created a cached transformation for a given image and given it a unique
	 * id and tag. You can then later recall the cached image using only the id
	 * and tag. This is sometimes useful if the original image is a sage image
	 * or a buffered image. Since there isn't a file path associated with the
	 * image, it could not normally be cached. But if you gave it an id and tag,
	 * then it can be cached and recalled.
	 * 
	 * @param id
	 *            virtual id that was assigned to an image
	 * @param tag
	 *            arbitrary tag/group for the image
	 * @return File object representint the image, or null;
	 */
	public Object GetImage(String id, String tag) {
		File f = ImageUtil.getCachedImageFile(id, tag, ImageUtil.DEFAULT_IMAGE_FORMAT);

		if (f != null && f.exists()) {
			return f;
		}

		log.error("Failed to find a cached image for: " + id + "; tag: " + tag);
		return null;
	}

	/**
	 * Registers a transformation that can be recalled later. The transform must
	 * contains an "id" field.
	 * 
	 * @param transform
	 *            json transformation script
	 */
	public void RegisterImageTransform(String transform) {
		try {
			Phoenix.getInstance().getTransformFactory().createTransform(transform);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Registers a transformation that can be recalled later. The transform is
	 * registered using the id. This API is normally used to register a
	 * composite transformation (ie json array) since there is no way to
	 * actually name the json array using and id field.
	 * 
	 * @param transform
	 *            json transformation script
	 * @param id
	 *            for the transformation
	 */
	public void RegisterImageTransform(String transform, String id) {
		try {
			Phoenix.getInstance().getTransformFactory().registerTransform(id, transform);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Registers a transformed image that can later be recalled using the id and
	 * tag. Register image differs from CreateImage in that RegisterImage may
	 * not apply the transformation immediately. It may queue the transformation
	 * and do it in a background thread.
	 * 
	 * TODO: This is not implemented... so currently RegisterImage behaves like
	 * CreateImage except that it does not return the image.
	 * 
	 * @param id
	 * @param tag
	 * @param image
	 * @param transform
	 * @param overwrite
	 */
	public void RegisterImage(String id, String tag, Object image, String transform, boolean overwrite) {
		log.debug("TODO: Register Images in a new thread");
		CreateImage(id, tag, image, transform, overwrite);
	}

	private Object cacheImageTransform(String id, String tag, Object image, String transform, boolean overwrite) {
		transform = toValidTransform(transform);

		ElapsedTimer et = new ElapsedTimer();
		File f = ImageUtil.getCachedImageFile(id, tag, ImageUtil.DEFAULT_IMAGE_FORMAT);

		if (!overwrite && f != null && f.exists() && !isNewer(image, f)) {
			return f;
		}

		BufferedImage img;
		try {
			img = ImageUtil.getImageAsBufferedImage(image);
		} catch (IOException e1) {
			throw new RuntimeException("Failed to load image " + image, e1);
		}

		File retImg = null;
		BufferedImage imgNew;
		try {
			imgNew = Phoenix.getInstance().getTransformFactory().applyTransform(img, transform);
		} catch (Exception e) {
			throw new RuntimeException("Failed while applying transform: " + transform + " to id: " + id + "; tag:" + tag, e);
		}
		try {
			ImageUtil.writeImage(imgNew, f);
		} catch (IOException e) {
			throw new RuntimeException("Failed to write image: " + f);
		}

		retImg = f;

		if (log.isDebugEnabled()) {
			log.debug(String.format("Time: %s ms; Image: %s; Cached Image: %s; Transform: %s", et.delta(), image,
					f.getAbsolutePath(), transform));
		}

		return retImg;
	}

	private File createCachedImageTransform(String id, String tag, Object image, String transform, boolean overwrite) {
		transform = toValidTransform(transform);

		ElapsedTimer et = new ElapsedTimer();
		File f = ImageUtil.getCachedImageFile(id, tag, ImageUtil.DEFAULT_IMAGE_FORMAT);

		if (!overwrite && f != null && f.exists() && !isNewer(image, f)) {
			return f;
		}

		try {
			BufferedImage img = ImageUtil.getImageAsBufferedImage(image);
			BufferedImage imgNew;
			imgNew = Phoenix.getInstance().getTransformFactory().applyTransform(img, transform);
			ImageUtil.writeImage(imgNew, f);
		} catch (Exception e) {
			throw new RuntimeException("Failed while applying transform: " + transform + " to id: " + id + "; tag:" + tag, e);
		}

		if (log.isDebugEnabled()) {
			log.debug(String.format("Time: %s ms; Image: %s; Cached Image: %s; Transform: %s", et.delta(), image, f, transform));
		}

		return f;
	}

	private boolean isNewer(Object image, File f2) {
		if (image == null || f2 == null)
			return false;
		File f1 = null;
		if (image instanceof File) {
			f1 = ((File) image);
		} else if (image instanceof String) {
			f1 = new File((String) image);
		} else {
			return false;
		}

		return f1.lastModified() > f2.lastModified();
	}

	private String imageToId(Object image) {
		if (image == null) {
			return null;
		}

		String id = null;
		if (image instanceof File) {
			id = ((File) image).getAbsolutePath();
		} else if (image instanceof String) {
			id = String.valueOf(image);
		} else {
			// parse a unique id from the sage meta image object
			Matcher m = sageMetaImagePattern.matcher(String.valueOf(image));
			if (m.find()) {
				id = m.group(1);
				log.debug("Using ImageId: " + id + " for Image: " + image);
			} else {
				m = sageMetaImagePatternMediaFile.matcher(String.valueOf(image));
				if (m.find()) {
					id = "MF-" + m.group(1);
					log.debug("Using ImageId: " + id + " for Image: " + image);
				} else {
					log.warn("Creating Image Hash from toString() for Image: " + image);
					id = DigestUtils.md5Hex(String.valueOf(image));
				}
			}
		}
		return id;
	}

	private String tagFromTransform(String transform) {
		return DigestUtils.md5Hex(transform);
	}

	/**
	 * Returns a scaled image of the source image. If the scaled image cannot be
	 * found/created, then the original is passed back.
	 * 
	 * If a scaled image is not located, then a scaled image is created on the
	 * fly and saved, so that the next call will have access to the scaled
	 * image.
	 * 
	 * @param image
	 *            String file/url, or File, or Sage File or BufferedImage
	 * @param width
	 *            scaled width; -1 means proportional
	 * @param height
	 *            scaled height; -1 means proportional
	 * 
	 * @deprecated use CreateImage
	 * @return
	 */
	public Object CreateScaledImage(Object image, int width, int height) {
		return CreateImage(image, String.format("{name: scale, width: %s, height: %s}", width, height), false);
	}

	/**
	 * Creates a Just Reflection image.
	 * 
	 * @param image
	 * @param reflectionAlphaStart
	 * @param reflectionAlphaEnd
	 * @deprecated use CreateImage
	 * @return
	 */
	public Object CreateJustReflection(Object image, float reflectionAlphaStart, float reflectionAlphaEnd) {
		return CreateImage(image,
				String.format("{name: just_reflection, alphaStart: %s, alphaEnd: %s}", reflectionAlphaStart, reflectionAlphaEnd),
				false);
	}

	/**
	 * Creates a Reflected Image
	 * 
	 * @param image
	 * @param reflectionAlphaStart
	 * @param reflectionAlphaEnd
	 * @deprecated use CreateImage
	 * @return
	 */
	public Object CreateReflection(Object image, float reflectionAlphaStart, float reflectionAlphaEnd) {
		return CreateImage(image,
				String.format("{name: reflection, alphaStart: %s, alphaEnd: %s}", reflectionAlphaStart, reflectionAlphaEnd), false);
	}

	/**
	 * Creates a Reflected Image
	 * 
	 * @param image
	 * @deprecated use CreateImage
	 * @return
	 */
	public Object CreateReflection(Object image) {
		return CreateImage(image, "{name: reflection}", false);
	}

	/**
	 * Create a Perspective Image
	 * 
	 * @param image
	 * @param scalex
	 * @param shifty
	 * @deprecated use CreateImage
	 * @return
	 */
	public Object CreatePerspective(Object image, double scalex, double shifty) {
		return CreateImage(image, String.format("{name: perspective, scalex: %s, shifty: %s}", scalex, shifty), false);
	}

	/**
	 * Creates a rotated image
	 * 
	 * @param image
	 * @param theta
	 * @deprecated use CreateImage
	 * @return
	 */
	public Object CreateRotatedImage(Object image, double theta) {
		return CreateImage(image, String.format("{name: rotate, theta: %s}", theta), false);
	}

	/**
	 * Creates a Gradient Fill Image.
	 * 
	 * @param imageId
	 *            image id or fake image name (ie, "somimage.jpg")
	 * @param imgWidth
	 * @param imgHeight
	 * @param opacityStart
	 * @param opacityEnd
	 * @deprecated use CreateImage
	 * @return
	 */
	public Object CreateGradientFill(String imageId, int imgWidth, int imgHeight, float opacityStart, float opacityEnd) {
		return cacheImageTransform(imageId, String.format("gradient_%s_%s_%s_%s", imgWidth, imgHeight, opacityStart, opacityEnd),
				null, String.format("{name: gradient, width: %s, height: %s, opacityStart: %s, opacityEnd: %s}", imgWidth,
						imgHeight, opacityStart, opacityEnd), false);
	}

	/**
	 * Deletes ALL files from the Image Cache Location
	 */
	public void CleanImageDiskCache() {
		ImageUtil.ClearImageDiskCache();
	}

	/**
	 * Returns of number of bytes in the Image Cache
	 * 
	 * @return
	 */
	public long GetImageCacheDiskSize() {
		return ImageUtil.getCacheSize();
	}

	private static String toValidTransform(String in) {
		// if the transform is null, then use the dummy transform
		if (in == null)
			return "{name: 'dummy'}";
		return in;
	}
}
