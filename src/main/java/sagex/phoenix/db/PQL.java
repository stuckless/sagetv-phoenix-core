package sagex.phoenix.db;

import java.util.LinkedList;

import sagex.phoenix.util.Pair;
import sagex.phoenix.vfs.filters.IResourceFilter;

/**
 * Phoenix Query Language - A Simple Filter builder
 * 
 * @author sean
 */
public class PQL {
	private LinkedList<IResourceFilter> groups = new LinkedList<IResourceFilter>();
	private IResourceFilter current;

	public PQL() {
	}

	public void and() {
		current = (new AndFilter(current));
	}

	public void or() {
		current = (new ORFilter(current));
	}

	@SuppressWarnings("unchecked")
	public void field(String field, String op, String value) throws ParseException {
		if (current == null) {
			current = (new FieldFilter(field, op, value));
		} else if (current instanceof Pair) {
			((Pair) current).second(new FieldFilter(field, op, value));
		} else {
			throw new ParseException("Can't add field " + field + " to " + current);
		}
	}

	public void begingroup() {
		if (current != null) {
			groups.push(current);
			current = null;
		}
	}

	public void endgroup() throws ParseException {
		if (groups.peek() != null) {
			IResourceFilter filter = groups.pop();
			if (filter instanceof Pair) {
				((Pair) filter).second(current);
				current = filter;
			} else {
				throw new ParseException("Can't add to " + filter);
			}
		}
	}

	public IResourceFilter getFilter() {
		return current;
	}

	public String toString() {
		return "Query: " + current;
	}
}
