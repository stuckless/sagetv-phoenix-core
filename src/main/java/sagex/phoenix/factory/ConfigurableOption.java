package sagex.phoenix.factory;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import sagex.phoenix.util.var.DynamicVariable;

/**
 * Represents a Configurable Factory Option
 * 
 * @author seans
 */
public class ConfigurableOption implements Cloneable, Comparable<ConfigurableOption> {
	public static class ListValue implements Cloneable {
		public ListValue(String name, String value) {
			this.name = name;
			this.value = value;
		}

		private String name;
		private String value;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public Object clone() throws CloneNotSupportedException {
			return super.clone();
		}
	}

	public enum DataType {
		string, bool, integer, directory
	}

	public enum ListSelection {
		single, multi
	}

	private String name;
	private String label;
	private DataType dataType;
	private boolean isList;
	private ListSelection listSelection;

	private DynamicVariable<Object> value = new DynamicVariable<Object>(Object.class);
	private List<ListValue> listValues;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
		result = prime * result + (isList ? 1231 : 1237);
		result = prime * result + ((listSelection == null) ? 0 : listSelection.hashCode());
		result = prime * result + ((listValues == null) ? 0 : listValues.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConfigurableOption other = (ConfigurableOption) obj;
		if (dataType == null) {
			if (other.dataType != null)
				return false;
		} else if (!dataType.equals(other.dataType))
			return false;
		if (isList != other.isList)
			return false;
		if (listSelection == null) {
			if (other.listSelection != null)
				return false;
		} else if (!listSelection.equals(other.listSelection))
			return false;
		if (listValues == null) {
			if (other.listValues != null)
				return false;
		} else if (!listValues.equals(other.listValues))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FactoryOption [" + (dataType != null ? "dataType=" + dataType + ", " : "") + "isList=" + isList + ", "
				+ (listSelection != null ? "listSelection=" + listSelection + ", " : "")
				+ (listValues != null ? "listValues=" + listValues + ", " : "") + (name != null ? "name=" + name + ", " : "")
				+ (value != null ? "value=" + value : "") + "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DynamicVariable<Object> value() {
		return value;
	}

	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	public boolean isList() {
		return isList;
	}

	public void setList(boolean isList) {
		this.isList = isList;
	}

	public ListSelection getListSelection() {
		return listSelection;
	}

	public void setListSelection(ListSelection listSelection) {
		this.listSelection = listSelection;
	}

	public List<ListValue> getListValues() {
		return listValues;
	}

	public void setListValues(List<ListValue> listValues) {
		this.listValues = listValues;
	}

	public ConfigurableOption(String name) {
		super();
		this.name = name;
	}

	public ConfigurableOption(String name, String label, String value, DataType dataType) {
		super();
		this.name = name;
		this.label = label;
		this.value.setValue(value);
		this.dataType = dataType;
	}

	public ConfigurableOption(String name, String label, String value, DataType dataType, boolean isList,
			ListSelection listSelection, List<ListValue> listValues) {
		super();
		this.name = name;
		this.label = label;
		this.value.setValue(value);
		this.dataType = dataType;
		this.isList = isList;
		this.listSelection = listSelection;
		this.listValues = listValues;
	}

	/**
	 * Create a new Configuration option
	 * 
	 * @param name
	 * @param label
	 * @param value
	 * @param dataType
	 * @param isList
	 * @param listSelection
	 * @param listValues
	 *            List in the format "Key1:Label1,Key2:Label2,..."
	 */
	public ConfigurableOption(String name, String label, String value, DataType dataType, boolean isList,
			ListSelection listSelection, String listValues) {
		this(name, label, value, dataType, isList, listSelection, convertToList(listValues));
	}

	public ConfigurableOption(String name, String value) {
		this(name, null, value, null);
	}

	private static List<ListValue> convertToList(String values) {
		List<ListValue> list = new ArrayList<ListValue>();
		if (values == null) {
			return list;
		}

		String varr[] = values.split("\\s*,\\s*");
		for (String v : varr) {
			String nvp[] = v.split("\\s*:\\s*");
			ListValue lv = null;
			if (nvp.length > 1) {
				lv = new ListValue(nvp[1], nvp[0]);
			} else {
				lv = new ListValue(nvp[0], nvp[0]);
			}
			list.add(lv);
		}
		return list;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getString(String defValue) {
		Object v = value.get();
		if (v == null)
			return defValue;
		return String.valueOf(v);
	}

	public int getInt(int defValue) {
		Object v = value.get();
		if (v == null)
			return defValue;
		return NumberUtils.toInt(String.valueOf(v));
	}

	public boolean getBoolean(boolean defValue) {
		Object v = value.get();
		if (v == null)
			return defValue;
		return BooleanUtils.toBoolean(String.valueOf(v));
	}

	public float getFloat(float defValue) {
		Object v = value.get();
		if (v == null)
			return defValue;
		return NumberUtils.toFloat(String.valueOf(v));
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		ArrayList<ListValue> newList = null;
		if (listValues != null) {
			newList = new ArrayList<ListValue>();
			for (ListValue lv : listValues) {
				newList.add((ListValue) lv.clone());
			}
		}

		// return new ConfigurableOption(name, label, value.getValue(),
		// dataType, isList, listSelection, newList);
		ConfigurableOption co = (ConfigurableOption) super.clone();
		co.value = new DynamicVariable<Object>(value.getType(), value.getValue());
		co.listValues = newList;
		return co;
	}

	@Override
	public int compareTo(ConfigurableOption o) {
		if (this.name == null)
			return -1;
		return this.name.compareTo(o.name);
	}

	public void updateFrom(ConfigurableOption newOption) {
		if (StringUtils.isEmpty(newOption.getLabel())) {
			this.setLabel(newOption.getLabel());
		}
		this.value().setValue(newOption.value().getValue());
	}
}
