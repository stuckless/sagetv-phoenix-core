package sagex.phoenix.weather.yahoo;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import sagex.phoenix.json.JSON;
import sagex.phoenix.util.url.UrlUtil;
import sagex.phoenix.weather.*;
import sagex.phoenix.weather.IForecastPeriod.Type;
import sagex.remote.json.JSONException;
import sagex.remote.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Parses Weather Json for Yahoo
 * - updated by jusjoken Jan 13, 2019 as Yahoo moved to OAuth 1.0a protected solution
 */
public class YahooWeatherJsonHandler {
    private Logger log = Logger.getLogger(this.getClass());
    private static String appId = "fPhtIv56";
    private static String consumerKey = "dj0yJmk9U0VZbktDM2F2NEY2JnM9Y29uc3VtZXJzZWNyZXQmc3Y9MCZ4PWM0";
    private static String consumerSecret = "2b55b824db4343b0ca184c9576e109178946ec0b";
    private static String url = "https://weather-ydn-yql.media.yahoo.com/forecastrss";

    private static HashMap<String, String> codeMap = new HashMap<String, String>();

    static {
        codeMap.put("-1", "Unknown");
        codeMap.put("0", "tornado");
        codeMap.put("1", "tropical storm");
        codeMap.put("2", "hurricane");
        codeMap.put("3", "severe thunderstorms");
        codeMap.put("4", "thunderstorms");
        codeMap.put("5", "mixed rain and snow");
        codeMap.put("6", "mixed rain and sleet");
        codeMap.put("7", "mixed snow and sleet");
        codeMap.put("8", "freezing drizzle");
        codeMap.put("9", "drizzle");
        codeMap.put("10", "freezing rain");
        codeMap.put("11", "showers");
        codeMap.put("12", "showers");
        codeMap.put("13", "snow flurries");
        codeMap.put("14", "light snow showers");
        codeMap.put("15", "blowing snow");
        codeMap.put("16", "snow");
        codeMap.put("17", "hail");
        codeMap.put("18", "sleet");
        codeMap.put("19", "dust");
        codeMap.put("20", "foggy");
        codeMap.put("21", "haze");
        codeMap.put("22", "smoky");
        codeMap.put("23", "blustery");
        codeMap.put("24", "windy");
        codeMap.put("25", "cold");
        codeMap.put("26", "cloudy");
        codeMap.put("27", "mostly cloudy (night)");
        codeMap.put("28", "mostly cloudy (day)");
        codeMap.put("29", "partly cloudy (night)");
        codeMap.put("30", "partly cloudy (day)");
        codeMap.put("31", "clear (night)");
        codeMap.put("32", "sunny");
        codeMap.put("33", "fair (night)");
        codeMap.put("34", "fair (day)");
        codeMap.put("35", "mixed rain and hail");
        codeMap.put("36", "hot");
        codeMap.put("37", "isolated thunderstorms");
        codeMap.put("38", "scattered thunderstorms");
        codeMap.put("39", "scattered thunderstorms");
        codeMap.put("40", "scattered showers");
        codeMap.put("41", "heavy snow");
        codeMap.put("42", "scattered snow showers");
        codeMap.put("43", "heavy snow");
        codeMap.put("44", "partly cloudy");
        codeMap.put("45", "thundershowers");
        codeMap.put("46", "snow showers");
        codeMap.put("47", "isolated thundershowers");
        codeMap.put("3200", "not available");
    }

    private String city, region, country, timezone_id;
    private Double dLat, dLong;
    private String windChill, windSpeed, windDirection;
    private String humidity, visibility, pressure, rising;
    private String sunrise, sunset;
    private String pubDate;
    private Date recordedDate;

    private CurrentForecast current;
    private List<ILongRangeForecast> days = new ArrayList<ILongRangeForecast>();

    public YahooWeatherJsonHandler() {
    }

    public boolean LatLongFromLocation(String CityorPostalCode) {
        boolean validLatLong = false;
        dLat = IForecastPeriod.dInvalid;
        dLong = IForecastPeriod.dInvalid;
        String data = getYahooOAuthResponse(CityorPostalCode,"f");
        if (data != null) {
            JSONObject channel = null;
            try {
                channel = new JSONObject(data);
            } catch (JSONException e) {
                log.info("JSON Response for LatLongFromLocation did not contain a valid response");
                e.printStackTrace();
            }
            if (channel != null) {
                JSONObject location = JSON.get("location", channel);
                city = JSON.get("city", location);
                region = JSON.get("region", location);
                country = JSON.get("country", location);
                dLat = JSON.get("lat", location);
                dLong = JSON.get("long", location);
                timezone_id = JSON.get("timezone_id", location);
                validLatLong = true;
            }

        }
        return validLatLong;
    }

