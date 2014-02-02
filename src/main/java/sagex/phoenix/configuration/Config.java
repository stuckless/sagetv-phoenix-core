package sagex.phoenix.configuration;

/**
 * All possible config hints and types. This is mainly used by gwt and other
 * facilities that cannot consume the ConfigType directly
 * 
 * @author sean
 */
public final class Config {
	public static final class Hint {
		public static final String REGEX = "regex";
		public static final String DATE = "date";
		public static final String DATETIME = "datetime";
		public static final String[] values = new String[] { REGEX, DATE, DATETIME };
	}

	public static final class Type {
		public static final String BOOL = "bool";
		public static final String NUMBER = "number";
		public static final String TEXT = "text";
		public static final String CHOICE = "choice";
		public static final String MULTICHOICE = "multichoice";
		public static final String FILE = "file";
		public static final String DIRECTORY = "dir";
		public static final String BUTTON = "button";
		public static final String PASSWORD = "password";
		public static final String[] values = new String[] { BOOL, NUMBER, TEXT, CHOICE, MULTICHOICE, FILE, DIRECTORY, BUTTON,
				PASSWORD };
	}
}
