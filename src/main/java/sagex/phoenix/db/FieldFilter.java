package sagex.phoenix.db;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import sagex.phoenix.metadata.MetadataUtil;
import sagex.phoenix.metadata.proxy.SageProperty;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.filters.IResourceFilter;

public class FieldFilter implements IResourceFilter {
	private enum OP {
		EQUALS, NOT_EQUALS, CONTAINS, GREATER_THAN, LESS_THAN, IS_NOT, IS
	}

	private SageProperty field;
	private OP op;
	private String value;

	public FieldFilter(String field, String op, String value) throws ParseException {
		this.field = MetadataUtil.getSageProperty(field);
		if (field == null) {
			throw new ParseException("Metadata Field is Null");
		}

		if ("=".equals(op)) {
			this.op = OP.EQUALS;
		} else if ("<>".equals(op)) {
			this.op = OP.NOT_EQUALS;
		} else if ("contains".equals(op)) {
			this.op = OP.CONTAINS;
		} else if (">".equals(op)) {
			this.op = OP.GREATER_THAN;
		} else if ("<".equals(op)) {
			this.op = OP.LESS_THAN;
		} else if ("is".equals(op)) {
			this.op = OP.IS;
		} else if ("is not".equals(op)) {
			this.op = OP.IS_NOT;
		} else {
			throw new ParseException("Invalid Operand " + op);
		}
		if ("null".equals(value))
			value = null;
		this.value = value;
	}

	@Override
	public boolean accept(IMediaResource res) {
		if (!(res instanceof IMediaFile))
			return true;

		String fldval = ((IMediaFile) res).getMetadata().get(field);
		if (op == OP.EQUALS) {
			return (fldval != null && fldval.equalsIgnoreCase(value));
		} else if (op == OP.NOT_EQUALS) {
			return !(fldval != null && fldval.equalsIgnoreCase(value));
		} else if (op == OP.LESS_THAN) {
			return NumberUtils.toLong(fldval) < NumberUtils.toLong(value);
		} else if (op == OP.GREATER_THAN) {
			return NumberUtils.toLong(fldval) > NumberUtils.toLong(value);
		} else if (op == OP.CONTAINS) {
			return fldval != null && fldval.toLowerCase().contains(value.toLowerCase());
		} else if (op == OP.IS) {
			return StringUtils.isEmpty(fldval);
		} else if (op == OP.IS_NOT) {
			return !StringUtils.isEmpty(fldval);
		}
		return true;
	}

	public String toString() {
		return field.value() + " " + op + " '" + value + "'";
	}
}
