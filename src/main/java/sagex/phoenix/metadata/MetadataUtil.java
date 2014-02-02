package sagex.phoenix.metadata;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import sagex.api.MediaFileAPI;
import sagex.api.ShowAPI;
import sagex.phoenix.Phoenix;
import sagex.phoenix.metadata.proxy.MetadataProxy;
import sagex.phoenix.metadata.proxy.SageMediaFileMetadataProxy;
import sagex.phoenix.metadata.proxy.SageProperty;
import sagex.phoenix.util.DateUtils;
import sagex.phoenix.util.Loggers;
import sagex.phoenix.util.Pair;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.util.ILog;
import sagex.util.LogProvider;

/**
 * SageTV Metadata Class. This class should always contain the complete list of
 * metadata fields that work with {@link MediaFileAPI}.GetMediaFileMetadata()
 * 
 * Other classes can call addProperties() to register thier own custom metadata
 * fields
 * 
 * @author seans
 * 
 */
public class MetadataUtil {
	private static final ILog log = LogProvider.getLogger(MetadataUtil.class);

	private static Map<String, SageProperty> SageProperties = null;

	/**
	 * Return the SageTV Property Keys that are Defined in the given interface
	 * class
	 * 
	 * @return String[] of Sage Keys
	 */
	public static <T> String[] getPropertyKeys(Class<T> klas) {
		Set<String> keys = new TreeSet<String>();

		Method[] methods = klas.getMethods();
		if (methods != null) {
			for (Method m : methods) {
				SageProperty p = m.getAnnotation(SageProperty.class);
				if (p != null) {
					keys.add(p.value());
				}
			}
		}

		return keys.toArray(new String[] {});
	}

	/**
	 * Returns a Map<String, SageProperty> of all the defined properties for the
	 * given interface. This can be used to map a known "String" property to the
	 * it's SageProperty instance
	 * 
	 * @return String[] of Sage Keys
	 */
	public static <T> Map<String, SageProperty> getProperties(Class<T> klas) {
		Map<String, SageProperty> props = new HashMap<String, SageProperty>();

		Method[] methods = klas.getMethods();
		if (methods != null) {
			for (Method m : methods) {
				SageProperty p = m.getAnnotation(SageProperty.class);
				if (p != null) {
					props.put(p.value(), p);
				}
			}
		}

		return props;
	}

	/**
	 * Returns a Map of Sage Properties for known {@link IMetadata} properties
	 */
	public static Map<String, Pair<Method, Method>> getPropertyKeys() {
		Map<String, Pair<Method, Method>> all = new TreeMap<String, Pair<Method, Method>>();

		Method[] methods = IMetadata.class.getMethods();
		if (methods != null) {
			for (Method m : methods) {
				SageProperty p = m.getAnnotation(SageProperty.class);
				if (p != null) {
					Pair<Method, Method> pair = all.get(p.value());
					if (pair == null) {
						pair = new Pair<Method, Method>(null, null);
						all.put(p.value(), pair);
					}
					if (m.getName().startsWith("set")) {
						pair.first(m);
					} else if (m.getName().startsWith("get") || m.getName().startsWith("is")) {
						pair.second(m);
					} else {
						log.debug("Skipping Method: " + m.getName());
					}
				}
			}
		}

		return all;
	}

	/**
	 * creates a metadata object that is backed by the Sage MediaFile. All
	 * sets/gets on the metadata are immediately reflected on the Sage
	 * MediaFile.
	 * 
	 * @param sageMediaFile
	 * @return
	 */
	public static IMetadata createMetadata(Object sageMediaFile) {
		return SageMediaFileMetadataProxy.newInstance(sageMediaFile);
	}

	/**
	 * Creates a IMetadata object that is backed by a Map. It is not persistent
	 * in any way.
	 */
	public static IMetadata createMetadata() {
		return MetadataProxy.newInstance();
	}

	/**
	 * Creates a metadata object that is backed by the given map. All sets/gets
	 * to the metadata are immediately reflected in the map.
	 * 
	 * @param map
	 * @return
	 */
	public static IMetadata createMetadata(Map<String, String> map) {
		return MetadataProxy.newInstance(map);
	}

