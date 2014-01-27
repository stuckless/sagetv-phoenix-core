package sagex.phoenix.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

/**
 * Provides basic text replacements in a string using a mask and a
 * {@link IVariableResolver}.
 * 
 * @author skiingwiz
 */
public class TextReplacement {
	/**
	 * Resolves a variable value with a given name. The relation object can be
	 * used by the implementation as the container that holder the varName.
	 * 
	 * @author seans
	 */
	public interface IVariableResolver<T> {
		public String resolve(T relation, String varName);
	}

	/**
	 * Resolves the variable name against the passed map.
	 * 
	 * @author seans
	 */
	public static class MapResolver implements
			IVariableResolver<Map<String, String>> {
		@Override
		public String resolve(Map<String, String> relation, String varName) {
			return relation.get(varName);
		}
	}

	private static final MapResolver mapResolverInstance = new MapResolver();
	private static final Logger log = Logger.getLogger(TextReplacement.class);

	/**
	 * Convenience method that wraps the replacements map into a
	 * {@link MapResolver} and calls replaceVariables.
	 */
	public static String replaceVariables(String string,
			Map<String, String> replacements) {
		return replaceVariables(string, replacements, mapResolverInstance);
	}

	/**
	 * Given a string that may contain variables, replace the variables with
	 * their (optionally formatted) values. The variables may be in one of three
	 * forms:
	 * <ol>
	 * <li>${name}
	 * <li>${name:class:format}
	 * <li>${name:StringFormat} (ie, "%02d", etc)
	 * </ol>
	 * In the first form, the variable named <i>name</i> is replaced with its
	 * value. In the second form, the variable named <i>name</i> is replaced
	 * with its value after that value has been formatted using the subclass of
	 * <code>java.text.Format</code> named in <i>class</i> and the format string
	 * (passed to the formatter's constructor) named in <i>format</i>.
	 * 
	 * As an example of the second form, consider the variable
	 * <i>SeasonNumber</i>. To replace this variable with its integer value,
	 * formatted to always contain 2 digits (e.g. 1 is formatted to 01) use the
	 * variable <code>${SeasonNumber:java.text.DecimalFormat:00}</code>
	 * 
	 * As an example of the second form, consider the variable
	 * <i>SeasonNumber</i>. To replace this variable with its integer value,
	 * formatted to always contain 2 digits (e.g. 1 is formatted to 01) use the
	 * variable <code>${SeasonNumber:%02d}</code>
	 * 
	 * @param string
	 *            The <code>String</code> whose variables are being replaced
	 * @param replacements
	 *            A
	 *            <code>Map<code> whose keys are possible variable names and whose values are the
	 * <code>String</code>s with which the variable should be replaced
	 * @return The given <code>String</code> with all possible variable
	 *         substitutions made
	 * @throws Exception
	 */
	public static <T> String replaceVariables(String string, T relation,
			IVariableResolver<T> replacements) {
		return replaceVariables(string, relation, replacements, 0);
	}

	/**
	 * internal replacement handler
	 * 
	 * @param string
	 * @param relation
	 * @param replacements
	 * @param sanity
	 * @return
	 */
	private static <T> String replaceVariables(String string, T relation,
			IVariableResolver<T> replacements, int sanity) {
		if (sanity > 20)
			return string;

		Pattern pattern = Pattern.compile("(\\$\\{([^\\}]+)\\})");
		Matcher m = pattern.matcher(string);
		if (m.find()) {
			String all = m.group(1);
			String keys[] = m.group(2).split(":");
			String value = null;
			value = replacements.resolve(relation, keys[0]);
			if (value == null)
				value = "";

			try {
				if (keys.length == 2) {
					Object v = value;
					if (keys[1].endsWith("d")) {
						v = NumberUtils.toInt(value);
					}
					value = String.format(keys[1], v);
				} else if (keys.length == 3) {
					String formatClassName = keys[1];
					String format = keys[2];

					Format formatter = getFormat(formatClassName, format);
					if (formatter != null) {
						try {
							if (formatter instanceof DateFormat) {
								// convert long number to date object
								value = formatter.format(Long.parseLong(value));
							} else {
								Object o = formatter.parseObject(value);
								value = formatter.format(o);
							}
						} catch (ParseException pe) {
							log.warn(
									"Could not format value because it was not parsable by the configured formatter."
											+ " Value: "
											+ value
											+ " Formatter: "
											+ formatClassName
											+ " Format: " + format, pe);
						}
					}
				}
			} catch (Exception e) {
				log.warn("Could not replace: " + all + " with " + value, e);
			}
			string = string.replace(all, value);

			// keep replacing
			return replaceVariables(string, relation, replacements, ++sanity);
		}
		return string;
	}

	/**
	 * Given a class name and a format, create and instance of the class, which
	 * must be a concrete subclass of <code>java.text.Format</code>, and
	 * initialize it with the format.
	 * 
	 * @param formatClassName
	 *            The name of a class, a subclass of
	 *            <code>java.text.Format</code>
	 * @param format
	 *            The format to be used
	 * @return An initialized subclass of <code>java.text.Format</code>
	 */
	private static Format getFormat(String formatClassName, String format) {
		Format formatter = null;

		try {
			Class<?> c = Class.forName(formatClassName);
			if (Format.class.isAssignableFrom(c)) {
				Class<? extends Format> formatClass = c
						.asSubclass(Format.class);
				Constructor<? extends Format> constructor = formatClass
						.getConstructor(String.class);
				formatter = constructor.newInstance(format);
			}
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
			log.error(
					"Could not instantiate Format Object because the class name is invalid: "
							+ formatClassName, cnfe);
		} catch (NoSuchMethodException nsme) {
			nsme.printStackTrace();
			log.error(
					"Invalid Format Class.  Instance of java.text.Format doesn't have a (String) constructor.",
					nsme);
		} catch (InvocationTargetException ite) {
			ite.printStackTrace();

			log.error("Could not instantiate format Object.", ite);
		} catch (IllegalAccessException iae) {
			iae.printStackTrace();

			log.error("Could not instantiate format Object.", iae);
		} catch (InstantiationException ie) {
			ie.printStackTrace();

			log.error("Could not instantiate format Object.", ie);
		}

		return formatter;
	}
}
