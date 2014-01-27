package phoenix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;
import phoenix.impl.*;
import sagex.api.Configuration;
import sagex.phoenix.homecontrol.themostat.*;
import sagex.phoenix.homecontrol.themostat.IDevice;
import sagex.phoenix.homecontrol.themostat.IDeviceStatus;
import sagex.phoenix.homecontrol.themostat.IThermostatControl;
import sagex.phoenix.homecontrol.themostat.nest.FakeNestControl;
import sagex.phoenix.homecontrol.themostat.nest.NestThermostatControl;
import sagex.phoenix.tools.annotation.API;

/**
 * API Generated: Thu Oct 10 19:40:46 EDT 2013<br/>
 * API Source: {@link ThermostatAPI}<br/>

 * API the provide access to your home thermostat for purpsoses for reading
 * current conditions and/or adjusting the temperatature
 * 
 * @author seans
 
  */
public final class thermostat {
   private static phoenix.impl.ThermostatAPI thermostatapi = new phoenix.impl.ThermostatAPI();
   /**

	 * Sets current Thermostat Control library. Valid choices are 'nest' and
	 * 'fakenest'. 'fakenest' is used for testing.
	 * 
	 * @param implName
	 * @return null if the api could not be set
	 
    */
   public static IThermostatControl SetThermostatImpl(String implName) {
      return thermostatapi.SetThermostatImpl(implName);
   }

   /**

	 * Get the list of keys used to set an implementation by name, 'nest',
	 * 'fakenest'
	 * 
	 * @return collection of impl keys
	 
    */
   public static ArrayList<String> GetThermostatImplKeys() {
      return thermostatapi.GetThermostatImplKeys();
   }

   /**

	 * Get the impl name for a specific impl key ('nest', 'fakenest')
	 * 
	 * @return name of specific impl key
	 
    */
   public static String GetThermostatImplName(String key) {
      return thermostatapi.GetThermostatImplName(key);
   }

   /**

	 * Get the impl name for the current impl key ('nest', 'fakenest')
	 * 
	 * @return name of current impl key
	 
    */
   public static String GetThermostatImplName() {
      return thermostatapi.GetThermostatImplName();
   }

   /**

	 * Get the impl key for the current impl key ('nest', 'fakenest')
	 * 
	 * @return current impl key
	 
    */
   public static String GetThermostatImplKey() {
      return thermostatapi.GetThermostatImplKey();
   }

   /**

	 * Sets the impl key property if valid ('nest', 'fakenest')
	 * 
	 * @return true if the impl key was valid and set
	 
    */
   public static boolean SetThermostatImplKey(String key) {
      return thermostatapi.SetThermostatImplKey(key);
   }

   /**

	 * Returns the list of devices controlled by this controller.
	 * 
	 * @return
	 
    */
   public static List<IDevice> GetDevices() {
      return thermostatapi.getDevices();
   }

   /**

	 * Returns the current device status (temp, humidity, etc)
	 * 
	 * @param device
	 * @return
	 
    */
   public static IDeviceStatus GetDeviceStatus(IDevice device) {
      return thermostatapi.getDeviceStatus(device);
   }

   /**

	 * Gets the Device for given device id
	 * 
	 * @param id
	 * @return
	 
    */
   public static IDevice GetDeviceForId(String id) {
      return thermostatapi.getDeviceForId(id);
   }

   /**

	 * Sets a new target temperature for a device
	 * 
	 * @param device
	 * @param temp
	 
    */
   public static void SetTargetTemp(IDevice device, float temp) {
       thermostatapi.setTargetTemp(device, temp);
   }

   /**

	 * Mode is 'heat', 'cool', 'heatcool' or 'off'
	 * 
	 * @param mode
	 
    */
   public static void SetMode(IDevice device, String mode) {
       thermostatapi.setMode(device, mode);
   }

   /**

	 * Convenience Method that simply checks if the LastMessage is not null
	 * 
	 * @return
	 
    */
   public static boolean HasError() {
      return thermostatapi.HasError();
   }

   /**

	 * Returns the last error message that set since the last update. Will be
	 * null if there are no errors.
	 * 
	 * @return
	 
    */
   public static String GetLastMessage() {
      return thermostatapi.getLastMessage();
   }

