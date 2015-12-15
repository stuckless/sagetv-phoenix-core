package sagex.phoenix.plugin;

import org.apache.commons.lang.StringUtils;
import sagex.phoenix.util.Loggers;
import sagex.plugin.IPropertyValidator;

import java.util.regex.Pattern;

public class RegexPropertyValidator implements IPropertyValidator {
    public RegexPropertyValidator() {
    }

    @Override
    public void validate(String setting, String value) throws Exception {
        try {
            // it's ok to clear the value
            if (StringUtils.isEmpty(value))
                return;

            // simply calls pattern.compile().. if it's invalid, then pattern
            // will
            // throw an exception
            Pattern.compile(value);
        } catch (Exception e) {
            Loggers.LOG.warn("Regex Validation Failed for setting: " + setting + "; value: " + value, e);
            throw e;
        }
    }
}
