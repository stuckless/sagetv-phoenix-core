package sagex.phoenix.configuration.proxy;


public class Converter {
	public static final BooleanConverter BOOLEAN = new BooleanConverter(); 
	public static final IntConverter INT = new IntConverter(); 
	public static final TextConverter TEXT = new TextConverter();
	public static final LongConverter LONG = new LongConverter(); 
	public static final FloatConverter FLOAT = new FloatConverter(); 
	public static final DoubleConverter DOUBLE = new DoubleConverter();
	public static final ObjectConverter OBJECT = new ObjectConverter();
	
	public static FieldConverter<?> fromObject(Object in) {
		if (in==null) throw new RuntimeException("Can't determine Field Converter for null object");
		return fromType(in.getClass());
	}

	public static FieldConverter<?> fromType(Class<?> type) {
		if (type==null) throw new RuntimeException("Can't determine Field Converter for null object");
		if (type == int.class || type== Integer.class) {
			return INT;
		} else if (type == String.class) {
			return TEXT;
		} else if (type == boolean.class || type== Boolean.class) {
			return BOOLEAN;
		} else if (type == long.class || type== Long.class) {
			return LONG;
		} else if (type == double.class || type== Double.class) {
			return DOUBLE;
		} else if (type == float.class || type == Float.class) {
			return FLOAT;
		} else if (type == Object.class) {
			return OBJECT;
		} else {
			throw new RuntimeException("Can't Determine FieldConverter for Object Type: " + type.getName());
		}
	}
}
