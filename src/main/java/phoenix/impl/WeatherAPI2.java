package phoenix.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import sagex.api.Configuration;
import sagex.phoenix.tools.annotation.API;
import sagex.phoenix.weather.ICurrentForecast;
import sagex.phoenix.weather.IForecastPeriod;
import sagex.phoenix.weather.ILongRangeForecast;
import sagex.phoenix.weather.IWeatherSupport2;
import sagex.phoenix.weather.IWeatherSupport2.Units;
import sagex.phoenix.weather.worldweather.WorldWeatherWeatherSupport;
import sagex.phoenix.weather.yahoo.YahooWeatherSupport;
import sagex.phoenix.weather.yahoo.YahooWeatherSupport2;

/**
 * WeatherAPI provides access to weather information, including current forecast and 
 * long range forecasts.
 *  
 * @author seans
 */
@API(group = "weather2")
public class WeatherAPI2 {
	private static final Logger log = Logger.getLogger(WeatherAPI2.class);
	private IWeatherSupport2 api = null;
	private List<IForecastPeriod> periods = null;
	private static HashMap<Integer, Integer> dayCodeMap  =new HashMap<Integer, Integer>();
	static {
		dayCodeMap.put(23,24);//
		dayCodeMap.put(27,28);//
		dayCodeMap.put(29,30);//
		dayCodeMap.put(31,32);//
		dayCodeMap.put(33,34);//
		dayCodeMap.put(47,37);//
		dayCodeMap.put(45,39);//
		dayCodeMap.put(46,41);//
	}
	private static HashMap<Integer, Integer> nightCodeMap  =new HashMap<Integer, Integer>();
	static {
                nightCodeMap.put(24,23);//
		nightCodeMap.put(28,27);//
		nightCodeMap.put(30,29);//
		nightCodeMap.put(32,31);//
		nightCodeMap.put(34,33);//
		nightCodeMap.put(36,31);//
		nightCodeMap.put(37,47);//
		nightCodeMap.put(38,47);//
		nightCodeMap.put(39,45);//
		nightCodeMap.put(41,46);//
        }
	private static HashMap<Integer, String> codeMap  =new HashMap<Integer, String>();
	static {
		codeMap.put(-1,"Unknown");
		codeMap.put(0,"Tornado");
		codeMap.put(1,"Tropical Storm");
		codeMap.put(2,"Hurricane");
		codeMap.put(3,"Severe Thunderstorms");
		codeMap.put(4,"Thunderstorms");
		codeMap.put(5,"Mixed Rain and Snow");
		codeMap.put(6,"Mixed Rain and Sleet");
		codeMap.put(7,"Mixed Snow and Sleet");
		codeMap.put(8,"Freezing Drizzle");
		codeMap.put(9,"Drizzle");
		codeMap.put(10,"Freezing Rain");
		codeMap.put(11,"Showers");
		codeMap.put(12,"Showers");
		codeMap.put(13,"Snow Flurries");
		codeMap.put(14,"Light Snow Showers");
		codeMap.put(15,"Blowing Snow");
		codeMap.put(16,"Snow");
		codeMap.put(17,"Hail");
		codeMap.put(18,"Sleet");
		codeMap.put(19,"Dust");
		codeMap.put(20,"Foggy");
		codeMap.put(21,"Haze");
		codeMap.put(22,"Smoky");
		codeMap.put(23,"Blustery");
		codeMap.put(24,"Windy");
		codeMap.put(25,"Cold");
		codeMap.put(26,"Cloudy");
		codeMap.put(27,"Mostly Cloudy"); //night
		codeMap.put(28,"Mostly Cloudy");
		codeMap.put(29,"Partly Cloudy"); //night
		codeMap.put(30,"Partly Cloudy");
		codeMap.put(31,"Clear"); //night
		codeMap.put(32,"Sunny");
		codeMap.put(33,"Fair"); //night
		codeMap.put(34,"Fair");
		codeMap.put(35,"Mixed Rain and Hail");
		codeMap.put(36,"Hot");
		codeMap.put(37,"Isolated Thunderstorms");
		codeMap.put(38,"Scattered Thunderstorms");
		codeMap.put(39,"Scattered Thunderstorms");
		codeMap.put(40,"Scattered Showers");
		codeMap.put(41,"Heavy Snow");
		codeMap.put(42,"Scattered Snow Showers");
		codeMap.put(43,"Heavy Snow");
		codeMap.put(44,"Partly Cloudy");
		codeMap.put(45,"Thundershowers");
		codeMap.put(46,"Snow Showers");
		codeMap.put(47,"Isolated Thundershowers");
		codeMap.put(3200,"Unknown");		
	}
	
