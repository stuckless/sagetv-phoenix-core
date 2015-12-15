package sagex.phoenix.configuration;

import sagex.UIContext;
import sagex.api.WidgetAPI;
import sagex.phoenix.util.NamedValue;

import java.util.List;

/**
 * Creates an Option Array based on an expression.
 *
 * @author sean
 */
public class SageExpressionOptionFactory implements IOptionFactory {
    String expression = null;

    public SageExpressionOptionFactory(String expression) {
        this.expression = expression;
    }

    @Override
    public List<NamedValue> getOptions(String key) {
        return ConfigUtils.getOptions(WidgetAPI.EvaluateExpression(UIContext.getCurrentContext(), expression));
    }
}
