package sagex.phoenix.homecontrol.themostat;

import java.util.List;

/**
 * Communicates with NEST and gets nest information
 * 
 * 
 * @author seans
 *
 */
public interface IThermostatControl {
	/**
	 * Returns the list of devices controlled by this controller.
	 * 
	 * @return
	 */
	public List<IDevice> getDevices();
	
	/**
	 * Returns the current device status (temp, humidity, etc)
	 * @param device
	 * @return
	 */
	public IDeviceStatus getDeviceStatus(IDevice device);
	
	/**
	 * Gets the Device for given device id
	 * @param id
	 * @return
	 */
	public IDevice getDeviceForId(String id);
	
	/**
	 * Sets a new target temperature for a device
	 * 
	 * @param device
	 * @param temp
	 */
	public void setTargetTemp(IDevice device, float temp);
	/**
	 * Mode is 'heat', 'cool', 'heatcool' or 'off'
	 * @param mode
	 */
	public void setMode(IDevice device, String mode);
	
	/**
	 * Returns the last error message that set since the last update.  Will be null if there are no errors.
	 * @return
	 */
	public String getLastMessage();
	
	/**
	 * Returns the date/time of the last sucessful update
	 * @return
	 */
	public long getLastUpdated();
}

