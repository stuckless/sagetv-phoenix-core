package sagex.phoenix.stv;


import sagex.UIContext;
import sagex.api.Global;
import sagex.api.WidgetAPI;

/**
 * Progress Dialog that can be called from Java but show in the STV
 * 
 * @author sean
 *
 */
public class ProgressDialog {
	public interface CancelHandler {
		public void onCancel();
	}
	
	public static final String DIALOG_WIDGET_SHOW = "PHNX-253443";
	public static final String DIALOG_WIDGET_UPDATE = "PHNX-253463";
	public static final String DIALOG_WIDGET_DISMISS = "PHNX-253456";
	
	private static final String VAR_MSG = "gNativeProgressPrompt";
	private static final String VAR_PERCENT = "gNativeProgressPercent";
	private static final String VAR_POLL_INTERVAL = "gNativeProgressPollInterval";
	private static final String VAR_ON_CANCEL = "gNativeProgressOnCancel";
	private static final String VAR_DIALOG_ID = "gPhoenixDialog";
	
	private int percent=0;
	private String message;
	private UIContext ctx;
	private CancelHandler handler=null;
	private boolean done=false;
	
	
	public ProgressDialog(String uictx, String msg) {
		this.message = msg;
		this.ctx = new UIContext(uictx);
	}

	public int getPercent() {
		return percent;
	}

	public void setPercent(int percent) {
		this.percent = percent;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public CancelHandler getHandler() {
		return handler;
	}

	public void setHandler(CancelHandler handler) {
		this.handler = handler;
	}
	
	public void show() {
		Global.AddGlobalContext(ctx, VAR_MSG, message);
		Global.AddGlobalContext(ctx, VAR_PERCENT, percent);
		if (handler!=null) {
			Global.AddGlobalContext(ctx, VAR_DIALOG_ID, this);
			Global.AddGlobalContext(ctx, VAR_ON_CANCEL, "sagex_phoenix_stv_ProgressDialog_cancel("+VAR_DIALOG_ID+")");
		}
		WidgetAPI.ExecuteWidgetChainInCurrentMenuContext(ctx, DIALOG_WIDGET_SHOW);
	}
	
	public void update(int progress) {
		this.percent=progress;
		update();
	}

	public void update(String msg) {
		this.message=msg;
		update();
	}

	public void update(String msg, int progress) {
		this.message=msg;
		this.percent=progress;
		update();
	}
	
	private void update() {
		Global.AddGlobalContext(ctx, VAR_MSG, message);
		Global.AddGlobalContext(ctx, VAR_PERCENT, percent);
		WidgetAPI.ExecuteWidgetChainInCurrentMenuContext(ctx, DIALOG_WIDGET_UPDATE);
	}
	
	public void close() {
		WidgetAPI.ExecuteWidgetChainInCurrentMenuContext(ctx, DIALOG_WIDGET_DISMISS);
	}
	
	public void cancel() {
		if (handler!=null) {
			handler.onCancel();
		}
		done=true;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

}