	private static HashMap<String, String> API_IMPL = new HashMap<String, String>(); 
	private static HashMap<String, String> API_IMPL_NAME = new HashMap<String, String>(); 
	static {
		API_IMPL.put("yahoo", YahooWeatherSupport2.class.getName());
		API_IMPL_NAME.put("yahoo", "Yahoo! Weather");

                // google is no longer in use so has been removed from the implementations
		//API_IMPL.put("google", "sagex.phoenix.weather.google.GoogleWeatherSupport");
		//API_IMPL_NAME.put("google", "Google/NWS");

                // wunderground requires 3rd party libs (googleweather.jar) that may not be installed, so
		// we have to specify it's class as a string
		API_IMPL.put("wunderground", "sagex.phoenix.weather.wunderground.WundergroundWeatherSupport");
		API_IMPL_NAME.put("wunderground", "Weather Underground");
		API_IMPL.put("world", WorldWeatherWeatherSupport.class.getName());
		API_IMPL_NAME.put("world", "World Weather");
	}
	private static final String API_IMPL_PROP = "phoenix/weather/weatherSupport";
	private static final String API_IMPL_DEFAULT = "yahoo";
	public WeatherAPI2() {
		try {
			String prop = (String) Configuration.GetProperty(API_IMPL_PROP,	API_IMPL_DEFAULT);
                        //use the default if the result is google as google is no longer available
                        if (prop.equals("google")){
                            log.warn("Google weather called but is no longer available - defaulting to: "
					+ YahooWeatherSupport.class.getName());
                            api = new YahooWeatherSupport2();
                        }else{
                            api = (IWeatherSupport2) Class.forName(API_IMPL.get(prop)).newInstance();
                        }

		} catch (Throwable e) {
			log.warn("Failed to load weather support class; defaulting to: "
					+ YahooWeatherSupport.class.getName(), e);
			api = new YahooWeatherSupport2();
		}
	}

	/**
	 * Sets the current weather Implementation by name, 'yahoo', 'google', 'wunderground' or 'world'
	 * This change is persistent, and the 'phoenix/weather/weatherSupport'
	 * property will be set to the new value
	 * 
	 * @param implName
	 * @return null if the api could not be set
	 */
	public IWeatherSupport2 SetWeatherImpl(String implName) {
                //handle good as it is no longer available
                if (implName.equals("google")){
                    log.warn("Google weather called but is no longer available - returning null");
                    return null;
                }else{
                    try {
                            api = (IWeatherSupport2) Class.forName(API_IMPL.get(implName)).newInstance();
                            Configuration.SetProperty(API_IMPL_PROP, implName);
                            return api;
                    } catch (Throwable e) {
                            log.warn("Failed to load weather support: " + implName, e);
                    }
                    return null;
                }
	}

	/**
	 * Get the list of keys used to set an implementation by name, 'yahoo', 'google', 'wunderground' or 'world'
	 * 
	 * @return collection of impl keys
	 */
	public  ArrayList<String> GetWeatherImplKeys() {
            return new ArrayList<String>(API_IMPL_NAME.keySet());
        }
        
