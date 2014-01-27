package sagex.phoenix.remote;

import org.apache.commons.lang.BooleanUtils;

import sagex.phoenix.util.Function;

public class BooleanFunction implements Function<String, Boolean> {
	@Override
	public Boolean apply(String in) {
		return BooleanUtils.toBoolean(in);
	}
}