	/**
	 * Copy all metadata from the source object to the destination object,
	 * overwriting ALL metadata.
	 * 
	 * @param src
	 * @param dest
	 * @throws Exception
	 */
	public static void copyMetadata(IMetadata src, IMetadata dest) throws Exception {
		for (Pair<Method, Method> p : MetadataUtil.getPropertyKeys().values()) {
			if (p.first() != null && p.second() != null) {
				if (p.first().getParameterTypes().length == 0) {
					log.debug("copyFrom(): No Valid Parameter Type for setter on " + p.first());
				} else {
					try {
						p.first().invoke(dest, p.second().invoke(src, (Object[]) null));
					} catch (Exception e) {
						log.warn("Metdata Copy Failed for Setter: " + p.first() + "; Getter: " + p.second(), e);
					}
				}
			} else {
				if (p.first() == null && p.second() != null && p.second().getReturnType().isAssignableFrom(List.class)) {
					log.debug("copyFrom(): Cloning List; " + p.second());
					try {
						List l1 = (List) p.second().invoke(dest, (Object[]) null);
						List l2 = (List) p.second().invoke(src, (Object[]) null);
						l1.clear();
						l1.addAll(l2);
					} catch (Exception e) {
						log.warn("Metdata Copy Failed for List: " + p.second(), e);
					}
				} else {
					log.debug("Skipping Setter:Getter; " + p.first() + ":" + p.second());
				}
			}
		}
	}

	/**
	 * Copy all metadata from the source object to the destination object, and
	 * only overwrite the destination if the source has been modified. This is
	 * useful when you modify some metadata from a source and you want to ONLY
	 * copy those modified fields to another metadata object. In addition to
	 * setting only modified values, it will NOT call set on the destination if
	 * the src and destination values are the same.
	 * 
	 * @param src
	 * @param dest
	 * @throws Exception
	 */
	public static void copyModifiedMetadata(IMetadata src, IMetadata dest) throws Exception {
		for (Map.Entry<String, Pair<Method, Method>> me : MetadataUtil.getPropertyKeys().entrySet()) {
			Pair<Method, Method> p = me.getValue();
			if (p.first() != null && p.second() != null) {
				if (p.first().getParameterTypes().length == 0) {
					log.debug("copyFrom(): No Valid Parameter Type for setter on " + p.first());
				} else {
					if (src.isSet(getSageProperty(me.getKey()))) {
						Object srcO = p.second().invoke(src, (Object[]) null);
						Object dstO = p.second().invoke(dest, (Object[]) null);

						if (srcO != null && dstO != null) {
							if (srcO.equals(dstO)) {
								log.debug("Skipping Field: " + me.getKey() + "; Same Value: " + srcO);
								continue;
							}
						}

						log.debug("Setting Modified Value for " + me.getKey() + ": " + srcO);
						p.first().invoke(dest, srcO);
					} else {
						log.debug("Skipping Field: " + me.getKey() + "; Not Set");
					}
				}
			} else {
				if (p.first() == null && p.second() != null && p.second().getReturnType().isAssignableFrom(List.class)) {
					if (src.isSet(getSageProperty(me.getKey()))) {
						log.debug("Setting List: " + me.getKey());
						List l1 = (List) p.second().invoke(dest, (Object[]) null);
						List l2 = (List) p.second().invoke(src, (Object[]) null);
						l1.clear();
						l1.addAll(l2);
					} else {
						log.debug("Skipping List Field: " + me.getKey() + "; Not Set");
					}
				} else {
					log.debug("Skipping Field; " + me.getKey());
				}
			}
		}
	}

	public static SageProperty getSageProperty(String key) {
		if (SageProperties == null) {
			SageProperties = getProperties(IMetadata.class);
		}
		return SageProperties.get(key);
	}

	/**
	 * Copies all metadata from the source to the destination, but it will not
	 * overwrite the destination if the source has null or 0 values.
	 * 
	 * @param src
	 * @param dest
	 * @throws Exception
	 */
	public static void mergeMetadata(IMetadata src, IMetadata dest) throws Exception {
		for (Pair<Method, Method> p : MetadataUtil.getPropertyKeys().values()) {
			if (p.first() != null && p.second() != null) {
				if (p.first().getParameterTypes().length == 0) {
					log.debug("copyFrom(): No Valid Parameter Type for setter on " + p.first());
				} else {
					Object osrc = p.second().invoke(src, (Object[]) null);
					Object odst = p.second().invoke(dest, (Object[]) null);
					if (osrc == null)
						continue;
					// TODO: handle ints, floats, etc
					p.first().invoke(dest, osrc);
				}
			} else {
				if (p.first() == null && p.second() != null && p.second().getReturnType().isAssignableFrom(List.class)) {
					log.debug("copyFrom(): Cloning List; " + p.second());
					List l1 = (List) p.second().invoke(dest, (Object[]) null);
					List l2 = (List) p.second().invoke(src, (Object[]) null);

					// don't update if the src is empty
					if (l2.size() == 0)
						continue;
					l1.clear();
					l1.addAll(l2);
				} else {
					log.debug("Skipping Setter:Getter; " + p.first() + ":" + p.second());
				}
			}
		}
	}

