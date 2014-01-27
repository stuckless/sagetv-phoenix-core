package sagex.phoenix.configuration;

/**
 * Simple Boolean Options list for "True" and "False"
 * 
 * @author sean
 */
public class BooleanOptionsFactory extends StaticOptionsFactory {
	public static final BooleanOptionsFactory DEFAULT_BOOLEAN_FACTORY = new BooleanOptionsFactory();
	
	public BooleanOptionsFactory() {
		addOption("True", "true");
		addOption("False", "false");
	}

	@Override
	public void addOption(String name, String value) {
		if (getOptions(null).size()>1) throw new UnsupportedOperationException("Can't add any more options to Boolean list");
		super.addOption(name, value);
	}
}
