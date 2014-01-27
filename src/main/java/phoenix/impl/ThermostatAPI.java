package phoenix.impl;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import sagex.api.Configuration;
import sagex.phoenix.homecontrol.themostat.IDevice;
import sagex.phoenix.homecontrol.themostat.IDeviceStatus;
import sagex.phoenix.homecontrol.themostat.IThermostatControl;
import sagex.phoenix.homecontrol.themostat.nest.FakeNestControl;
import sagex.phoenix.homecontrol.themostat.nest.NestThermostatControl;
import sagex.phoenix.tools.annotation.API;

/**
 * API the provide access to your home thermostat for purpsoses for reading
 * current conditions and/or adjusting the temperatature
 * 
 * @author seans
 */
@API(group = "thermostat")
public class ThermostatAPI {
	private static final Logger log = Logger.getLogger(ThermostatAPI.class);
	private IThermostatControl api = null;
	private static HashMap<String, String> API_IMPL = new HashMap<String, String>();
	private static HashMap<String, String> API_IMPL_NAME = new HashMap<String, String>();
	static {
		API_IMPL.put("nest", NestThermostatControl.class.getName());
		API_IMPL_NAME.put("nest", "Nest Thermostat");
		API_IMPL.put("fakenest", FakeNestControl.class.getName());
		API_IMPL_NAME.put("fakenest", "Fake Nest Thermostat");
	}
	private static final String API_IMPL_PROP = "phoenix/homecontrol/thermostatSupport";
	private static final String API_IMPL_DEFAULT = "fakenest";

	public ThermostatAPI() {
		try {
			String prop = (String) Configuration.GetProperty(API_IMPL_PROP, API_IMPL_DEFAULT);
			api = (IThermostatControl) Class.forName(API_IMPL.get(prop)).newInstance();

		} catch (Throwable e) {
			log.warn("Failed to load Thermostat support class; defaulting to: " + FakeNestControl.class.getName(), e);
			api = new FakeNestControl();
		}
	}

	/**
	 * Sets current Thermostat Control library. Valid choices are 'nest' and
	 * 'fakenest'. 'fakenest' is used for testing.
	 * 
	 * @param implName
	 * @return null if the api could not be set
	 */
	public IThermostatControl SetThermostatImpl(String implName) {
		try {
			api = (IThermostatControl) Class.forName(API_IMPL.get(implName)).newInstance();
			Configuration.SetProperty(API_IMPL_PROP, implName);
			return api;
		} catch (Throwable e) {
			log.warn("Failed to load thermostat support: " + implName, e);
		}
		return null;
	}

	/**
	 * Get the list of keys used to set an implementation by name, 'nest',
	 * 'fakenest'
	 * 
	 * @return collection of impl keys
	 */
	public ArrayList<String> GetThermostatImplKeys() {
		return new ArrayList<String>(API_IMPL_NAME.keySet());
	}

	/**
	 * Get the impl name for a specific impl key ('nest', 'fakenest')
	 * 
	 * @return name of specific impl key
	 */
	public String GetThermostatImplName(String key) {
		if (API_IMPL_NAME.containsKey(key)) {
			return API_IMPL_NAME.get(key);
		} else {
			return API_IMPL_DEFAULT;
		}
	}

	/**
	 * Get the impl name for the current impl key ('nest', 'fakenest')
	 * 
	 * @return name of current impl key
	 */
	public String GetThermostatImplName() {
		String key = (String) Configuration.GetProperty(API_IMPL_PROP, API_IMPL_DEFAULT);
		if (API_IMPL_NAME.containsKey(key)) {
			return API_IMPL_NAME.get(key);
		} else {
			return API_IMPL_NAME.get(API_IMPL_DEFAULT);
		}
	}

	/**
	 * Get the impl key for the current impl key ('nest', 'fakenest')
	 * 
	 * @return current impl key
	 */
	public String GetThermostatImplKey() {
		String key = (String) Configuration.GetProperty(API_IMPL_PROP, API_IMPL_DEFAULT);
		if (API_IMPL.containsKey(key)) {
			return key;
		} else {
			return API_IMPL_DEFAULT;
		}
	}

	/**
	 * Sets the impl key property if valid ('nest', 'fakenest')
	 * 
	 * @return true if the impl key was valid and set
	 */
	public boolean SetThermostatImplKey(String key) {
		if (API_IMPL.containsKey(key)) {
			Configuration.SetProperty(API_IMPL_PROP, key);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns the list of devices controlled by this controller.
	 * 
	 * @return
	 */
	public List<IDevice> getDevices() {
		return api.getDevices();
	}

	/**
	 * Returns the current device status (temp, humidity, etc)
	 * 
	 * @param device
	 * @return
	 */
	public IDeviceStatus getDeviceStatus(IDevice device) {
		return api.getDeviceStatus(device);
	}

	/**
	 * Gets the Device for given device id
	 * 
	 * @param id
	 * @return
	 */
	public IDevice getDeviceForId(String id) {
		return api.getDeviceForId(id);
	}

	/**
	 * Sets a new target temperature for a device
	 * 
	 * @param device
	 * @param temp
	 */
	public void setTargetTemp(IDevice device, float temp) {
		api.setTargetTemp(device, temp);
	}

	/**
	 * Mode is 'heat', 'cool', 'heatcool' or 'off'
	 * 
	 * @param mode
	 */
	public void setMode(IDevice device, String mode) {
		api.setMode(device, mode);
	}

	/**
	 * Convenience Method that simply checks if the LastMessage is not null
	 * 
	 * @return
	 */
	public boolean HasError() {
		return getLastMessage() != null;
	}

	/**
	 * Returns the last error message that set since the last update. Will be
	 * null if there are no errors.
	 * 
	 * @return
	 */
	public String getLastMessage() {
		return api.getLastMessage();
	}

	/**
	 * Returns the date/time of the last sucessful update
	 * 
	 * @return
	 */
	public long getLastUpdated() {
		return api.getLastUpdated();
	}

	/** 
	 * Returns the Target Temp formatted to 1 decimal and with the Units appended, ie, "22.5 C"
	 * @param devStatus
	 * @return
	 */
	public String GetFormattedTargetTemp(IDeviceStatus devStatus) {
		if (devStatus == null)
			return "N/A";

		return String.format("%.1f %s", devStatus.getTargetTemp(), devStatus.getTempUnits());
	}

	/** 
	 * Returns the Current Temp formatted to 1 decimal and with the Units appended, ie, "22.5 C"
	 * @param devStatus
	 * @return
	 */
	public String GetFormattedCurrentTemp(IDeviceStatus devStatus) {
		if (devStatus == null)
			return "N/A";

		return String.format("%.1f %s", devStatus.getCurrentTemp(), devStatus.getTempUnits());
	}

	/**
	 * Gets the formatted Humidity
	 *  
	 * @param devStatus
	 * @return
	 */
	public String GetFormattedHumidity(IDeviceStatus devStatus) {
		if (devStatus == null)
			return "N/A";

		return String.format("%.1f %s", devStatus.getHumidity());
	}

}
