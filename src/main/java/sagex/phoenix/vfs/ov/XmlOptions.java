package sagex.phoenix.vfs.ov;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.metadata.FieldName;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.util.Loggers;

public class XmlOptions {
    public static final String FEED_URL = "feedurl";
    public static final String OFFLINE = "offline";
    public static final String ITEM_ELEMENT = "item-element";

    private Map<String, ConfigurableOption> options = new HashMap<String, ConfigurableOption>();

    public class XmlMetadata implements Comparable<XmlMetadata> {
        public String MetadataKey;
        public String XmlElement;
        public String XmlAttribute;

        @Override
        public int compareTo(XmlMetadata md) {
            return toString().compareTo(md.toString());
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((MetadataKey == null) ? 0 : MetadataKey.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            XmlMetadata other = (XmlMetadata) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (MetadataKey == null) {
                if (other.MetadataKey != null)
                    return false;
            } else if (!MetadataKey.equals(other.MetadataKey))
                return false;
            return true;
        }

        private XmlOptions getOuterType() {
            return XmlOptions.this;
        }

        @Override
        public String toString() {
            return "XmlMetadata [MetadataKey=" + MetadataKey + ", XmlElement=" + XmlElement + ", XmlAttribute=" + XmlAttribute
                    + "]";
        }
    }

    // map MetadataKey to XmlMetadata structure
    // private Map<String, XmlMetadata> metadataMap = new TreeMap<String,
    // XmlOptions.XmlMetadata>();
    private Set<XmlMetadata> metadataMap = new LinkedHashSet<XmlOptions.XmlMetadata>();

    public XmlOptions() {
        // item options
        addOption(FEED_URL, null, "Feed Url");
        addOption(ITEM_ELEMENT, "item", "Repeatable item element in the Xml");
        addOption(FieldName.MediaType, MediaType.MOVIE.sageValue(), "Media Type for this feed");

        // metadata options
        addMetadataOption(FieldName.MediaTitle, "title", "Movie Title or Music Album");
        addMetadataOption(FieldName.Description, "description", "Description");

        addMetadataOption(FieldName.EpisodeName, null, "Song Name if Music");
        addMetadataOption(FieldName.Album, null, "Albumn Name if Music");
        addMetadataOption(FieldName.AlbumArtist, null, "Artist Name if Muisc");
        addMetadataOption(FieldName.MediaUrl, "link", "Link to downloadable content");
        addMetadataOption(FieldName.Genre, "category", "Genre");
        addMetadataOption(FieldName.OriginalAirDate, "pubDate", "Aired Date");

        // default is offline=true (ie, no real media content, just info)
        addOption(new ConfigurableOption(OFFLINE, "Offline?", "true", DataType.bool));
    }

    public void addOption(String optName, String optValue, String label) {
        addOption(new ConfigurableOption(optName, label, optValue, DataType.string));

        if (optName.endsWith("-element") && !StringUtils.isEmpty(optValue)) {
            String keys[] = optName.split("-");
            String key = keys[0];

            String attr = null;
            String xmlElement = optValue;
            if (xmlElement.contains("@")) {
                String a[] = xmlElement.split("@");
                xmlElement = a[0];
                attr = a[1];
            }

            XmlMetadata md = new XmlMetadata();
            md.MetadataKey = key;
            md.XmlElement = xmlElement;
            md.XmlAttribute = attr;

            if (metadataMap.contains(md)) {
                metadataMap.remove(md);
            }
            metadataMap.add(md);
        }
    }

    public void addMetadataOption(String metadataKey, String xmlElement, String label) {
        addOption(metadataKey + "-element", xmlElement, label);
        addOption(metadataKey + "-regex", null, label);
    }

    public String getFeedUrl() {
        return options.get(FEED_URL).value().getValue();
    }

    public String getItemElement() {
        return options.get(ITEM_ELEMENT).value().getValue();
    }

    public void setItemElement(String el) {
        options.get(ITEM_ELEMENT).value().set(el);
    }

    public String getMediaType() {
        return options.get(FieldName.MediaType).value().getValue();
    }

    public void setFeedUrl(String url) {
        options.get(FEED_URL).value().set(url);
    }

    public Pattern getRegex(String metadataKey) {
        ConfigurableOption co = options.get(metadataKey + "-regex");
        if (co != null && !StringUtils.isEmpty(co.value().getValue())) {
            try {
                Pattern p = Pattern.compile(co.value().getValue(), Pattern.CASE_INSENSITIVE);
                return p;
            } catch (Exception e) {
                Loggers.LOG.warn("Failed to regex parse expression " + co.value().getValue(), e);
            }
        }
        return null;
    }

    public void setRegex(String metadataKey, String regex) {
        addOption(metadataKey + "-regex", regex, null);
    }

    public Set<XmlMetadata> getMetadataKeysForElement(String elName) {
        Set<XmlMetadata> set = new TreeSet<XmlMetadata>();
        for (XmlMetadata md : metadataMap) {
            if (elName.equals(md.XmlElement)) {
                set.add(md);
            }
        }
        return set;
    }

    public Set<XmlMetadata> getXmlMetadata(String mdKey) {
        Set<XmlMetadata> set = new TreeSet<XmlMetadata>();
        for (XmlMetadata md : metadataMap) {
            if (mdKey.equals(md.MetadataKey)) {
                set.add(md);
            }
        }
        return set;
    }

    public void setMediaType(String mt) {
        options.get(FieldName.MediaType).value().set(mt);
    }

    public Set<ConfigurableOption> getOptions() {
        return new TreeSet<ConfigurableOption>(options.values());
    }

    public void addOption(ConfigurableOption configurableOption) {
        ConfigurableOption co = options.get(configurableOption.getName());
        if (co != null) {
            updateOption(configurableOption);
        } else {
            options.put(configurableOption.getName(), configurableOption);
        }
    }

    public void updateOption(ConfigurableOption configurableOption) {
        ConfigurableOption co = options.get(configurableOption.getName());
        if (co == null) {
            addOption(configurableOption);
        } else {
            co.updateFrom(configurableOption);
        }
    }
}
