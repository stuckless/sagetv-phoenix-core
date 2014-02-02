package sagex.phoenix.homecontrol.themostat.nest;

import sagex.phoenix.homecontrol.themostat.IDevice;
import sagex.phoenix.homecontrol.themostat.IDeviceStatus;

public class NestDeviceStatus implements IDeviceStatus {
	protected IDevice device;
	protected float currentTemp;
	protected float targetTemp;
	protected String tempUnits;
	protected boolean canCool;
	protected boolean canHeat;
	protected boolean isEnerySaving;
	protected String currentMode;
	protected float humidity;

	public NestDeviceStatus() {
	}

	@Override
	public IDevice getDevice() {
		return device;
	}

	@Override
	public float getCurrentTemp() {
		return currentTemp;
	}

	@Override
	public float getTargetTemp() {
		return targetTemp;
	}

	@Override
	public float getHumidity() {
		return humidity;
	}

	@Override
	public String getCurrentMode() {
		return currentMode;
	}

	@Override
	public boolean isEnerySaving() {
		return isEnerySaving;
	}

	@Override
	public boolean getCanHeat() {
		return canHeat;
	}

	@Override
	public boolean getCanCool() {
		return canCool;
	}

	@Override
	public String getTempUnits() {
		return tempUnits;
	}
}