	/**
	 * Fills the metadata in Dest where the Dest is null and the source is not
	 * null. This has the effect of updating dest from the the src, ONLY when
	 * the dest has an empty metadata field. It does not copy from src to dest
	 * if dest has a value already.
	 * 
	 * @param src
	 * @param dest
	 * @throws Exception
	 */
	public static void fillMetadata(IMetadata src, IMetadata dest) throws Exception {
		for (Pair<Method, Method> p : MetadataUtil.getPropertyKeys().values()) {
			if (p.first() != null && p.second() != null) {
				if (p.first().getParameterTypes().length == 0) {
					log.debug("copyFrom(): No Valid Parameter Type for setter on " + p.first());
				} else {
					Object osrc = p.second().invoke(src, (Object[]) null);
					Object odst = p.second().invoke(dest, (Object[]) null);
					// skip is src is null OR dest is not null
					if (osrc == null)
						continue;

					if (odst instanceof String && !StringUtils.isEmpty((String) odst)) {
						continue;
					}

					if (odst instanceof Number && ((Number) odst).floatValue() != 0) {
						continue;
					}

					p.first().invoke(dest, osrc);
				}
			} else {
				if (p.first() == null && p.second() != null && p.second().getReturnType().isAssignableFrom(List.class)) {
					log.debug("copyFrom(): Cloning List; " + p.second());
					List l1 = (List) p.second().invoke(dest, (Object[]) null);
					List l2 = (List) p.second().invoke(src, (Object[]) null);

					// don't update if the src is empty or
					// we have data in the destination list
					if (l2.size() == 0 || l1.size() > 0)
						continue;
					l1.clear();
					l1.addAll(l2);
				} else {
					log.debug("Skipping Setter:Getter; " + p.first() + ":" + p.second());
				}
			}
		}
	}

	public static String getRelativePathWithTitle(IMediaFile file, IMetadata md) {
		String title = null;
		if (!file.isType(MediaResourceType.RECORDING.value())) {
			Object obj = file.getMediaObject();
			String path = MediaFileAPI.GetMediaFileRelativePath(obj);
			if (!StringUtils.isEmpty(path) && !StringUtils.isEmpty(md.getMediaTitle())) {
				File f = new File(path);
				File dir = new File(f.getParentFile(), md.getMediaTitle());
				title = dir.getPath();
			}
		}

		if (title == null) {
			title = md.getMediaTitle();
		}

		return title;
	}

	/**
	 * Returns true if this media was imported into the recordings, as opposed
	 * to a native sagetv recording.
	 * 
	 * @param file
	 * @return
	 */
	public static boolean isImportedRecording(IMediaFile file) {
		if (file.isType(MediaResourceType.RECORDING.value())) {
			String eid = file.getMetadata().getExternalID();
			if (eid != null && eid.length() > 5) {
				return eid.charAt(2) == 'm' && eid.charAt(3) == 't';
			}
		}
		return false;
	}

	/**
	 * Returns true if the resource is a recorded movie, or future recorded
	 * movie
	 * 
	 * @param resource
	 * @return
	 */
	public static boolean isRecordedMovie(IMediaFile resource) {
		try {
			if (resource.isType(MediaResourceType.EPG_AIRING.value()) || resource.isType(MediaResourceType.RECORDING.value())) {
				Object sagemf = phoenix.media.GetSageMediaFile(resource);
				if (sagemf != null) {
					// Now check the alternate category
					String altCat = ShowAPI.GetShowCategory(sagemf);
					log.debug("checking category for movie " + altCat);
					if (!StringUtils.isEmpty(altCat)) {
						if (altCat.equals("Movie") || altCat.equals(phoenix.config.GetProperty("alternate_movie_category"))) {
							log.debug("this is a recorded movie " + resource);
							return true;
						}
					}
					String eid = resource.getMetadata().getExternalID();
					log.debug("testing show id " + eid);
					if (eid != null) {
						return eid.startsWith("MV");
					}

					log.debug("this is a NOT recorded movie " + resource);
				} else {
					log.debug("failed to get sage mediafile for " + resource);
				}
			} else {
				log.debug("resource is not an airing or recording " + resource);
			}
		} catch (Exception e) {
			Loggers.LOG.warn("failed while attempting to set the media type based on alternate_movie_category", e);
		}

		return false;
	}

