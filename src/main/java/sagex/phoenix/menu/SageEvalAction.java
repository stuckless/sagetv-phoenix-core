package sagex.phoenix.menu;

import sagex.UIContext;
import sagex.api.WidgetAPI;

public class SageEvalAction extends OutputVariableAction {

    public SageEvalAction() {
    }

    @Override
    public boolean invoke() {
        try {
            String action = action().get();
            log.debug("Invoking Sage Expression Action: " + action);

            Object o = WidgetAPI.EvaluateExpression(UIContext.getCurrentContext(), action);

            if (getOutputVariable() != null) {
                updateOutputVariable(o);
            }

            return true;
        } catch (Exception e) {
            log.error("Failed to invoke Expression: " + action());
            return false;
        }
    }

}
