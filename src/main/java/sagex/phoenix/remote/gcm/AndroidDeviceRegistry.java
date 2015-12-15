package sagex.phoenix.remote.gcm;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import sagex.api.Configuration;
import sagex.phoenix.remote.gcm.Message.Builder;
import sagex.phoenix.tools.annotation.API;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

@API(group = "gcm")
public class AndroidDeviceRegistry {
    private Logger log = Logger.getLogger(this.getClass());

    private static final String API_KEY = "AIzaSyCmmqKzQaOMjQIFCS1qZ2u9iLFO12qyU0o";

    public static final String ANDROID_DEVICES_KEY = "sagex.phoenix.remote.gcm.devices";
    public static final String NAME = "sagex.phoenix.remote.gcm.devices.{0}.name";
    public static final String LAST_ACCESS = "sagex.phoenix.remote.gcm.devices.{0}.lastaccess";

    private Set<String> devices = new TreeSet<String>();

    public AndroidDeviceRegistry() {
        initDevices();
    }

    protected void initDevices() {
        String devs = Configuration.GetServerProperty(ANDROID_DEVICES_KEY, null);
        if (!StringUtils.isEmpty(devs)) {
            String devarray[] = devs.split(",");
            for (String dev : devarray) {
                dev = dev.trim();
                if (!StringUtils.isEmpty(dev)) {
                    devices.add(dev);
                }
            }
        }
    }

    public void registerDevice(String devId, String devName) {
        Device d = new Device(devId, devName);
        updateDevice(d);
    }

    public void removeDevice(String id) {
        devices.remove(id);
        Configuration.RemoveServerProperty(key(NAME, id));
        Configuration.RemoveServerProperty(key(LAST_ACCESS, id));
        saveDeviceList();
    }

    public Device getDevice(String id) {
        if (!devices.contains(id)) {
            return null;
        }

        Device d = new Device(id, Configuration.GetServerProperty(key(id, NAME), id));
        d.lastAccessed = NumberUtils
                .toLong(Configuration.GetServerProperty(key(id, LAST_ACCESS), null), System.currentTimeMillis());
        return d;
    }

    protected void updateDevice(Device d) {
        devices.add(d.id);
        saveDeviceList();
        Configuration.SetServerProperty(key(NAME, d.id), d.name);
        Configuration.SetServerProperty(key(LAST_ACCESS, d.id), String.valueOf(d.lastAccessed));
    }

    void saveDeviceList() {
        if (devices.size() > 0) {
            Configuration.SetServerProperty(ANDROID_DEVICES_KEY, StringUtils.join(devices, ","));
        } else {
            Configuration.SetServerProperty(ANDROID_DEVICES_KEY, "");
        }
    }

    String key(String key, String id) {
        // not unique... but, rare overlap, given the amount of devices we'll be
        // seeing
        return MessageFormat.format(key, String.valueOf(id.hashCode()));
    }

    /**
     * Sends gcm message where the message is in the 'message' field of the data
     * packet.
     * <p/>
     * If devices is null, then sends to ALL registered devices
     *
     * @param message
     * @param device
     * @throws IOException
     */
    public boolean sendMessage(String message, String... device) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("message", message);
        return sendMessage(map, device);
    }

    /**
     * Sends message to all devices
     *
     * @param message
     * @throws IOException
     */
    public boolean sendMessage(String message) {
        return sendMessage(message, (String[]) null);
    }

    /**
     * Sends message to devices
     *
     * @param message
     * @param device
     * @throws IOException
     */
    public boolean sendMessage(Map<String, String> message, String... device) {
        if (message == null || message.size() == 0 || devices.size() == 0) {
            log.debug("Either no message, or no registered devices");
            return false;
        }

        Builder b = new Message.Builder().delayWhileIdle(true);

        for (Map.Entry<String, String> me : message.entrySet()) {
            b.addData(me.getKey(), me.getValue());
        }

        Message m = b.build();

        Sender sender = new Sender(API_KEY);
        // sender.sendNoRetry(m,
        // "APA91bE2i3-9Iy2-3wAoa9hbq_aATgs-Dplukr0mrjF2QeYreMsBMQo7oLRFqaGBaQCQRU-qlLaPpGbDI3imENBow_0jDvJav8mUiqplAzlc4rKY6i8excz8XeTt1V3t5ZtZFzCUbPwnKaxviIeV6iQirN9xVsEPKXmQ_0BlzgTEt4uhZo3YL7U");
        try {
            sender.send(m, Arrays.asList(device), 5);
            return true;
        } catch (IOException e) {
            log.warn("Failed to send message " + m + " to " + device, e);
            return false;
        }
    }

    public boolean sendMessage(Map<String, String> message) {
        return sendMessage(message, (String[]) null);
    }

}
