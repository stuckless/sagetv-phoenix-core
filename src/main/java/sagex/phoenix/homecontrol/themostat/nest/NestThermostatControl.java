package sagex.phoenix.homecontrol.themostat.nest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.homecontrol.themostat.IDevice;
import sagex.phoenix.homecontrol.themostat.IDeviceStatus;
import sagex.phoenix.homecontrol.themostat.IThermostatControl;
import sagex.phoenix.homecontrol.themostat.nest.jnest.Credentials;
import sagex.phoenix.homecontrol.themostat.nest.jnest.JNest;

public class NestThermostatControl implements IThermostatControl {
/*
 * 
  {
   "tuneups":{
      "02AA01AB501208GY":{
         "$version":-1,
         "$timestamp":1
      }
   },
   "metadata":{
      "02AA01AB501208GY":{
         "$version":-1,
         "$timestamp":1356998400000,
         "last_ip":"127.0.0.1",
         "last_connection":1356998400000
      }
   },
   "track":{
      "02AA01AB501208GY":{
         "$version":-209505229,
         "$timestamp":1368395013481,
         "online":true,
         "last_connection":1368395013481,
         "last_ip":"99.248.231.247"
      }
   },
   "user_settings":{
      "323260":{
         "$version":1529756391,
         "$timestamp":1368143985250,
         "email_verified":true,
         "tos_accepted_version":1319500800000,
         "receive_marketing_emails":true,
         "receive_nest_emails":true,
         "receive_support_emails":true,
         "max_structures":2,
         "max_thermostats":10,
         "max_thermostats_per_structure":10,
         "tos_minimum_version":1319500800000,
         "tos_current_version":1319500800000,
         "lang":"en_US"
      }
   },
   "structure":{
      "73f6fb70-b904-11e2-acbb-12313b0cf507":{
         "$version":-559764244,
         "$timestamp":1368377491000,
         "away_timestamp":1368377486,
         "away":false,
         "touched_by":{
            "touched_by":5
         },
         "dr_reminder_enabled":true,
         "house_type":"family",
         "postal_code":"N5Y5M4",
         "num_thermostats":"1",
         "renovation_date":"2000",
         "country_code":"CA",
         "structure_area":232.258,
         "away_setter":0,
         "measurement_scale":"imperial",
         "user":"user.323260",
         "devices":[
            "device.02AA01AB501208GY"
         ]
      }
   },
   "demand_response":{
      "02AA01AB501208GY":{
         "$version":-1,
         "$timestamp":1
      }
   },
   "utility":{
      "73f6fb70-b904-11e2-acbb-12313b0cf507":{
         "$version":-1,
         "$timestamp":1
      }
   },
   "link":{
      "02AA01AB501208GY":{
         "$version":-239757552,
         "$timestamp":1368371408000,
         "structure":"structure.73f6fb70-b904-11e2-acbb-12313b0cf507"
      }
   },
   "message_center":{
      "323260":{
         "$version":-1,
         "$timestamp":1325379661000,
         "messages":[

         ]
      }
   },
   "device":{
      "02AA01AB501208GY":{
         "$version":-797772820,
         "$timestamp":1368387773000,
         "heatpump_setback_active":false,
         "touched_by":{

         },
         "emer_heat_enable":false,
         "switch_system_off":false,
         "local_ip":"192.168.1.111",
         "away_temperature_high":29.0,
         "y2_type":"unknown",
         "temperature_lock_high_temp":22.222,
         "cooling_source":"electric",
         "leaf_threshold_cool":25.55,
         "heater_source":"gas",
         "fan_cooling_state":false,
         "note_codes":[

         ],
         "compressor_lockout_leaf":-17.8,
         "has_x3_heat":false,
         "target_humidity_enabled":false,
         "heat_x3_source":"gas",
         "alt_heat_delivery":"forced-air",
         "has_x2_heat":false,
         "fan_mode":"auto",
         "sunlight_correction_active":false,
         "rssi":67.0,
         "emer_heat_delivery":"forced-air",
         "heatpump_savings":"off",
         "pin_y2_description":"none",
         "filter_reminder_level":0,
         "filter_reminder_enabled":false,
         "capability_level":3.5,
         "schedule_learning_reset":false,
         "has_x2_cool":false,
         "hvac_pins":"W1,Y1,Rh,G",
         "ob_orientation":"O",
         "cooling_delivery":"unknown",
         "range_enable":true,
         "dual_fuel_breakpoint_override":"none",
         "auto_away_enable":true,
         "lower_safety_temp_enabled":true,
         "has_fan":true,
         "dehumidifier_state":false,
         "emer_heat_source":"electric",
         "nlclient_state":"",
         "heatpump_ready":false,
         "cooling_x2_delivery":"unknown",
         "available_locales":"en_US,fr_CA,es_US",
         "current_version":"3.5",
         "learning_state":"steady",
         "pin_ob_description":"none",
         "pin_rh_description":"power",
         "has_alt_heat":false,
         "fan_duty_cycle":3600,
         "pin_y1_description":"cool",
         "humidifier_state":false,
         "gear_threshold_high":0.0,
         "backplate_serial_number":"02BA01AB50120NZU",
         "has_x2_alt_heat":false,
         "heat_x3_delivery":"forced-air",
         "leaf_threshold_heat":18.89,
         "has_emer_heat":false,
         "learning_mode":true,
         "leaf_learning":"ready",
         "has_aux_heat":false,
         "filter_changed_set_date":0,
         "aux_heat_source":"electric",
         "backplate_bsl_info":"BSL",
         "sunlight_correction_ready":false,
         "alt_heat_x2_source":"gas",
         "pin_c_description":"none",
         "humidifier_type":"unknown",
         "pin_w2aux_description":"none",
         "fan_timer_timeout":0,
         "sunlight_correction_enabled":true,
         "country_code":"CA",
         "heat_x2_delivery":"forced-air",
         "target_humidity":35.0,
         "gear_threshold_low":0.0,
         "lower_safety_temp":4.444,
         "cooling_x2_source":"electric",
         "equipment_type":"electric",
         "heat_pump_aux_threshold":10.0,
         "alt_heat_x2_delivery":"forced-air",
         "heat_pump_comp_threshold":-31.5,
         "learning_days_completed_cool":1,
         "backplate_bsl_version":"2.1",
         "current_schedule_mode":"HEAT",
         "fan_duty_end_time":0,
         "hvac_wires":"Heat,Cool,Fan,Rh",
         "leaf":false,
         "type":"TBD",
         "pin_g_description":"fan",
         "click_sound":"on",
         "aux_heat_delivery":"forced-air",
         "away_temperature_low_enabled":true,
         "filter_changed_date":0,
         "heat_pump_comp_threshold_enabled":false,
         "preconditioning_ready":true,
         "has_dehumidifier":false,
         "fan_cooling_enabled":true,
         "leaf_away_high":28.88,
         "fan_cooling_readiness":"not ready",
         "temperature_scale":"C",
         "device_locale":"en_US",
         "maint_band_upper":0.39,
         "error_code":"",
         "battery_level":3.846,
         "preconditioning_active":false,
         "fan_control_state":false,
         "away_temperature_high_enabled":true,
         "learning_days_completed_heat":3,
         "upper_safety_temp_enabled":false,
         "pin_star_description":"none",
         "preconditioning_enabled":false,
         "current_humidity":34,
         "dual_fuel_breakpoint":-1.0,
         "postal_code":"N5Y5M4",
         "backplate_mono_version":"4.0.14",
         "alt_heat_source":"gas",
         "aux_lockout_leaf":10.0,
         "has_heat_pump":false,
         "heater_delivery":"forced-air",
         "radiant_control_enabled":false,
         "auto_away_reset":false,
         "away_temperature_low":10.0,
         "has_air_filter":true,
         "temperature_lock":false,
         "upper_safety_temp":35.0,
         "time_to_target_training":"training",
         "dehumidifier_type":"unknown",
         "fan_timer_duration":900,
         "target_time_confidence":0.0,
         "temperature_lock_low_temp":20.0,
         "pin_w1_description":"heat",
         "forced_air":true,
         "temperature_lock_pin_hash":"",
         "auto_dehum_enabled":false,
         "leaf_type":1,
         "backplate_mono_info":"TFE (BP_D2) 4.0.14 (root@bamboo) 2013-04-16 17:44:31",
         "star_type":"unknown",
         "has_dual_fuel":false,
         "maint_band_lower":0.39,
         "creation_time":1368368282965,
         "learning_time":1020,
         "has_humidifier":false,
         "learning_days_completed_range":0,
         "dehumidifier_orientation_selected":"unknown",
         "leaf_schedule_delta":1.11,
         "logging_priority":"informational",
         "user_brightness":"high",
         "leaf_away_low":16.67,
         "pin_rc_description":"none",
         "auto_dehum_state":false,
         "serial_number":"02AA01AB501208GY",
         "heat_x2_source":"gas",
         "mac_address":"18b4300a5196",
         "fan_duty_start_time":0,
         "time_to_target":0,
         "backplate_model":"Backplate-2.5",
         "model_version":"Display-2.6",
         "heat_pump_aux_threshold_enabled":true,
         "ob_persistence":true
      }
   },
   "shared":{
      "02AA01AB501208GY":{
         "$version":62932194,
         "$timestamp":1368390402000,
         "touched_by":{

         },
         "auto_away":0,
         "auto_away_learning":"training",
         "hvac_heat_x3_state":false,
         "compressor_lockout_enabled":false,
         "hvac_alt_heat_state":false,
         "target_temperature_type":"heat",
         "hvac_heater_state":false,
         "hvac_emer_heat_state":false,
         "can_heat":true,
         "compressor_lockout_timeout":0,
         "hvac_cool_x2_state":false,
         "target_temperature_high":24.0,
         "hvac_aux_heater_state":false,
         "hvac_heat_x2_state":false,
         "target_temperature_low":20.0,
         "target_temperature":22,
         "hvac_ac_state":false,
         "hvac_fan_state":false,
         "target_change_pending":false,
         "name":"Main",
         "current_temperature":22.47,
         "hvac_alt_heat_x2_state":false,
         "can_cool":true
      }
   },
   "schedule":{
      "02AA01AB501208GY":{
         "$version":1930634632,
         "$timestamp":1368378414000,
         "days":{
            "3":{
               "0":{
                  "touched_by":1,
                  "time":0,
                  "touched_tzo":-14400,
                  "entry_type":"continuation",
                  "temp":20.0,
                  "type":"HEAT",
                  "touched_at":1368371820
               }
            },
            "2":{
               "0":{
                  "touched_by":1,
                  "time":0,
                  "touched_tzo":-14400,
                  "entry_type":"continuation",
                  "temp":20.0,
                  "type":"HEAT",
                  "touched_at":1368371820
               }
            },
            "1":{
               "0":{
                  "touched_by":1,
                  "time":0,
                  "touched_tzo":-14400,
                  "entry_type":"continuation",
                  "temp":20.0,
                  "type":"HEAT",
                  "touched_at":1368371820
               }
            },
            "0":{
               "0":{
                  "touched_by":1,
                  "time":0,
                  "touched_tzo":-14400,
                  "entry_type":"continuation",
                  "temp":20.0,
                  "type":"HEAT",
                  "touched_at":1368371820
               }
            },
            "6":{
               "0":{
                  "touched_by":1,
                  "time":0,
                  "touched_tzo":-14400,
                  "entry_type":"continuation",
                  "temp":20.0,
                  "type":"HEAT",
                  "touched_at":1368371820
               }
            },
            "5":{
               "0":{
                  "touched_by":1,
                  "time":0,
                  "touched_tzo":-14400,
                  "entry_type":"continuation",
                  "temp":20.0,
                  "type":"HEAT",
                  "touched_at":1368371820
               }
            },
            "4":{
               "0":{
                  "touched_by":1,
                  "time":0,
                  "touched_tzo":-14400,
                  "entry_type":"continuation",
                  "temp":20.0,
                  "type":"HEAT",
                  "touched_at":1368371820
               }
            }
         },
         "schedule_mode":"HEAT",
         "name":"Main Current Schedule",
         "ver":2
      }
   },
   "user_alert_dialog":{
      "323260":{
         "$version":-615481805,
         "$timestamp":1368371413000,
         "dialog_id":"confirm-pairing",
         "dialog_data":""
      }
   },
   "user":{
      "323260":{
         "$version":878705010,
         "$timestamp":1368143955000,
         "name":"sean.stuckless@gmail.com",
         "structures":[
            "structure.73f6fb70-b904-11e2-acbb-12313b0cf507"
         ]
      }
   }
} */
	private Logger log = Logger.getLogger(this.getClass());
	
