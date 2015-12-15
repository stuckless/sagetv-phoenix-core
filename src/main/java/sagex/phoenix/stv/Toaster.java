package sagex.phoenix.stv;

import org.apache.log4j.Logger;

import sagex.UIContext;
import sagex.api.Global;
import sagex.api.WidgetAPI;

public class Toaster {
    private static Logger log = Logger.getLogger(Toaster.class);

    private static final String TOAST_REFERENCE = "PHNX-254212";
    private static final String VAR_MSG = "gNativeToastPrompt";
    private static final String VAR_BUTTON_TEXT = "gNativeToastButtonText";
    private static final String VAR_TIMEOUT = "gNativeToastTimeout";

    public static void toast(String message, long wait) {
        toast(null, message, null, wait);
    }

    public static void toast(String ctx, String message, String buttonText, long wait) {
        log.info(message);

        UIContext uictx = null;
        if (ctx == null) {
            uictx = UIContext.getCurrentContext();
        } else {
            uictx = new UIContext(ctx);
        }

        Global.AddGlobalContext(uictx, VAR_MSG, message);
        Global.AddGlobalContext(uictx, VAR_BUTTON_TEXT, buttonText);
        Global.AddGlobalContext(uictx, VAR_TIMEOUT, wait);

        WidgetAPI.ExecuteWidgetChainInCurrentMenuContext(uictx, TOAST_REFERENCE);
    }
}
