package sagex.phoenix.event;

import java.util.Map;

import sage.SageTVEventListener;
import sage.SageTVPluginRegistry;
import sagex.phoenix.util.Loggers;

/**
 * EventBus backed by the SageTV event system
 * 
 * @author sean
 */
public class SageEventBus implements IEventBus {
	private SageTVPluginRegistry pluginRegistry = null;

	public SageEventBus(SageTVPluginRegistry pluginRegistry) {
		this.pluginRegistry = pluginRegistry;
	}

	@Override
	public void addListener(String type, SageTVEventListener l) {
		Loggers.LOG.info("Adding Event Listener for " + type + " routing to " + l);
		pluginRegistry.eventSubscribe(l, type);
	}

	@Override
	public void fireEvent(String event, Map eventArgs, boolean wait) {
		pluginRegistry.postEvent(event, eventArgs, wait);
	}

	@Override
	public void removeListener(String type, SageTVEventListener l) {
		pluginRegistry.eventUnsubscribe(l, type);
	}
}