	/**
	 * Get the impl name for a specific impl key ('yahoo', 'google', 'wunderground' or 'world')
	 * 
	 * @return name of specific impl key
	 */
	public  String GetWeatherImplName(String key) {
            if (API_IMPL_NAME.containsKey(key)){
                return API_IMPL_NAME.get(key);
            }else{
                return API_IMPL_DEFAULT;
            }
        }
        
	/**
	 * Get the impl name for the current impl key ('yahoo', 'google', 'wunderground' or 'world')
	 * 
	 * @return name of current impl key
	 */
	public  String GetWeatherImplName() {
            String key = (String) Configuration.GetProperty(API_IMPL_PROP, API_IMPL_DEFAULT);
            if (API_IMPL_NAME.containsKey(key)){
                return API_IMPL_NAME.get(key);
            }else{
                return API_IMPL_NAME.get(API_IMPL_DEFAULT);
            }
        }
        
	/**
	 * Get the impl key for the current impl key ('yahoo', 'google', 'wunderground' or 'world')
	 * 
	 * @return current impl key
	 */
	public  String GetWeatherImplKey() {
            String key = (String) Configuration.GetProperty(API_IMPL_PROP, API_IMPL_DEFAULT);
            if (API_IMPL.containsKey(key)){
                return key;
            }else{
                return API_IMPL_DEFAULT;
            }
        }
        
	/**
	 * Sets the impl key property if valid ('yahoo', 'google', 'wunderground' or 'world')
	 * 
	 * @return true if the impl key was valid and set
	 */
	public boolean SetWeatherImplKey(String key) {
            if (API_IMPL.containsKey(key)){
                Configuration.SetProperty(API_IMPL_PROP, key);
                return true;
            }else{
                return false;
            }
        }
        
	/**
	 * Forces the current and forecasted weather to update.  It is up to the implementation to 
	 * ensure that weather updates are cached.  If the weather is updated since the last call
	 * then it will return true.  If an Error happens, then IsError will return true and GetError
	 * will contain the failure message.
	 * 
	 * @return true if the weather was updated
	 */
	public boolean Update() {
		// clear out the periods
		if (periods!=null) {
			periods.clear();
			periods = null;
		}
		
		return api.update();
	}
	
	/**
	 * Sets the user's location based on the implementations location handling. 
         * This may be a Postal Code, Zip Code, City Name or other.  
	 * The implementation should try to convert the location into something that
	 * can be consumed by the implementation.  For example, the Yahoo Weather Service doesn't use
	 * zip code so it will convert it into the Yahoo WOEID.  If the implementation can't convert
	 * the location then this method will return false.
	 * 
	 * @param location ZIP, Postal Code, City Name, Country
	 * @return true if the implementation accepted the location.
	 */
	public boolean SetLocation(String location) {
		return api.setLocation(location);
	}

	/**
	 * Get the current weather location's location code.  
         * this may be a ZIP, Postal Code, unique ID etc dependent on the implementation
         * It may return null, if the weather hasn't been configured.
	 * 
	 * @return location
	 */
	public String GetLocation() {
		return api.getLocation();
	}
	
        /**
         * Clears the location from the weather source.
         * 
         * @return 
         */
        public void removeLocation() {
            api.removeLocation();
        }

        /**
	 * Set the Unit for the weather service.  Valid values are 'm' for Metric, and 's' for Standard (imperial) units.
	 * 
	 * @param units
	 */
	public void SetUnits(String units) {
		IWeatherSupport2.Units u = null;
		if (units==null) u = Units.Metric;
		if (units.toLowerCase().startsWith("m")) { 
			u = Units.Metric;
		} else {
			u = Units.Standard;
		}
		
		api.setUnits(u);
	}
	
	/**
	 * Return the configured units for the Weather Service
	 * 
	 * @return
	 */
	public String GetUnits() {
		IWeatherSupport2.Units u = api.getUnits();
		if (u==null) u=Units.Metric;
		return u.name();
	}
		
	/**
	 * Return true if the Weather Service is configured.
	 * 
	 * @return true if configured
	 */
	public boolean IsConfigured() {
		return api.isConfigured();
	}
	
