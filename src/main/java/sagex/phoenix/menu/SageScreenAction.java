package sagex.phoenix.menu;

import sagex.UIContext;
import sagex.api.WidgetAPI;
import sagex.phoenix.util.var.DynamicVariable;

public class SageScreenAction extends Action {
    public SageScreenAction() {
    }

    @Override
    public boolean invoke() {
        return launchScreen(screen().get());
    }
    
    public static boolean launchScreen(String screen) {
        try {
            UIContext ctx = UIContext.getCurrentContext();
            String thisName = screen;
            log.debug("Launch Sage Screen Action: " + screen);
            if (thisName==null) throw new Exception("Missing Screen Name!");
            String menuName=null;
            Object menuWidget = null;
            Object menus[] = WidgetAPI.GetWidgetsByType(ctx, "Menu");
            for (Object m : menus) {
                menuName = WidgetAPI.GetWidgetName(ctx, m);
                if (thisName.equals(menuName)) {
                    menuWidget = m;
                    break;
                }
            }
            
            if (menuWidget==null) {
                throw new Exception("Unable to Find Menu: " + thisName);
            }
            
            WidgetAPI.LaunchMenuWidget(ctx, menuWidget);
            return true;
        } catch (Exception e) {
            log.error("Failed to execute Sage Screen: " + screen, e);
            return false;
        }
    }
   

    public DynamicVariable<String> screen() {
        return action();
    }
}
