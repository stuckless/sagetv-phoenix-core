package sagex.phoenix.util;

import org.apache.log4j.Logger;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import sagex.phoenix.ConfigurationErrorEventBus;

/**
 * Base Xml Parser that handles errors, etc. Xml parsers should extend this
 * parser, to get better error logging.
 * 
 * @author seans
 * 
 */
public class BaseBuilder extends DefaultHandler {
	protected Logger log = Logger.getLogger(this.getClass());

	// TODO: Change this when we go to production, or use a configuration
	// property for xml validation
	public static boolean failOnError = false;

	private Locator locator = null;
	protected String data;
	protected String name = null;

	public BaseBuilder() {
	}

	/**
	 * @param name
	 *            - used for logging and error reporting
	 */
	public BaseBuilder(String name) {
		this.name = name;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		data = new String(ch, start, length).trim();
	}

	protected String getData() {
		return data;
	}

	protected void clearData() {
		data = null;
	}

	@Override
	public void error(SAXParseException saxparseexception) throws SAXException {
		if (name == null) {
			name = "unknown xml";
		}

		if (locator != null) {
			log.warn(
					String.format("Xml Parser Failure at line %d, column: %d; for: %s", locator.getLineNumber(),
							locator.getColumnNumber(), name), saxparseexception);
		} else {
			log.warn("Xml Parser Failure; no line information available for: " + name, saxparseexception);
		}

		ConfigurationErrorEventBus.getBus().addError(name, saxparseexception);

		if (failOnError)
			throw saxparseexception;
	}

	@Override
	public void fatalError(SAXParseException saxparseexception) throws SAXException {
		if (name == null) {
			name = "unknown xml";
		}

		if (locator != null) {
			log.warn(
					String.format("Fatal Xml Parser Failure at line %d, column: %d; for: %s", locator.getLineNumber(),
							locator.getColumnNumber(), name), saxparseexception);
		} else {
			log.warn("Fatal Xml Parser Failure; no line information available for: " + name, saxparseexception);
		}

		ConfigurationErrorEventBus.getBus().addError(name, saxparseexception);

		throw saxparseexception;
	}

	@Override
	public void warning(SAXParseException saxparseexception) throws SAXException {
		if (name == null) {
			name = "unknown xml";
		}

		if (locator != null) {
			log.warn(
					String.format("Xml Parser warning at line %d, column: %d; for: %s", locator.getLineNumber(),
							locator.getColumnNumber(), name), saxparseexception);
		} else {
			log.warn("Fatal Xml Parser warning; no line information available for: " + name, saxparseexception);
		}

		ConfigurationErrorEventBus.getBus().addError(name, saxparseexception);

		if (failOnError)
			throw saxparseexception;
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}

	public Locator getLocator() {
		return locator;
	}

	public void error(String msg, Exception ex) throws SAXException {
		SAXParseException e = new SAXParseException(msg, getLocator(), ex);
		error(e);
	}

	public void error(String msg) throws SAXException {
		SAXParseException e = new SAXParseException(msg, getLocator());
		error(e);
	}

	public void fatalError(String msg, Exception ex) throws SAXException {
		fatalError(new SAXParseException(msg, getLocator(), ex));
	}

	public void fatalError(String msg) throws SAXException {
		fatalError(new SAXParseException(msg, getLocator()));
	}

	public void warning(String msg, Exception ex) throws SAXException {
		warning(new SAXParseException(msg, getLocator(), ex));
	}

	public void warning(String msg) throws SAXException {
		warning(new SAXParseException(msg, getLocator()));
	}

	protected Logger getLog() {
		return log;
	}
}