	/**
	 * Returns the {@link Date} the weather was last updated.
	 * 
	 * @return {@link Date} of last update
	 */
	public Date GetLastUpdated() {
		return api.getLastUpdated();
	}
	
	/**
	 * Returns the {@link Date} the weather was recorded.
	 * 
	 * @return {@link Date} weather was recorded
	 */
	public Date GetRecordedDate() {
		return api.getRecordedDate();
	}
	
	/**
	 * Returns the location name (usually the City) if known.  This may be null until an
	 * update happens.
	 * 
	 * @return location name, usually the city
	 */
	public String GetLocationName() {
		return api.getLocationName();
	}
	
	/**
	 * Return true if there was a Weather Service error
	 * 
	 * @return true if error
	 */
	public boolean HasError() {
		return api.hasError();
	}
	
	/**
	 * Returns the error if HasError return true, otherwise it will return null.
	 * 
	 * @return
	 */
	public String GetError() {
		return api.getError();
	}
	
	public ICurrentForecast GetCurrentWeather() {
		return api.getCurrentWeather();
	}
	
	public int GetForecastDays() {
		return api.getForecastDays();
	}
	
	public int GetForecastPeriodCount() {
		if (periods == null) return 0;
		return periods.size();
	}
	
	/**
	 * Gets the specific day number for a ForecastPeriod
	 * 
	 * @param IForecastPeriod period
	 * @return int day number, defaults to 0
	 */
	public int GetForecastDay(IForecastPeriod period) {
            if (period==null){
                return 0;
            }
            int periodIndex = GetForecastPeriods().indexOf(period);
            if (periodIndex==-1){
                return 0;
            }
            if (HasTodaysHigh()){
                return periodIndex/2;
            }else{
                return (periodIndex+1)/2;
            }
	}
	
	public  List<IForecastPeriod> GetForecastPeriods(int MaxPeriods) {
            List<IForecastPeriod> tPeriods = new ArrayList<IForecastPeriod>();
            int counter = 0;
            for (IForecastPeriod fp:GetForecastPeriods()){
                counter++;
                if (counter>MaxPeriods){
                    break;
                }
                tPeriods.add(fp);
            }
            return tPeriods;
        }
        
	public  List<IForecastPeriod> GetForecastPeriods() {
		if (periods==null) {
			periods=new ArrayList<IForecastPeriod>();
			if (GetForecasts()!=null) {
				for (ILongRangeForecast lr : GetForecasts()) {
					IForecastPeriod p = lr.getForecastPeriodDay();
					
					// TOOO: check if valid
					if (p!=null) {
						periods.add(p);
					}
					
					p=lr.getForecastPeriodNight();
					
					// TODO: check if valid
					if (p!=null) {
						periods.add(p);
					}
				}
			}
		}
		
		return periods;
	}
	
	public  List<ILongRangeForecast> GetForecasts(int MaxDays) {
            List<ILongRangeForecast> tDays = new ArrayList<ILongRangeForecast>();
            int counter = 0;
            for (ILongRangeForecast lrf:GetForecasts()){
                counter++;
                if (counter>MaxDays){
                    break;
                }
                tDays.add(lrf);
            }
            
            return tDays;
        }
        
	public List<ILongRangeForecast> GetForecasts() {
		return api.getForecasts();
	}
	
	/**
	 * Gets the LongRangForecast for a specific day
	 * 
	 * @param int day
	 * @return ILongRangeForecast
	 */
	public  ILongRangeForecast GetForecast(int day) {
            if (api.getForecasts()==null){
                return null;
            }
            if (api.getForecasts().size()>day){
                return api.getForecasts().get(day);
            }
            return null;
        }
        
       /**
         * Returns the forecast data for this specific forecast day
         * 
	 * @param int day
         * @return ForecastPeriod for the Day

        */
       public IForecastPeriod GetForecastPeriodDay(int day) {
            if (api.getForecasts().size()>day){
                return api.getForecasts().get(day).getForecastPeriodDay();
            }
            return null;
       }
        
