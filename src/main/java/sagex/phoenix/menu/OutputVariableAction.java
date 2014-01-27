package sagex.phoenix.menu;

import sagex.UIContext;
import sagex.api.Global;

public abstract class OutputVariableAction extends Action {
    public String outputVariable;

    public OutputVariableAction() {
    }

    public String getOutputVariable() {
        return outputVariable;
    }

    public void setOutputVariable(String outputVariable) {
        this.outputVariable = outputVariable;
    }
    
    public void updateOutputVariable(Object data) {
        updateOutputVariable(getOutputVariable(), data);
    }

    public static void updateOutputVariable(String var, Object data) {
        if (var==null) {
            log.error("updateOuputVariable called with a Null Variable");
            return;
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Setting Output Variable: " + var + "; Data: " + data);
        }

        Global.AddStaticContext(UIContext.getCurrentContext(), var, data);
    }
    
}
