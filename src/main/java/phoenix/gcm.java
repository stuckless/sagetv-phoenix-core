package phoenix;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import sagex.api.Configuration;
import sagex.phoenix.remote.gcm.*;
import sagex.phoenix.remote.gcm.Message.Builder;
import sagex.phoenix.tools.annotation.API;

/**
 * API Generated: Thu Oct 10 19:40:46 EDT 2013<br/>
  */
public final class gcm {
   private static sagex.phoenix.remote.gcm.AndroidDeviceRegistry androiddeviceregistry = new sagex.phoenix.remote.gcm.AndroidDeviceRegistry();
   public static void RegisterDevice(String devId, String devName) {
       androiddeviceregistry.registerDevice(devId, devName);
   }

   public static void RemoveDevice(String id) {
       androiddeviceregistry.removeDevice(id);
   }

   public static Device GetDevice(String id) {
      return androiddeviceregistry.getDevice(id);
   }

   /**

	 * Sends gcm message where the message is in the 'message' field of the data packet.
	 * 
	 * If devices is null, then sends to ALL registered devices
	 * 
	 * @param message
	 * @param device
	 * @throws IOException
	 
    */
   public static boolean SendMessage(String message, String device) {
      return androiddeviceregistry.sendMessage(message, device);
   }

   /**

	 * Sends message to all devices
	 * 
	 * @param message
	 * @throws IOException
	 
    */
   public static boolean SendMessage(String message) {
      return androiddeviceregistry.sendMessage(message);
   }

   /**

	 * Sends message to devices
	 * 
	 * @param message
	 * @param device
	 * @throws IOException
	 
    */
   public static boolean SendMessage(Map<String, String> message, String device) {
      return androiddeviceregistry.sendMessage(message, device);
   }

   public static boolean SendMessage(Map<String, String> message) {
      return androiddeviceregistry.sendMessage(message);
   }

}