       /**
         * Returns the forecast data for this specific forecast day
         * 
	 * @param int day
         * @return ForecastPeriod for the Night

        */
       public IForecastPeriod GetForecastPeriodNight(int day) {
            if (api.getForecasts().size()>day){
                return api.getForecasts().get(day).getForecastPeriodNight();
            }
            return null;
       }
        
       /**
         * Returns the best forecast period for this specific forecast day
         * 
	 * @param int day
         * @return ForecastPeriod for the Day unless null then uses the Night

        */
       public IForecastPeriod GetForecastPeriodSingle(int day) {
            if (api.getForecasts().size()>day){
                if (api.getForecasts().get(day).getForecastPeriodDay()==null){
                   return api.getForecasts().get(day).getForecastPeriodNight();
                }
                return api.getForecasts().get(day).getForecastPeriodDay();
            }
            return null;
       }
        
       /**
         * Returns the best forecast period for this specific long range forecast day
         * 
	 * @param int day
         * @return ForecastPeriod for the Day unless null then uses the Night

        */
       public IForecastPeriod GetForecastPeriodSingle(ILongRangeForecast ilongrangeforecast) {
           if (ilongrangeforecast==null){
               return null;
           }
           if (ilongrangeforecast.getForecastPeriodDay()==null){
               return ilongrangeforecast.getForecastPeriodNight();
           }
           return ilongrangeforecast.getForecastPeriodDay();
       }
        
	/**
	 * Gets the ForecastPeriod for a specific period
	 * 
	 * @param int period number
	 * @return IForecastPeriod
	 */
	public  IForecastPeriod GetForecastPeriod(int period) {
            if (GetForecastPeriods()==null){
                return null;
            }
            if (GetForecastPeriods().size()>period){
                return GetForecastPeriods().get(period);
            }
            return null;
        }
        
	public String GetSourceName() {
		return api.getSourceName();
	}
	
	public String GetFormattedTemp(Object temp) {
		if (temp==null) return "N/A";
		
		if (Units.Metric.name().equals(GetUnits())) {
			return temp + " C"; 
		} else {
			return temp + " F";
		}		
	}
		
	public String GetFormattedSpeed(Object speed) {
		if (speed==null) return "N/A";
		if (Units.Metric.name().equals(GetUnits())) {
			return speed + " k/h"; 
		} else {
			return speed + " mph";
		}		
	}
    
	public String GetFormattedVisibility(Object visibility) {
		if (visibility==null) return "N/A";
		if (Units.Metric.name().equals(GetUnits())) {
			return visibility + " km"; 
		} else {
			return visibility + " mi";
		}		
	}
	
	/**
	 * Convenience function to return formatted wind information
         * examples - "Calm" or SW/5 mph
	 * 
	 * @param IForcastPeriod, separator string
	 * @return wind info formatted
	 */
        public String GetFormattedWind(IForecastPeriod forecastperiod, String separator){
            if (forecastperiod==null){
                return "";
            }
            if (forecastperiod.getWindDirText().toLowerCase().equals("calm")){
                return forecastperiod.getWindDirText();
            }else{
                //make sure this is supported and valid
                String retVal = "";
                if (IsSupported(forecastperiod.getWindSpeed()) && IsValid(forecastperiod.getWindSpeed())){
                    retVal = GetFormattedSpeed(forecastperiod.getWindSpeed());
                    if (IsSupported(forecastperiod.getWindDirText()) && IsSupported(forecastperiod.getWindDirText())){
                        retVal = forecastperiod.getWindDirText() + separator + retVal;
                    }
                }
                return retVal;
            }
        }

	public String GetDay(Date date) {
		if (date==null) return "Unknown";
		SimpleDateFormat df = new SimpleDateFormat("E");
		return df.format(date);
	}
	