   /**

	 * Returns the date/time of the last sucessful update
	 * 
	 * @return
	 
    */
   public static long GetLastUpdated() {
      return thermostatapi.getLastUpdated();
   }

   /**
 
	 * Returns the Target Temp formatted to 1 decimal and with the Units appended, ie, "22.5 C"
	 * @param devStatus
	 * @return
	 
    */
   public static String GetFormattedTargetTemp(IDeviceStatus devStatus) {
      return thermostatapi.GetFormattedTargetTemp(devStatus);
   }

   /**
 
	 * Returns the Current Temp formatted to 1 decimal and with the Units appended, ie, "22.5 C"
	 * @param devStatus
	 * @return
	 
    */
   public static String GetFormattedCurrentTemp(IDeviceStatus devStatus) {
      return thermostatapi.GetFormattedCurrentTemp(devStatus);
   }

   /**

	 * Gets the formatted Humidity
	 *  
	 * @param devStatus
	 * @return
	 
    */
   public static String GetFormattedHumidity(IDeviceStatus devStatus) {
      return thermostatapi.GetFormattedHumidity(devStatus);
   }

   public static String GetId(IDevice idevice) {
      try {
      if (idevice == null) {
         return null;
      }
      return idevice.getId();
      } catch (Throwable t) {
         t.printStackTrace();
         return null;
      }
   }

   public static String GetName(IDevice idevice) {
      try {
      if (idevice == null) {
         return null;
      }
      return idevice.getName();
      } catch (Throwable t) {
         t.printStackTrace();
         return null;
      }
   }

   public static IDevice GetDevice(IDeviceStatus idevicestatus) {
      try {
      if (idevicestatus == null) {
         return null;
      }
      return idevicestatus.getDevice();
      } catch (Throwable t) {
         t.printStackTrace();
         return null;
      }
   }

   public static float GetCurrentTemp(IDeviceStatus idevicestatus) {
      try {
      if (idevicestatus == null) {
         return 0;
      }
      return idevicestatus.getCurrentTemp();
      } catch (Throwable t) {
         t.printStackTrace();
         return 0;
      }
   }

   public static float GetTargetTemp(IDeviceStatus idevicestatus) {
      try {
      if (idevicestatus == null) {
         return 0;
      }
      return idevicestatus.getTargetTemp();
      } catch (Throwable t) {
         t.printStackTrace();
         return 0;
      }
   }

   public static float GetHumidity(IDeviceStatus idevicestatus) {
      try {
      if (idevicestatus == null) {
         return 0;
      }
      return idevicestatus.getHumidity();
      } catch (Throwable t) {
         t.printStackTrace();
         return 0;
      }
   }

   /**

	 * Returns the current mode, "HEAT", "COOL", "HEAT/COOL", "OFF"
	 * @return
	 
    */
   public static String GetCurrentMode(IDeviceStatus idevicestatus) {
      try {
      if (idevicestatus == null) {
         return null;
      }
      return idevicestatus.getCurrentMode();
      } catch (Throwable t) {
         t.printStackTrace();
         return null;
      }
   }

   public static boolean IsEnerySaving(IDeviceStatus idevicestatus) {
      try {
      if (idevicestatus == null) {
         return false;
      }
      return idevicestatus.isEnerySaving();
      } catch (Throwable t) {
         t.printStackTrace();
         return false;
      }
   }

   public static boolean GetCanHeat(IDeviceStatus idevicestatus) {
      try {
      if (idevicestatus == null) {
         return false;
      }
      return idevicestatus.getCanHeat();
      } catch (Throwable t) {
         t.printStackTrace();
         return false;
      }
   }

   public static boolean GetCanCool(IDeviceStatus idevicestatus) {
      try {
      if (idevicestatus == null) {
         return false;
      }
      return idevicestatus.getCanCool();
      } catch (Throwable t) {
         t.printStackTrace();
         return false;
      }
   }

   public static String GetTempUnits(IDeviceStatus idevicestatus) {
      try {
      if (idevicestatus == null) {
         return null;
      }
      return idevicestatus.getTempUnits();
      } catch (Throwable t) {
         t.printStackTrace();
         return null;
      }
   }

}

