package test.junit.cm;

import java.util.ArrayList;
import java.util.List;

import sagex.phoenix.configuration.IOptionFactory;
import sagex.phoenix.util.NamedValue;

public class MyOptionFactory implements IOptionFactory {
	private ArrayList<NamedValue> list = new ArrayList<NamedValue>();

	public MyOptionFactory() {
		list.add(new NamedValue("One", "1"));
	}

	@Override
	public List<NamedValue> getOptions(String key) {
		return list;
	}
}