	/**
	 * Batch update the Metadata using the Map of properties and values.
	 * 
	 * NOTE: If you ware updating the Genre fields you can use +Genre or -Genre
	 * to add/remove a genre instead of replacing the entire genre field.
	 * 
	 * @param props
	 * @param md
	 */
	public static void batchUpdate(Map<String, String> props, IMetadata md) {
		for (Map.Entry<String, String> me : props.entrySet()) {
			if (FieldName.Genre.equals(me.getKey())) {
				if (StringUtils.isEmpty(me.getValue())) {
					setProperty(md, me.getKey(), me.getValue());
				} else if (me.getValue().startsWith("+")) {
					String g = me.getValue().substring(1);
					if (!md.getGenres().contains(g)) {
						md.getGenres().add(g);
					}
				} else if (me.getValue().startsWith("-")) {
					String g = me.getValue().substring(1);
					md.getGenres().remove(g);
				} else {
					setProperty(md, me.getKey(), me.getValue());
				}
			} else {
				setProperty(md, me.getKey(), me.getValue());
			}
		}
	}

	/**
	 * Not a very efficient way to update metadata, but can be used in cases
	 * where you can't use the typesafe methods of {@link IMetadata}. It is
	 * recommended to use this method any time you will use {@link IMetadata}
	 * .set() since this method will correct some values.
	 * 
	 * @param md
	 *            {@link IMetadata} instance
	 * @param key
	 *            Metadata Key
	 * @param value
	 *            Metadata Value
	 */
	public static void setProperty(IMetadata md, String key, String value) {
		SageProperty p = getSageProperty(key);
		if (p == null) {
			log.warn("setProperty(): Invalid Property: " + key);
			return;
		}

		if (StringUtils.isEmpty(value)) {
			md.set(p, value);
			return;
		}

		if ("OriginalAirDate".equals(key)) {
			long l = NumberUtils.toLong(value, 0);
			if (!(l > 0)) {
				Date d = DateUtils.parseDate(value);
				if (d != null) {
					l = d.getTime();
				}
			}
			if (l > 0) {
				value = String.valueOf(l);
			} else {
				value = "";
			}
		} else if ("RunningTime".equals(key)) {
			long l = DateUtils.parseRuntimeInMinutes(value);
			if (l > 0) {
				value = String.valueOf(l);
			} else {
				value = "";
			}
		}

		md.set(p, value);
	}

	/**
	 * Returns a metadata field from the {@link IMetadata} object. It is
	 * recommended to use this method when calling {@link IMetadata}.get() since
	 * this method will correct some values.
	 * 
	 * @param md
	 * @param key
	 * @return
	 */
	public static String getProperty(IMetadata md, String key) {
		SageProperty p = getSageProperty(key);
		if (p == null) {
			log.warn("getProperty(): Invalid Property: " + key);
			return null;
		}
		String value = md.get(p);

		if ("OriginalAirDate".equals(key)) {
			long l = NumberUtils.toLong(value);
			if (l > 0) {
				value = DateUtils.formatDateTime(l);
			} else {
				value = "";
			}
		} else if ("RunningTime".equals(key)) {
			long l = NumberUtils.toLong(value);
			if (l > 0) {
				value = DateUtils.formatTimeInMinutes(l);
			} else {
				value = "";
			}
		}

		return value;
	}

	/**
	 * Given a non-empty rating, it will try to convert it into a valid sagetv
	 * rating.
	 * 
	 * @param type
	 * @param rating
	 * @return
	 */
	public static String fixContentRating(MediaType type, String rating) {
		if (!StringUtils.isEmpty(rating)) {
			return Phoenix.getInstance().getRatingsManager().getRating(type, rating);
		}
		return null;
	}
}
