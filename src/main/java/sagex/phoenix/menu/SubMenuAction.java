package sagex.phoenix.menu;

import sagex.UIContext;
import sagex.api.WidgetAPI;
import sagex.phoenix.util.var.DynamicVariable;


public class SubMenuAction extends Action {
    private DynamicVariable<String> menuNameVariable = new DynamicVariable<String>(String.class, null);
    
    @Override
    public boolean invoke() {
        String menu = menu().get();
        if (menu==null) {
            log.error("Menu is null for: " + action());
            return false;
        }
        if (menuNameVariable.get()!=null) {
            updateOutputVariable(menuNameVariable().get(), menu);
        } else {
            updateOutputVariable("MenuName", menu);
        }
        
        WidgetAPI.EvaluateExpression(UIContext.getCurrentContext(), "LaunchMenuWidget( GetCurrentMenuWidget() )");
        
        return true;
    }
    
    public void updateOutputVariable(String variable, Object data) {
        if (variable!=null) {
            OutputVariableAction.updateOutputVariable(variable, data);
        }
    }
    
    public DynamicVariable<String> menu() {
        return action();
    }

    public DynamicVariable<String> menuNameVariable() {
        return menuNameVariable;
    }
}
