package sagex.phoenix.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Hints are named data elements that can be
 * passed around that may understood by the receiver.
 * 
 * for example, A Search() method may take Hints to affect the search.  Or an Update() method
 * may accept Hints to affect how data is Updated()
 * 
 * @author sean
 */
public class Hints implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Map<String, String> hints = new HashMap<String, String>();
	
	public Hints() {
	}

	/**
	 * Sets the Hints based on an Array of elements where every other element is a key/value
	 * combination.   For example, "SearchType", "TV", "Title", "Temors"
	 * @param hints
	 */
	public Hints(String... hints) {
		for (int i=0;i<hints.length;i+=2) {
			this.hints.put(hints[i], hints[i+1]);
		}
	}
	
	public Hints(Map<String,String> hints) {
		addHints(hints);
	}

	public Hints(Hints hints) {
		addHints(hints);
	}

	public void setHint(String key, String value) {
		hints.put(key, value);
	}

	public String getHint(String key) {
		return hints.get(key);
	}

	public void setBooleanHint(String key, boolean value) {
		setHint(key,String.valueOf(value));
	}

	public boolean getBooleanValue(String key, boolean def) {
		String b = getHint(key);
		if (b==null) return def;
		return ("true".equalsIgnoreCase(b));
	}

	public void addHints(Map<String, String> hints) {
		this.hints.putAll(hints);
	}

	public void addHints(Hints hints) {
		this.hints.putAll(hints.getHints());
	}

	public Map<String, String> getHints() {
		return hints;
	}

	@Override
	public String toString() {
		return "Hints [hints=" + mapToString(hints) + "]";
	}
	
	// need this here so that gwt can compile
    private static String mapToString(Map map) {
        if (map == null) return "null";
        if (map.size()==0) return "empty";
        
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
