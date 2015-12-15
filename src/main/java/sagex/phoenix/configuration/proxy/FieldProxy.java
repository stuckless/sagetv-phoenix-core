package sagex.phoenix.configuration.proxy;

import org.apache.log4j.Logger;

import sagex.phoenix.Phoenix;

public class FieldProxy<T> {
    private static final Logger log = Logger.getLogger(FieldProxy.class);
    private T defaultValue = null;
    private String defaultValueAsString;
    private String key = null;
    private FieldConverter<T> converter;

    public FieldProxy(T defValue) {
        this(defValue, (FieldConverter<T>) Converter.fromObject(defValue));
    }

    public FieldProxy(T defValue, FieldConverter<T> converter) {
        this.defaultValue = defValue;
        this.converter = converter;
        this.defaultValueAsString = converter.toString(defValue);
    }

    private String getProperty(String xkey, String defValue) {
        if (xkey == null) {
            log.warn("Key cannot be null for FieldProxy! This is a misconfiguration or bug.", new Exception(
                    "FieldProxy was not initialized with a Key!"));
            return defValue;
        }
        return Phoenix.getInstance().getConfigurationManager().getProperty(xkey, defValue);
    }

    public T get() {
        return converter.toType(getProperty(key, defaultValueAsString));
    }

    public void set(T value) {
        Phoenix.getInstance().getConfigurationManager().setProperty(key, converter.toString(value));
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDefaultValueAsString() {
        return converter.toString(defaultValue);
    }

    public FieldConverter<T> getConverter() {
        return converter;
    }
}
