package com.example.weather;

import org.json.JSONException;
import org.json.JSONObject;

public class WeatherData {
    public static String APP_ID = "dab3af44de7d24ae7ff86549334e45bd";
    public static String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";
    public static long MIN_TIME = 5000;
    public static float MIN_DISTANCE = 1000;
    public static int REQUEST_CODE = 101;

    private String micon,mcity,mWeatherType;
    private int mCondition;
    private int mTemperature;
    private double wind;
    private int pressure;
    private int humidity;
    private int minTemperature;
    private int maxTemperature;

    public WeatherData() {
    }


    public static WeatherData fromJson(JSONObject jsonObject)
    {

        try
        {
            WeatherData weatherD=new WeatherData();
            weatherD.mcity=jsonObject.getString("name");
            weatherD.mCondition=jsonObject.getJSONArray("weather").getJSONObject(0).getInt("id");
            weatherD.mWeatherType=jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
            weatherD.pressure=jsonObject.getJSONObject("main").getInt("pressure");
            weatherD.humidity=jsonObject.getJSONObject("main").getInt("humidity");
            weatherD.wind=jsonObject.getJSONObject("wind").getInt("speed");
            weatherD.micon=updateWeatherIcon(weatherD.mCondition);
            weatherD.mTemperature=getСelsiusTemperature(jsonObject,"temp");
            weatherD.minTemperature=getСelsiusTemperature(jsonObject,"temp_min");
            weatherD.maxTemperature=getСelsiusTemperature(jsonObject,"temp_max");
            return weatherD;
        }


        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }


    }

    private static int getСelsiusTemperature(JSONObject jsonObject, String title) {
        double tempResult= 0;
        try {
            tempResult = jsonObject.getJSONObject("main").getDouble(title)-273.15;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return (int)Math.rint(tempResult);
    }


    private static String updateWeatherIcon(int condition)
    {
        if(condition>=0 && condition<=300)
        {
            return "thunderstrom1";
        }
        else if(condition>=300 && condition<=500)
        {
            return "lightrain";
        }
        else if(condition>=500 && condition<=600)
        {
            return "shower";
        }
        else  if(condition>=600 && condition<=700)
        {
            return "snow2";
        }
        else if(condition>=701 && condition<=771)
        {
            return "fog";
        }

        else if(condition>=772 && condition<800)
        {
            return "overcast";
        }
        else if(condition==800)
        {
            return "sunny";
        }
        if(condition >= 801 && condition <= 803) return "cloudy";
        if(condition == 804) return "cloudheavy";
        else  if(condition>=900 && condition<=902)
        {
            return "thunderstrom1";
        }
        if(condition==903)
        {
            return "snow1";
        }
        if(condition==904)
        {
            return "overcast";
        }
        if(condition>=905 && condition<=1000)
        {
            return "thunderstrom2";
        }

        return "dunno";


    }

    public int getHumidity() {
        return humidity;
    }
    public int getPressure() {
        return pressure;
    }
    public double getWind() {
        return wind;
    }
    public Integer getmTemperature() {
        return mTemperature;
    }
    public Integer getMinTemperature() {
        return minTemperature;
    }
    public Integer getMaxTemperature() {
        return maxTemperature;
    }
    public String getminmaxTemperature() {
        return String.valueOf(minTemperature)+"°c / "+String.valueOf(maxTemperature)+"°c";
    }

    public String getMicon() {
        return micon;
    }

    public String getMcity() {
        return mcity;
    }

    public String getmWeatherType() {
        return mWeatherType;
    }
}
