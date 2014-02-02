package sagex.phoenix.metadata.persistence;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import sagex.phoenix.fanart.FanartUtil;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.IMetadataPersistence;
import sagex.phoenix.metadata.MetadataException;
import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.util.Hints;
import sagex.phoenix.util.PropertiesUtils;
import sagex.phoenix.util.SortedProperties;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.MediaResourceType;
import sagex.phoenix.vfs.util.PathUtils;

/**
 * Save the metadata to a SageTV .properties file.
 * 
 * @author seans
 * 
 */
public class PropertiesPersistence implements IMetadataPersistence {
	public PropertiesPersistence() {
	}

	@Override
	public void storeMetadata(IMediaFile file, IMetadata md, Hints options) throws MetadataException {
		try {
			File f = PathUtils.getFirstFile(file);
			if (f != null && f.exists()) {
				File propFile = FanartUtil.resolvePropertiesFile(f);

				Map<String, String> map = new HashMap<String, String>();
				IMetadata newMD = MetadataUtil.createMetadata(map);
				MetadataUtil.copyMetadata(md, newMD);

				// add a separate X-Watched flag for later importing state
				map.put(IMetadata.XWatched, String.valueOf(file.isWatched()));
				if (file.isType(MediaResourceType.RECORDING.value())) {
					map.put(IMetadata.XLibraryFile, String.valueOf(file.isLibraryFile()));
				}

				Properties props = new SortedProperties();
				for (Map.Entry<String, String> me : map.entrySet()) {
					if (!StringUtils.isEmpty(me.getValue())) {
						props.setProperty(me.getKey(), me.getValue());
					}
				}

				PropertiesUtils.store(props, propFile, "Created by " + this.getClass().getName());
			}
		} catch (Exception e) {
			throw new MetadataException("Failed to store metadata", file, md, e);
		}
	}
}
