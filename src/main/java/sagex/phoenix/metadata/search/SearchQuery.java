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

    /*
     * we are going to add IMDBID and ID to the search query remove metadata id
     * and series id since they are basically the same provider will no longer
     * have search by id, just a searchQUery, and then the provider can
     * determine how to search, etc.
     */
    public enum Field {
        QUERY, RAW_TITLE, CLEAN_TITLE, SEASON, EPISODE, DISC, EPISODE_TITLE, EPISODE_DATE, YEAR, FILE, URL, PROVIDER, ID, ARTIST, ALBUM
    }

    ;

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
        fields.put(field, value);
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
