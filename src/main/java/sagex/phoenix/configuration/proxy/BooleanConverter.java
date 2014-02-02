package sagex.phoenix.configuration.proxy;

import org.apache.commons.lang.BooleanUtils;

public class BooleanConverter implements FieldConverter<Boolean> {
	@Override
	public Boolean toType(String in) {
		return BooleanUtils.toBoolean(in);
	}

	@Override
	public String toString(Boolean in) {
		return in == null ? null : String.valueOf(in);
	}
}
