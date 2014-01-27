package sagex.phoenix.configuration.proxy;

import org.apache.commons.lang.math.NumberUtils;

public class IntConverter implements FieldConverter<Integer> {
	@Override
	public Integer toType(String in) {
		return NumberUtils.toInt(in);
	}

	@Override
	public String toString(Integer in) {
		return in==null?null:String.valueOf(in);
	}
}
