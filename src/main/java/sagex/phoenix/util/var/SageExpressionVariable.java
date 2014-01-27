package sagex.phoenix.util.var;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import sagex.UIContext;
import sagex.api.WidgetAPI;


public class SageExpressionVariable<T> extends Variable<T> {
    private static final Pattern exprPattern = Pattern.compile("\\$\\{([^}]+)}");

    private String expression = null;
    private T defValue;
    
    public SageExpressionVariable(String expression, T defValue, Class<T> type) {
    	super(type);
        this.expression=expression;
        this.defValue = defValue;
    }

    @Override
    public T get() {
        if (expression==null) return defValue;
        
        try {
        	String expr = expression;
        	Matcher m = exprPattern.matcher(expression);
        	if (m.find()) {
        		expr = m.group(1);
        	}
        	
        	if (StringUtils.isEmpty(expr)) {
        		log.warn("Missing Expression for " + expression);
            	if (defValue!=null) return defValue;
            	return getConverter().toType(null);
        	}
        	
            Object o = WidgetAPI.EvaluateExpression(UIContext.getCurrentContext(), expr);
            if (o instanceof String) {
            	return getConverter().toType((String) o);
            } 
            
            if (o==null) {
            	if (defValue!=null) return defValue;
            	return getConverter().toType(null);
            }
            // if not a string, then assume the type we are looking for
            return (T)o;
        } catch (Throwable e) {
        	e.printStackTrace();
            log.warn("Failed to execute Sage Expression: " + expression + "; Using default Value: " + defValue, e);
            return defValue;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SageExpressionVariable [defValue=" + defValue + ", expression=" + expression + "]";
    }
}