    public void parse(String urlLocation, String urlUnits) throws JSONException {
        String data = getYahooOAuthResponse(urlLocation,urlUnits);
        if (data == null) throw new JSONException("YahooOAuthResponse Weather did not contain a valid response");
        JSONObject channel = new JSONObject(data);
        if (channel == null) throw new JSONException("JSON Response for Weather did not contain a valid response");

        JSONObject location = JSON.get("location", channel);
        city = JSON.get("city", location);
        region = JSON.get("region", location);
        country = JSON.get("country", location);
        dLat = JSON.get("lat", location);
        dLong = JSON.get("long", location);
        timezone_id = JSON.get("timezone_id", location);

        JSONObject current_observation = JSON.get("current_observation", channel);

        JSONObject wind = JSON.get("wind", current_observation);
        windChill = JSON.getString("chill", wind);
        windSpeed = JSON.getString("speed", wind);
        windDirection = JSON.getString("direction", wind);

        JSONObject atmosphere = JSON.get("atmosphere", current_observation);
        humidity = JSON.getString("humidity", atmosphere);
        visibility = JSON.getString("visibility", atmosphere);
        pressure = JSON.getString("pressure", atmosphere);
        rising = JSON.getString("rising", atmosphere);

        JSONObject astronomy = JSON.get("astronomy", current_observation);
        sunrise = JSON.get("sunrise", astronomy);
        sunset = JSON.get("sunset", astronomy);

        pubDate = JSON.getString("pubDate", current_observation);
        recordedDate = convertYahooDate(pubDate);

        JSONObject condition = JSON.get("condition", current_observation);
        current = new CurrentForecast();
        current.setType(Type.Current);
        current.setCloudCover(IForecastPeriod.sNotSupported);
        current.setDescription(IForecastPeriod.sNotSupported);
        current.setDewPoint(IForecastPeriod.sNotSupported);
        current.setPrecip(IForecastPeriod.sNotSupported);
        current.setUVIndex(IForecastPeriod.sNotSupported);
        current.setUVWarn(IForecastPeriod.sNotSupported);
        current.setCode(JSON.getInt("code", condition, IForecastPeriod.iInvalid));
        current.setCondition(JSON.getString("text", condition));
        current.setDate(convertYahooDate(pubDate));
        current.setFeelsLike(NumberUtils.toInt(windChill, IForecastPeriod.iInvalid));
        current.setHumid(NumberUtils.toInt(humidity, IForecastPeriod.iInvalid));
        current.setPressure(pressure);
        current.setPressureDir(NumberUtils.toInt(rising, IForecastPeriod.iInvalid));
        current.setSunrise(sunrise);
        current.setSunset(sunset);
        current.setTemp(JSON.getInt("temperature", condition, IForecastPeriod.iInvalid));
        current.setVisibility(formatVisibility(visibility));
        current.setWindSpeed((int) Math.round(NumberUtils.toDouble(windSpeed, IForecastPeriod.iInvalid)));

        if (current.getWindSpeed() == 0) {
            current.setWindDir(IForecastPeriod.iInvalid);
            current.setWindDirText(IForecastPeriod.WindCalm);
        } else {
            current.setWindDir(NumberUtils.toInt(windDirection, IForecastPeriod.iInvalid));
            current.setWindDirText(formatCompassDirection(current.getWindDir()));
        }

        JSON.each("forecasts", channel, new JSON.ArrayVisitor() {
            public void visitItem(int i, JSONObject item) {
                LongRangForecast r = new LongRangForecast();
                ForecastPeriod day = new ForecastPeriod();
                ForecastPeriod night = new ForecastPeriod();
                r.setForecastPeriodDay(day);
                r.setForecastPeriodNight(night);

                days.add(r);

                day.setCode(JSON.getInt("code", item, -1));
                day.setCondition(JSON.getString("text", item));
                day.setDate(convertYahooDate(JSON.getString("date", item)));
                day.setDescription(IForecastPeriod.sNotSupported);
                day.setHumid(IForecastPeriod.iNotSupported);
                day.setPrecip(IForecastPeriod.sNotSupported);
                day.setTemp(JSON.getInt("high", item, Integer.MAX_VALUE));
                day.setType(Type.Day);
                day.setWindDir(IForecastPeriod.iNotSupported);
                day.setWindDirText(IForecastPeriod.sNotSupported);
                day.setWindSpeed(IForecastPeriod.iNotSupported);

                night.setCode(phoenix.weather2.GetCodeForceNight(JSON.getInt("code", item, -1)));
                night.setCondition(JSON.getString("text", item));
                night.setDate(convertYahooDate(JSON.getString("date", item)));
                night.setDescription(IForecastPeriod.sNotSupported);
                night.setHumid(IForecastPeriod.iNotSupported);
                night.setPrecip(IForecastPeriod.sNotSupported);
                night.setTemp(JSON.getInt("low", item, Integer.MAX_VALUE));
                night.setType(Type.Night);
                night.setWindDir(IForecastPeriod.iNotSupported);
                night.setWindDirText(IForecastPeriod.sNotSupported);
                night.setWindSpeed(IForecastPeriod.iNotSupported);

            }
        });
    }


