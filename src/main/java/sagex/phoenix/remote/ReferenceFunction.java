package sagex.phoenix.remote;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.math.NumberUtils;

import sagex.phoenix.util.Function;

public class ReferenceFunction implements Function<String, Object> {
	private Pattern pat = Pattern.compile("([a-zA-Z]+)\\[([^\\]]+)\\]");

	public ReferenceFunction() {
	}

	@Override
	public Object apply(String in) {
		Matcher m = pat.matcher(in);
		Object out;
		if (m.find()) {
			out = RemoteContext.get().getReference(m.group(1));
			if (out != null) {
				int n = NumberUtils.toInt(m.group(2), -1);
				if (n >= 0) {
					if (out.getClass().isArray()) {
						out = ((Object[]) out)[n];
					} else if (out instanceof List) {
						out = ((List) out).get(n);
					} else {
						throw new RuntimeException("Not an array or list.  Can't get sub index of " + n);
					}
				} else {
					if (out instanceof Map) {
						out = ((Map) out).get(m.group(2));
					} else {
						throw new RuntimeException("Not a Map.  Can't get sub index of " + m.group(2));
					}
				}
			}
		} else {
			out = RemoteContext.get().getReference(in);
		}
		return out;
	}

}
