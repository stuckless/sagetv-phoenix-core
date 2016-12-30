package sagex.phoenix.metadata.search;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.util.HasHints;
import sagex.phoenix.util.Hints;

public class SearchQuery implements Serializable, HasHints {
    private static final long serialVersionUID = 1L;

    public enum Field {
        QUERY, RAW_TITLE, CLEAN_TITLE, SEASON, EPISODE, DISC, EPISODE_TITLE, EPISODE_DATE,
        YEAR, FILE, URL, PROVIDER, ID, ARTIST, ALBUM, AIRING_ID, EPISODE_RANGE_END, IMDBID
    }

    private Map<Field, String> fields = new HashMap<Field, String>();
    private MediaType type = MediaType.MOVIE;
    private Hints hints = new Hints();

    public SearchQuery(Hints hints) {
        this.hints.addHints(hints);
    }

    public SearchQuery(SearchQuery query) {
        this.type = query.getMediaType();
        for (Field f : query.fields.keySet()) {
            fields.put(f, query.get(f));
        }
        hints.addHints(query.getHints());
    }

    public String getEpisodeRangeEnd() {
        return fields.get(Field.EPISODE_RANGE_END);
    }

    public void setEpisodeRangeEnd(String val) {
        set(Field.EPISODE_RANGE_END, val);
    }

    public String getAiringId() {
        return fields.get(Field.AIRING_ID);
    }

    public void setAiringId(String val) {
        set(Field.AIRING_ID, val);
    }

    public String getAlbum() {
        return fields.get(Field.ALBUM);
    }

    public void setAlbum(String val) {
        set(Field.ALBUM, val);
    }

    public String getArtist() {
        return fields.get(Field.ARTIST);
    }

    public void setArtist(String val) {
        set(Field.ARTIST, val);
    }

    public String getId() {
        return fields.get(Field.ID);
    }

    public void setId(String val) {
        set(Field.ID, val);
    }

    public String getProvider() {
        return fields.get(Field.PROVIDER);
    }

    public void setProvider(String val) {
        set(Field.PROVIDER, val);
    }

    public String getUrl() {
        return fields.get(Field.URL);
    }

    public void setUrl(String val) {
        set(Field.URL, val);
    }

    public String getFile() {
        return fields.get(Field.FILE);
    }

    public void setFile(String val) {
        set(Field.FILE, val);
    }

    public String getYear() {
        return fields.get(Field.YEAR);
    }

    public void setYear(String val) {
        set(Field.YEAR, val);
    }

    public String getEpisodeDate() {
        return fields.get(Field.EPISODE_DATE);
    }

    public void setEpisodeDate(String val) {
        set(Field.EPISODE_DATE, val);
    }


    public String getEpisodeTitle() {
        return fields.get(Field.EPISODE_TITLE);
    }

    public void setEpisodeTitle(String val) {
        set(Field.EPISODE_TITLE, val);
    }

    public String getDisc() {
        return fields.get(Field.DISC);
    }

    public void setDisc(String val) {
        set(Field.DISC, val);
    }

    public String getEpisode() {
        return fields.get(Field.EPISODE);
    }

    public void setEpisode(String val) {
        set(Field.EPISODE, val);
    }

    public String getSeason() {
        return fields.get(Field.SEASON);
    }

    public void setSeason(String val) {
        set(Field.SEASON, val);
    }

    public String getCleanTitle() {
        return fields.get(Field.CLEAN_TITLE);
    }

    public void setCleanTitle(String val) {
        set(Field.CLEAN_TITLE, val);
    }

    public String getRawTitle() {
        return fields.get(Field.RAW_TITLE);
    }

    public void setRawTitle(String val) {
        set(Field.RAW_TITLE, val);
    }

    public String getQuery() {
        return fields.get(Field.QUERY);
    }

    public void setQuery(String val) {
        set(Field.QUERY, val);
    }

    public String getIMDBId() {
        return fields.get(Field.IMDBID);
    }

    public void setIMDBID(String id) {
        fields.put(Field.IMDBID, id);
    }

    public Hints getHints() {
        return hints;
    }

    public SearchQuery(MediaType type, String title) {
        this(type, Field.RAW_TITLE, title);
    }

    public SearchQuery(MediaType type, String title, String year) {
        this(type, Field.RAW_TITLE, title);
        set(Field.YEAR, year);
    }

    public SearchQuery(MediaType type, Field field, String value) {
        this.type = type;
        set(field, value);
    }

    public MediaType getMediaType() {
        return type;
    }

    public SearchQuery setMediaType(MediaType type) {
        this.type = type;
        return this;
    }

    public SearchQuery set(Field field, String value) {
        if (value==null||value.trim().length()==0) {
            fields.remove(field);
            return this;
        }
        fields.put(field, value.trim());
        return this;
    }

    public String get(Field field) {
        return fields.get(field);
    }

    @Override
    public String toString() {
        return "SearchQuery [type=" + type + ", fields=" + mapToString(fields) + ", hints=" + hints + "]";
    }

    public static SearchQuery copy(SearchQuery q) {
        return new SearchQuery(q);
    }

    // need this here so that gwt can compile
    private static String mapToString(Map map) {
        if (map == null)
            return "null";
        if (map.size() == 0)
            return "empty";

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Object o : map.entrySet()) {
            Map.Entry me = (Entry) o;
            sb.append(me.getKey()).append(": ").append(me.getValue()).append(",");
        }
        sb.append("}");
        return sb.toString();
    }

}
