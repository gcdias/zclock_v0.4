package pt.gu.zclock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static pt.gu.zclock.zcHelper.WeatherData;

/**
 * Created by GU on 31-03-2015.
 */
public class zcWeather{

    private final boolean       debug               = true;
    private final String        TAG                 = "zcWeather";
    private final String        FORECAST_URL        = "http://api.openweathermap.org/data/2.5/forecast?";

    public List<WeatherData>    weatherData;
    public float                longitude;
    public float                latitude;
    private Context             mContext;
    private SharedPreferences   mPrefs;


    public zcWeather(Context context){
        this.mContext = context.getApplicationContext();
        weatherData = new ArrayList<>();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        this.latitude = mPrefs.getFloat("latitude",0f);
        this.longitude = mPrefs.getFloat("longitude",0f);
        JSONForecastTask task = new JSONForecastTask();
        task.execute();
    }

    public boolean updateForecast(double latitude, double longitude){
        boolean res = true;
        this.latitude = (float) latitude;
        this.longitude= (float) longitude;
        if (weatherData.size()!=0) {
            List<WeatherData> backup = new ArrayList<>(weatherData);
            weatherData.clear();
            JSONForecastTask task = new JSONForecastTask();
            task.execute();
            if (weatherData.size() == 0) {
                weatherData = new ArrayList<>(backup);
                res = false;
            }
            backup.clear();
        } else {
            JSONForecastTask task = new JSONForecastTask();
            task.execute();
            if (weatherData.size() == 0) {

                res = false;
            }
        }
        if (res) {
            SharedPreferences.Editor ed = mPrefs.edit();
            ed.putLong("lastForecastUpdate",System.currentTimeMillis());
            ed.apply();
            Toast.makeText(mContext, "new weather data", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mContext, "weather download failed", Toast.LENGTH_LONG).show();
        }
        return res;
    }

    public WeatherData[] get24hForecast(long startTimeMilis) {

        long dt_end     = startTimeMilis+86400000;

        if (weatherData.isEmpty()){
            if (debug) Log.d(TAG,"weatherData is Empty");
            return null;
        }
        if (debug) Log.d(TAG,"weatherData is " + weatherData.size());


        WeatherData[] tArray = new WeatherData[weatherData.size()];
        int i = 0;
        if (debug) Log.d(TAG,"dt_st is " + startTimeMilis+ "dt_end is" + dt_end);

        for (int j=0; j<weatherData.size();j++){
            WeatherData data= weatherData.get(j);
            if (data.getTime() > startTimeMilis && data.getTime() < dt_end){
                if (i==0 && j>0) {
                    tArray[0] = weatherData.get(j-1);
                    tArray[i++].setTime(System.currentTimeMillis());
                    if (debug) Log.d(TAG,"Now:" + startTimeMilis + "from " + weatherData.get(j-1).getTime());
                }
                tArray[i++]=data;
            }
            if (debug) Log.d(TAG,"Dt is " + data.getTime());
        }

        if (debug) Log.d(TAG,String.format("wData: %d values",i));
        WeatherData[] res =Arrays.copyOf(tArray,i);
        return res;
    }

    private class JSONForecastTask extends AsyncTask<String, Void, Void> {

