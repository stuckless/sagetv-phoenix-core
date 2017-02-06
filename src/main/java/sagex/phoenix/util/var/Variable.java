package sagex.phoenix.util.var;

import org.apache.log4j.Logger;

import sagex.phoenix.configuration.proxy.Converter;
import sagex.phoenix.configuration.proxy.FieldConverter;

/**
 * @author seans
 */
public class Variable<T> {
    protected static final Logger log = Logger.getLogger(Variable.class);

    protected T value;
    protected transient Class<T> type;
    protected transient FieldConverter<T> converter;

    public Variable(Class<T> type) {
        this(null, type);
    }

    public Variable(T value, Class<T> type) {
        this.type = type;
        this.converter = (FieldConverter<T>) Converter.fromType(type);
        if (value != null) {
            // don't bother calling set on null value
            set(value);
        }
    }

    public Class<T> getType() {
        return type;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    public FieldConverter<T> getConverter() {
        return converter;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Variable<" + type + "> [value=" + value + "]";
    }
}
