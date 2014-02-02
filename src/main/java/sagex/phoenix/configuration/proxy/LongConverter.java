package sagex.phoenix.configuration.proxy;

import org.apache.commons.lang.math.NumberUtils;

public class LongConverter implements FieldConverter<Long> {
	@Override
	public Long toType(String in) {
		return NumberUtils.toLong(in);
	}

	@Override
	public String toString(Long in) {
		return in == null ? null : String.valueOf(in);
	}
}