        protected boolean updated = false;
        @Override
        protected Void doInBackground(String... params) {
            String url = String.format(Locale.US,"%slat=%.1f&lon=%.1f",FORECAST_URL,latitude,longitude);
            if (debug) Log.d("ForecastTask",url);
            String data = getWeatherData(url);

            try {
                if (data!=null) {
                    JSONParseForecast(data);
                    SharedPreferences.Editor ed = mPrefs.edit();
                    ed.putString("weatherForecast",data);
                    ed.putLong("lastForecastUpdate", System.currentTimeMillis());
                    ed.apply();
                    mContext.sendBroadcast(new Intent(zcService.ZC_FORECASTUPDATE));
                    updated = true;
                    if (debug) Log.d(TAG, "Success, intent sent");
                }
                else
                {
                    if (debug) Log.d(TAG, "Null data received");
                    updated = false;
                }

            } catch (JSONException e) {
                updated = false;
                if (debug) Log.d(TAG,"Task failed" + e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }

    }

    private String getWeatherData(String url_string) {
        HttpURLConnection con = null ;
        InputStream is = null;

        try {
            con = (HttpURLConnection)(new URL(url_string)).openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.connect();

            // Let's read the response
            StringBuffer buffer = new StringBuffer();
            is = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while (  (line = br.readLine()) != null )
                buffer.append(line + "\r\n");

            is.close();
            con.disconnect();
            return buffer.toString();
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
        finally {
            try { is.close(); } catch(Throwable t) {}
            try { con.disconnect(); } catch(Throwable t) {}
        }

        return null;
    }

    private void JSONParseForecast(String data) throws JSONException {

        // We create out JSONObject from the data
        JSONObject jData = new JSONObject(data);
        if (debug) Log.d("JSON Data", jData.toString());
        JSONArray jList = jData.getJSONArray("list");
        if (debug) Log.d("JSON List", jList.toString());

        /*
        for (int i = 0; i < jList.length(); i++) {
            WeatherData w = new WeatherData();
            w.parseJSONObject(jList.getJSONObject(i));
            weatherData.add(w);
        }*/


        for (int i=0;i<jList.length();i++){

            WeatherData w = new WeatherData();
            JSONObject jEntry = jList.getJSONObject(i);
            w.dt              = jEntry.getLong("dt");
            JSONObject jMain  = jEntry.getJSONObject("main");
            w.main_temp       = getDouble("temp",jMain);
            w.main_temp_min   = getDouble("temp_min",jMain);
            w.main_temp_max   = getDouble("temp_max",jMain);
            w.main_pressure   = getDouble("pressure",jMain);
            w.main_sea_level  = getDouble("sea_level",jMain);
            w.main_grnd_level = getDouble("grnd_level",jMain);
            w.main_humidity   = getInt("humidity",jMain);
            w.main_temp_kf    = getInt("temp_kf",jMain);
            w.weather_id      = getInt("id",jEntry.getJSONArray("weather").getJSONObject(0));
            w.clouds_all      = getInt("all",jEntry.getJSONObject("clouds"));
            w.rain_3h         = tryDouble(jEntry,"rain","3h");
            w.wind_speed      = tryDouble(jEntry,"wind","speed");
            w.wind_deg        = tryDouble(jEntry,"wind","deg");
            w.sys_pod         = getString("pod",jEntry.getJSONObject("sys"));

            weatherData.add(w);
        }

        //endregion
    }

    //region deprecated code


    private JSONObject getObject(String tagName, JSONObject jObj)  throws JSONException {
        JSONObject subObj = jObj.getJSONObject(tagName);
        return subObj;
    }
    private String getString(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getString(tagName);
    }
    private float  getFloat(String tagName, JSONObject jObj) {
        try{
            return (float) jObj.getDouble(tagName);
        }
        catch (JSONException ignore){
            return 0;
        }
    }
    private double  getDouble(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getDouble(tagName);
    }
    private double tryDouble(JSONObject jObj,String objName,String tagName){
        try{
            return jObj.getJSONObject(objName).getDouble(tagName);
        } catch (Exception ignore){
            return 0;
        }
    }
    private int  getInt(String tagName, JSONObject jObj) {
        try {
            return jObj.getInt(tagName);
        }
        catch (JSONException ignore){
            return 0;
        }
    }
    /*

    private static String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?q=";
    private static String IMG_URL = "http://openweathermap.org/img/w/";
    private String gpsProvider;
    private byte[] icondata;
    public Date sunrise;
    public Date sunset;
    public int updatePeriodMilis = 30*60000;
    public float altitude;
    public long  lastgpsupdatemilis=-1;
    public String country;
    public String city;
    public int weatherId;
    public String condition;
    public String descr;
    public String icon;
    public float pressure;
    public float humidity;
    public float temp;
    public float minTemp;
    public float maxTemp;
    public int sealevel;
    public int groundlevel;
    public float windspeed;
    public float winddeg;
    public float windgust;
    public String raintime;
    public float rainammount;
    public String snowtime;
    public float snowammount;
    public int cloudsperc;
    public boolean weatherUpdated = false;

    public zcWeather(Context mContext){
        this.mContext = mContext;
        weatherData = new ArrayList<>();
        checkLocation();
        updateWeather();
    }

    public zcWeather(Context mContext,int refreshTimeMins){
        this.updatePeriodMilis = refreshTimeMins*60000;
        this.mContext = mContext;
        checkLocation();
        updateWeather();
    }

        public void updateWeather(){

        if (this.lastgpsupdatemilis == -1) return;
        JSONWeatherTask task = new JSONWeatherTask();
        task.execute(new String[]{city});
    }

        public void checkLocation(){
        LocationManager lm = (LocationManager) mContext.getSystemService(
                Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);

        android.location.Location l = null;

        for (int i = providers.size() - 1; i >= 0; i--) {
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null) {
                gpsProvider = providers.get(i);
                break;
            }
        }

        if (l != null) {
            this.latitude = (float)l.getLatitude();
            this.longitude = (float)l.getLongitude();
            this.altitude = (float)l.getAltitude();
            this.lastgpsupdatemilis = System.currentTimeMillis();
        };
    }

    private class JSONWeatherTask extends AsyncTask<String, Void, Void> {

        protected boolean updated = false;

        @Override
        protected Void doInBackground(String... params) {
            String data = getWeatherData(WEATHER_URL + params[0]);

            try {
                JSONParseWeather(data);
                updated = true;

            } catch (JSONException e) {
                e.printStackTrace();
                updated = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }

    }

    private void JSONParseWeather(String data)throws JSONException,NullPointerException{
        // We create out JSONObject from the data
        JSONObject jObj = new JSONObject(data);

        JSONObject coordObj = getObject("coord", jObj);
        this.latitude =getFloat("lat", coordObj);
        this.longitude=getFloat("lon", coordObj);

        JSONObject sysObj = getObject("sys", jObj);
        this.country = getString("country", sysObj);
        this.sunrise = new Date(getInt("sunrise", sysObj)*1000);
        this.sunset = new Date(getInt("sunset", sysObj)*1000);
        this.city = getString("name", jObj);

        // We get weather info (This is an array)
        JSONArray jArr = jObj.getJSONArray("weather");

        // We use only the first value
        JSONObject JSONWeather = jArr.getJSONObject(0);
        this.weatherId=getInt("id", JSONWeather);
        this.descr = getString("description", JSONWeather);
        this.condition = getString("main", JSONWeather);
        this.icon = getString("icon", JSONWeather);

        JSONObject mainObj = getObject("main", jObj);
        this.humidity = getInt("humidity", mainObj);
        this.pressure = getInt("pressure", mainObj);
        this.maxTemp = getFloat("temp_max", mainObj);
        this.minTemp = getFloat("temp_min", mainObj);
        this.temp = getFloat("temp", mainObj);
        this.sealevel = getInt("sea_level",mainObj);
        this.groundlevel = getInt("grnd_level",mainObj);

        JSONObject wObj = getObject("wind", jObj);
        this.windspeed = getFloat("speed", wObj);
        this.winddeg = getFloat("deg", wObj);
        this.windgust =getFloat("gust",wObj);

        JSONObject cObj = getObject("clouds", jObj);
        this.cloudsperc = getInt("all", cObj);

        JSONObject rObj = getObject("rain", jObj);
        this.rainammount = getInt("3h", rObj);

        JSONObject sObj = getObject("snow", jObj);
        this.snowammount = getInt("3h", sObj);

        this.weatherUpdated = true;
    }

    */
    //endregion
}