        /**
         * Returns the short day name for this specific day
         *  example - (Mon, Tue, Wed, etc)
         * 
         * @return period day name
         */
        public String GetDay(ILongRangeForecast longrangeforecast){
            if (longrangeforecast==null){
                return "N/A";
            }
            if (longrangeforecast.getForecastPeriodDay()==null){
                return GetDay(longrangeforecast.getForecastPeriodNight());
            }
            return GetDay(longrangeforecast.getForecastPeriodDay());
        }

        /**
         * Returns the full day name for this specific day
         *  example - (Today, Tonight, Monday, Monday Night, Tuesday, etc)
         * 
         * @return period full day name
         */
        public String GetDayFull(ILongRangeForecast longrangeforecast){
            if (longrangeforecast==null){
                return "N/A";
            }
            if (longrangeforecast.getForecastPeriodDay()==null){
                return GetDayFull(longrangeforecast.getForecastPeriodNight());
            }
            return GetDayFull(longrangeforecast.getForecastPeriodDay());
        }

	/**
	 * Return the Date for the specific Day
	 * 
	 * @return Day Date
	 */
        public Date GetDate(ILongRangeForecast longrangeforecast){
            if (longrangeforecast==null){
                return null;
            }
            if (longrangeforecast.getForecastPeriodDay()==null){
                return longrangeforecast.getForecastPeriodNight().getDate();
            }
            return longrangeforecast.getForecastPeriodDay().getDate();
        }

	/**
	 * Return the High temp for a specific Day
	 * 
	 * @return Day High temp
	 */
        public int GetHigh(ILongRangeForecast longrangeforecast){
            if (longrangeforecast==null || longrangeforecast.getForecastPeriodDay()==null){
                return IForecastPeriod.iInvalid;
            }
            return longrangeforecast.getForecastPeriodDay().getTemp();
        }

	/**
	 * Return the Low temp for a specific Day
	 * 
	 * @return Day Low temp
	 */
        public int GetLow(ILongRangeForecast longrangeforecast){
            if (longrangeforecast==null || longrangeforecast.getForecastPeriodNight()==null){
                return IForecastPeriod.iInvalid;
            }
            return longrangeforecast.getForecastPeriodNight().getTemp();
        }

	/**
	 * Determine if the first forecast day has a Day record with a valid High temp
	 * 
	 * @return
	 */
        public boolean HasTodaysHigh(){
            if (GetForecasts()==null) {
                return false;
            }
            if (GetForecasts().get(0).getForecastPeriodDay()==null){
                return false;
            }
            if (GetForecasts().get(0).getForecastPeriodDay().getTemp()==IForecastPeriod.iInvalid){
                return false;
            }
            return true;
        }

	/**
	 * Return the Day Type Name for the period
         * can be Day, Night or Current
	 * 
	 * @return Day Type as string
	 */
        public String GetTypeName(IForecastPeriod forecastperiod){
            if (forecastperiod==null){
                return "N/A";
            }
            return forecastperiod.getType().name();
        }

	/**
	 * Return the Day Name for the period
         * will be 3 character name - Mon, Tue, Wed, etc
	 * 
	 * @return Short Day Name
	 */
        public String GetDay(IForecastPeriod forecastperiod){
            if (forecastperiod==null){
                return "N/A";
            }
            return getDayName(forecastperiod.getDate());
        }

	/**
	 * Return the Long Day Name for the period
         * will be - Today, Tonight, Monday, Monday Night, etc
	 * 
	 * @return Full Day Name
	 */
        public String GetDayFull(IForecastPeriod forecastperiod){
            if (forecastperiod==null){
                return "N/A";
            }
            String tName = getDayNameFull(forecastperiod.getDate());
            if (forecastperiod.getType().equals(IForecastPeriod.Type.Night)){
                if (tName.equals("Today")){
                    return "Tonight";
                }else{
                    return tName + " Night";
                }
            }else{
                return tName;
            }
        }