    public static HashMap<String, String> getCodeMap() {
        return codeMap;
    }

    public String getCity() {
        return city;
    }

    public String getRegion() {
        return region;
    }

    public String getCountry() {
        return country;
    }

    public String getWindChill() {
        return windChill;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getPressure() {
        return pressure;
    }

    public String getRising() {
        return rising;
    }

    public String getSunrise() {
        return sunrise;
    }

    public String getSunset() {
        return sunset;
    }

    public Date getRecordedDate() {
        return recordedDate;
    }

    public Double getLat() {
        return dLat;
    }

    public Double getLong() {
        return dLong;
    }

    public List<ILongRangeForecast> getDays() {
        return days;
    }

    public CurrentForecast getCurrent() {
        return current;
    }

    private int formatVisibility(String Vis) {
        Double vis = NumberUtils.toDouble(Vis, IForecastPeriod.iInvalid);
        if (vis == IForecastPeriod.iInvalid)
            return IForecastPeriod.iInvalid;
        return (int) Math.round(vis);
    }

    private String formatCompassDirection(int degrees) {
        String[] directions = new String[]{"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW",
                "NW", "NNW"};
        int index = (int) ((degrees / 22.5) + .5);
        return directions[index % 16];
    }

    private Date convertYahooDate(String inDate){
        return new java.util.Date(Long.parseLong(inDate)*1000);
    }

    private String getYahooOAuthResponse(String urlLocation, String urlUnits){
        final Charset UTF8_CHARSET = Charset.forName("UTF-8");

        long timestamp = new Date().getTime() / 1000;
        byte[] nonce = new byte[32];
        Random rand = new Random();
        rand.nextBytes(nonce);
        String oauthNonce = new String(nonce).replaceAll("\\W", "");

        List<String> parameters = new ArrayList<>();
        parameters.add("oauth_consumer_key=" + consumerKey);
        parameters.add("oauth_nonce=" + oauthNonce);
        parameters.add("oauth_signature_method=HMAC-SHA1");
        parameters.add("oauth_timestamp=" + timestamp);
        parameters.add("oauth_version=1.0");
        // Make sure value is encoded
        parameters.add("location=" + UrlUtil.encode(urlLocation));
        parameters.add("format=json");
        parameters.add("u=" + urlUnits);
        Collections.sort(parameters);

        StringBuffer parametersList = new StringBuffer();
        for (int i = 0; i < parameters.size(); i++) {
            parametersList.append(((i > 0) ? "&" : "") + parameters.get(i));
        }

        String signatureString = "GET&" +
                UrlUtil.encode(url) + "&" +
                UrlUtil.encode(parametersList.toString());

        String signature = null;
        try {
            SecretKeySpec signingKey = new SecretKeySpec((consumerSecret + "&").getBytes(), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] rawHMAC = mac.doFinal(signatureString.getBytes());
            Base64.Encoder encoder = Base64.getEncoder();
            signature = encoder.encodeToString(rawHMAC);
        } catch (Exception e) {
            log.error("Unable to append signature");
            System.exit(0);
        }

        String authorizationLine = "OAuth " +
                "oauth_consumer_key=\"" + consumerKey + "\", " +
                "oauth_nonce=\"" + oauthNonce + "\", " +
                "oauth_timestamp=\"" + timestamp + "\", " +
                "oauth_signature_method=\"HMAC-SHA1\", " +
                "oauth_signature=\"" + signature + "\", " +
                "oauth_version=\"1.0\"";

        HttpClient client = HttpClientBuilder.create().build();
        URI uri = URI.create(url + "?location=" + UrlUtil.encode(urlLocation) + "&u=" + urlUnits + "&format=json");

        HttpUriRequest request = new HttpGet(uri);
        request.addHeader("Authorization", authorizationLine);
        request.addHeader("Yahoo-App-Id", appId);
        request.addHeader("Content-Type", "application/json");
        try {
            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode()!=200){
                log.error("getYahooOAuthResponse: invalid response: code '" + response.getStatusLine().getStatusCode() + "' " + response);
                return null;
            }
            String responseJSON = EntityUtils.toString(response.getEntity(), UTF8_CHARSET);
            log.info("responseJSON:" + responseJSON);
            return responseJSON;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
