package sagex.phoenix.vfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sagex.phoenix.factory.BaseConfigurable;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.util.HasName;
import sagex.phoenix.vfs.filters.Filter;
import sagex.phoenix.vfs.groups.Grouper;
import sagex.phoenix.vfs.sorters.Sorter;
import sagex.phoenix.vfs.views.ViewFactory;
import sagex.phoenix.vfs.views.ViewFolder;
import sagex.phoenix.vfs.views.ViewPresentation;
import sagex.remote.json.JSONArray;
import sagex.remote.json.JSONException;
import sagex.remote.json.JSONObject;

public class ViewStateSerializer {
	private Logger log = Logger.getLogger(ViewStateSerializer.class);

	/**
	 * Stores the current state of the view to the given name. A view can have
	 * multiple states, and they can be applied using the loadState method.
	 * 
	 * @param name
	 * @param folder
	 * @throws IOException
	 */
	public void saveState(String name, ViewFolder folder) throws IOException {
		getState(folder);
	}

	/**
	 * Returns the current state of the view.
	 * 
	 * @param folder
	 * @return
	 * @throws IOException
	 */
	public String getState(ViewFolder folder) throws IOException {
		JSONObject viewState = new JSONObject();
		try {
			ViewFactory fact = folder.getViewFactory();
			viewState.put("view", fact.getName());
			JSONArray presentations = new JSONArray();
			if (fact.getViewPresentations().size() > 0) {
				List<ViewPresentation> list = new ArrayList<ViewPresentation>(fact.getViewPresentations());
				for (int i = 0; i < list.size(); i++) {
					presentations.put(serializePresentation(list.get(i)));
				}
			}
			viewState.put("presentations", presentations);
		} catch (JSONException e) {
			throw new IOException("Failed to store ");
		}
		return viewState.toString();
	}

	/**
	 * Loads a named state for the view.
	 * 
	 * @param name
	 * @param folder
	 * @throws IOException
	 */
	public void loadState(String name, ViewFolder folder) throws IOException {

	}

	/**
	 * Applies the given state, as created with getState to the view.
	 * 
	 * @param state
	 * @param folder
	 */
	public void setState(String state, ViewFolder folder) {
		try {
			JSONObject jo = new JSONObject(state);
			ViewFactory f = folder.getViewFactory();
			JSONArray presArr = jo.optJSONArray("presentations");
			if (presArr != null) {
				for (int i = 0; i < presArr.length(); i++) {
					applyPresentation(i, presArr.getJSONObject(i), f);
				}
			}
			folder.setChanged();
		} catch (Exception e) {
			log.warn("Failed to apply state: " + state);
		}
	}

	private void applyPresentation(int i, JSONObject state, ViewFactory f) {
		ViewPresentation vp = f.getViewPresentation(i);
		if (vp != null) {
			applyPresentationState("filter", state.optJSONArray("filters"), vp.getFilters());
			applyPresentationState("sorter", state.optJSONArray("sorters"), vp.getSorters());
			applyPresentationState("grouper", state.optJSONArray("groupers"), vp.getGroupers());
		}
	}

	private void applyPresentationState(String name, JSONArray set, List<? extends BaseConfigurable> config) {
		if (set == null || set.length() == 0 || config == null || config.size() == 0 || set.length() != config.size()) {
			log.warn("Unable to apply '" + name + "' state since state and current configuration does not match; set "
					+ ((set == null) ? "is null" : "size " + set.length()) + "; config "
					+ ((config == null) ? "is null" : "size " + config.size()));
			return;
		}
		// log.info("name: " + name + " size: " + set.length());
		for (int i = 0; i < set.length(); i++) {
			BaseConfigurable bc = config.get(i);
			JSONObject state = set.optJSONObject(i);
			if (state != null) {
				// log.info("Applying State: " + name + " for " + state);
				applyOptions(state.optJSONArray("options"), bc);
			}
		}
	}

	private void applyOptions(JSONArray options, BaseConfigurable bc) {
		if (options == null || bc == null)
			return;
		for (int i = 0; i < options.length(); i++) {
			JSONObject jo = options.optJSONObject(i);
			if (jo != null) {
				ConfigurableOption co = bc.getOption(jo.optString("name"));
				if (co != null) {
					// log.info("setting option: " + jo);
					co.value().setValue(jo.optString("value"));
					bc.setChanged(true);
				}
			}
		}
	}

	private JSONObject serializePresentation(ViewPresentation vp) throws JSONException {
		JSONObject jo = new JSONObject();
		if (vp.getSorters().size() > 0) {
			JSONArray arr = new JSONArray();
			for (Sorter s : vp.getSorters()) {
				arr.put(serializeConfigurable(s));
			}
			jo.put("sorters", arr);
		}
		if (vp.getFilters().size() > 0) {
			JSONArray arr = new JSONArray();
			for (Filter s : vp.getFilters()) {
				arr.put(serializeConfigurable(s));
			}
			jo.put("filters", arr);
		}
		if (vp.getGroupers().size() > 0) {
			JSONArray arr = new JSONArray();
			for (Grouper s : vp.getGroupers()) {
				arr.put(serializeConfigurable(s));
			}
			jo.put("groupers", arr);
		}
		return jo;
	}

	private JSONObject serializeConfigurable(BaseConfigurable conf) throws JSONException {
		JSONObject jo = new JSONObject();
		jo.put("name", ((HasName) conf).getName());
		JSONArray optArr = new JSONArray();
		jo.put("options", optArr);
		for (String s : conf.getOptionNames()) {
			if ("name".equals(s))
				continue;
			ConfigurableOption co = conf.getOption(s);
			if (!StringUtils.isEmpty(co.value().getValue())) {
				JSONObject opt = new JSONObject();
				opt.put("name", co.getName());
				opt.put("value", co.value().getValue());
				optArr.put(opt);
			}
		}
		return jo;
	}

}