        private String getDayName(Date Day){
            Calendar thisDay = Calendar.getInstance();
            thisDay.setTime(Day);
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE");
            return dateFormat.format(thisDay.getTime());
        }
        private String getDayNameFull(Date Day){
            boolean isToday = false;
            Calendar Today = Calendar.getInstance();
            Today.setTime(Calendar.getInstance().getTime());
            Calendar thisDay = Calendar.getInstance();
            thisDay.setTime(Day);
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE");
            if (Today.get(Calendar.ERA) == thisDay.get(Calendar.ERA) &&
                Today.get(Calendar.YEAR) == thisDay.get(Calendar.YEAR) &&
                Today.get(Calendar.DAY_OF_YEAR) == thisDay.get(Calendar.DAY_OF_YEAR)){
                return "Today";
            }else{
                return dateFormat.format(thisDay.getTime());
            }
        }
    
	/**
	 * Return the temp type text for the periods temperature
         * can be Low, High or Now
	 * 
	 * @return temp type text
	 */
        public String GetTempType(IForecastPeriod forecastperiod){
            if (forecastperiod==null){
                return "N/A";
            }
            if (forecastperiod.getType().equals(sagex.phoenix.weather.IForecastPeriod.Type.Day)){
                return "High";
            }else if (forecastperiod.getType().equals(sagex.phoenix.weather.IForecastPeriod.Type.Night)){
                return "Low";
            }else{
                return "Now";
            }
        }

	/**
	 * Return the text condition for the specific passed in code
	 * 
	 * @return weather code condition
	 */
        public String GetCodeText(int code){
            if (codeMap.containsKey(code)){
                return codeMap.get(code);
            }else{
                return "Unknown";
            }
        }

	/**
	 * Return the text condition for the specific forecast period
	 * 
	 * @return weather code condition
	 */
        public String GetCodeText(IForecastPeriod forecastperiod){
            if (forecastperiod==null){
                return "Unknown";
            }
            if (codeMap.containsKey(forecastperiod.getCode())){
                return codeMap.get(forecastperiod.getCode());
            }else{
                return "Unknown (" + forecastperiod.getCode() + ")";
            }
        }

	/**
	 * Return the weather day code for the specific passed in code - even if it is a night code
	 * 
	 * @return weather code for day
	 */
        public int GetCodeForceDay(int code){
            if (dayCodeMap.containsKey(code)){
                return dayCodeMap.get(code);
            }else{
                return code;
            }
        }

        /**
	 * Return the weather night code for the specific passed in code - even if it is a day code
	 * 
	 * @return weather code for night
	 */
        public int GetCodeForceNight(int code){
            if (nightCodeMap.containsKey(code)){
                return nightCodeMap.get(code);
            }else{
                return code;
            }
        }
        
        public boolean IsValid(int object){
            if (object==IForecastPeriod.iInvalid){
                return false;
            }
            return true;
        }
        public boolean IsValid(String object){
            if (object.equals(IForecastPeriod.sInvalid)){
                return false;
            }
            return true;
        }
        public boolean IsSupported(int object){
            if (object==IForecastPeriod.iNotSupported){
                return false;
            }
            return true;
        }
        public boolean IsSupported(String object){
            if (object.equals(IForecastPeriod.sNotSupported)){
                return false;
            }
            return true;
        }
        public boolean IsDay(IForecastPeriod forecastperiod){
            if (forecastperiod==null){
                return false;
            }
            if (forecastperiod.getType().equals(IForecastPeriod.Type.Day)){
                return true;
            }
            return false;
        }
        public boolean IsNight(IForecastPeriod forecastperiod){
            if (forecastperiod==null){
                return false;
            }
            if (forecastperiod.getType().equals(IForecastPeriod.Type.Night)){
                return true;
            }
            return false;
        }
        public boolean IsCurrent(IForecastPeriod forecastperiod){
            if (forecastperiod==null){
                return false;
            }
            if (forecastperiod.getType().equals(IForecastPeriod.Type.Current)){
                return true;
            }
            return false;
        }

}
