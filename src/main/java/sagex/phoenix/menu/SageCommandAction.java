package sagex.phoenix.menu;

import sagex.UIContext;
import sagex.api.Global;

public class SageCommandAction extends Action {

	public SageCommandAction() {
	}

	@Override
	public boolean invoke() {
		log.debug("Invoking Sage Command: " + action().get());
		try {
			Global.SageCommand(UIContext.getCurrentContext(), action().get());
		} catch (Exception e) {
			log.error("Failed to execute SageCommand: " + action().get());
			return false;
		}
		return true;
	}

}
