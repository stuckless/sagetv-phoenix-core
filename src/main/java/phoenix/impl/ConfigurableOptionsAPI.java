package phoenix.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.factory.ConfigurableOption.ListValue;
import sagex.phoenix.tools.annotation.API;

/**
 * API for managing configurable options that tend to be used in Factories and
 * Views
 * 
 * @author seans
 */
@API(group = "opt")
public class ConfigurableOptionsAPI {
	public String GetName(ConfigurableOption opt) {
		return opt.getName();
	}

	public String GetLabel(ConfigurableOption opt) {
		return opt.getLabel();
	}

	public String GetType(ConfigurableOption opt) {
		return opt.getDataType().name();
	}

	public String GetValue(ConfigurableOption opt) {
		return opt.getString("");
	}

	public int GetInt(ConfigurableOption opt) {
		return opt.getInt(0);
	}

	public boolean GetBoolean(ConfigurableOption opt) {
		return opt.getBoolean(true);
	}

	public float GetFloat(ConfigurableOption opt) {
		return opt.getFloat(0f);
	}

	public void SetValue(ConfigurableOption opt, String value) {
		opt.value().setValue(value);
	}

	public boolean IsList(ConfigurableOption opt) {
		return opt.isList();
	}

	public boolean IsMultiSelectList(ConfigurableOption opt) {
		return IsList(opt) && ListSelection.multi.equals(opt.getListSelection());
	}

	public List<ListValue> GetListValues(ConfigurableOption opt) {
		return opt.getListValues();
	}

	public String GetName(ListValue listValue) {
		return listValue.getName();
	}

	public String GetValue(ListValue listValue) {
		return listValue.getValue();
	}

	public boolean IsToggle(ConfigurableOption opt) {
		return DataType.bool.equals(opt.getDataType())
				|| (IsList(opt) && opt.getListValues() != null && opt.getListValues().size() >= 2);
	}

	public void Toggle(ConfigurableOption opt) {
		if (IsToggle(opt)) {
			if (DataType.bool.equals(opt.getDataType())) {
				// if we are boolean, then just toggle
				opt.value().setValue(String.valueOf(!opt.getBoolean(true)));
				return;
			}

			// otherwise toggle list
			List<ListValue> values = opt.getListValues();
			String curVal = opt.getString("");
			if (curVal == null) {
				opt.value().setValue(values.get(0).getValue());
				return;
			}

			// otherwise, find our option, and then move to the next one
			int pos = -1;
			for (int i = 0; i < values.size(); i++) {
				if (curVal.equals(values.get(i).getValue())) {
					pos = i;
					break;
				}
			}

			// move to next spot
			pos = pos + 1;
			if (pos >= values.size()) {
				pos = 0;
			}
			opt.value().setValue(values.get(pos).getValue());
		}
	}

	public boolean IsListItemSelected(ConfigurableOption opt, ListValue item) {
		Object val = opt.value().get();
		if (val == null)
			return false;
		String vals[] = String.valueOf(val).split("\\s*,\\s*");

		for (String s : vals) {
			if (s.equals(item.getValue()))
				return true;
		}

		return false;
	}

	public void SelectItem(ConfigurableOption opt, ListValue item) {
		// if single selection, then set this item
		if (opt.isList() && ListSelection.single.equals(opt.getListSelection())) {
			opt.value().setValue(item.getValue());
		}

		// multi-select, then add this selection
		Object val = opt.value().get();
		if (val == null)
			return;
		List<String> vals = new ArrayList<String>(Arrays.asList(String.valueOf(val).split("\\s*,\\s*")));
		if (vals.contains(item.getValue()))
			return;
		vals.add(item.getValue());
		opt.value().setValue(StringUtils.join(vals, ","));
	}

	public void UnselectItem(ConfigurableOption opt, ListValue item) {
		// if single selection, then set this item
		if (opt.isList() && ListSelection.single.equals(opt.getListSelection())) {
			opt.value().setValue(null);
		}

		// multi-select, then add this selection
		Object val = opt.value().get();
		if (val == null)
			return;
		List<String> vals = new ArrayList<String>(Arrays.asList(String.valueOf(val).split("\\s*,\\s*")));
		vals.remove(item.getValue());
		opt.value().setValue(StringUtils.join(vals, ","));
	}

	public ListValue GetSelectedItem(ConfigurableOption opt) {
		for (ListValue lv : opt.getListValues()) {
			if (IsListItemSelected(opt, lv)) {
				return lv;
			}
		}
		return null;
	}

}
