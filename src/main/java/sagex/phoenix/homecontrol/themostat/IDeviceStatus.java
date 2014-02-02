package sagex.phoenix.homecontrol.themostat;

import sagex.phoenix.tools.annotation.API;

@API(group = "thermostat", proxy = true)
public interface IDeviceStatus {
	public IDevice getDevice();

	public float getCurrentTemp();

	public float getTargetTemp();

	public float getHumidity();

	/**
	 * Returns the current mode, "HEAT", "COOL", "HEAT/COOL", "OFF"
	 * 
	 * @return
	 */
	public String getCurrentMode(); // current_schedule_mode

	public boolean isEnerySaving(); // leaf

	public boolean getCanHeat();

	public boolean getCanCool();

	public String getTempUnits(); // temperature_scale "C" or "F";
}