	protected static final long CacheDelay = 1000*60;
	protected long lastUpdate = 0;
	protected JsonObject nestResponse = null;
	protected List<IDevice> devices = new ArrayList<IDevice>();
	protected HashMap<String, IDeviceStatus> statuses = new HashMap<String, IDeviceStatus>();
	protected String lastMessage = null;
	protected NestConfiguration config = null;
	protected JNest jnest = new JNest();
	
	public NestThermostatControl() {
		config = GroupProxy.get(NestConfiguration.class);
	}

	protected synchronized JsonObject getNestData() {
		if (nestResponse == null || (System.currentTimeMillis()-lastUpdate)>CacheDelay) {
			try {
				log.debug("Fetching NEST info");
				nestResponse = resolveNestData();
				
				// fill devices
				devices.clear();
				statuses.clear();
				
				JsonObject devs = nestResponse.getAsJsonObject("device");
				for (Entry<String, JsonElement> d : devs.entrySet()) {
					JsonObject mainDev = d.getValue().getAsJsonObject();
					if (mainDev==null) throw new Exception("No Device for " + d.getKey());
					
					JsonObject sharedDev = nestResponse.getAsJsonObject("shared").getAsJsonObject(d.getKey());
					if (sharedDev==null) throw new Exception("No Shared Device for " + d.getKey());
					
					// add device
					NestDevice device = new NestDevice(d.getKey(),  sharedDev.get("name").getAsString());
					devices.add(device);

					// create an add status
					NestDeviceStatus status = new NestDeviceStatus();
					status.canCool = sharedDev.get("can_cool").getAsBoolean();
					status.canHeat = sharedDev.get("can_heat").getAsBoolean();
					status.currentMode = sharedDev.get("target_temperature_type").getAsString();
					status.currentTemp = sharedDev.get("current_temperature").getAsFloat();
					status.device = device;
					status.humidity = mainDev.get("current_humidity").getAsFloat();
					status.isEnerySaving = mainDev.get("leaf").getAsBoolean();
					status.targetTemp = sharedDev.get("target_temperature").getAsFloat();
					status.tempUnits = mainDev.get("temperature_scale").getAsString();
					
					statuses.put(device.getId(), status);
					
					lastUpdate = System.currentTimeMillis();
					log.info("Nest Info Updated");
				}
				
			} catch (Throwable t) {
				t.printStackTrace();
				
				log.warn("Failed to update NEST info", t);
				lastMessage = t.getMessage();
			}
		}
		return nestResponse;
	}
	
	protected JsonObject resolveNestData() throws IOException {
		if (!jnest.isLoggedIn) {
			if (jnest.getCredentials()==null) {
				jnest.setCredentials(new Credentials(config.getUsername(), config.getPassword()));
			}
			jnest.login();
		}
		return jnest.getDeviceStatusInfo();
	}

	@Override
	public List<IDevice> getDevices() {
		getNestData();
		return devices;
	}

	@Override
	public IDeviceStatus getDeviceStatus(IDevice device) {
		getNestData();
		return statuses.get(device.getId());
	}

	@Override
	public IDevice getDeviceForId(String id) {
		for (IDevice d: getDevices()) {
			if (id.equals(d.getId())) {
				return d;
			}
		}
		return null;
	}

	@Override
	public void setTargetTemp(IDevice device, float temp) {
		throw new UnsupportedOperationException("setTargetTemp() not implemented");
	}

	@Override
	public void setMode(IDevice device, String mode) {
		throw new UnsupportedOperationException("setMode() not implemented");
	}
	
	public String getLastMessage() {
		return lastMessage;
	}
	
	public long getLastUpdated() {
		return lastUpdate;
	}
}
