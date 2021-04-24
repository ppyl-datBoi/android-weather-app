package com.example.weather.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.loopj.android.http.AsyncHttpClient;
import com.example.weather.R;
import com.example.weather.WeatherData;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class HomeFragment extends Fragment {

    String Location_Provider = LocationManager.GPS_PROVIDER;
    TextView NameofCity, weatherState, Temperature, minmaxTemperature, wind, pressure, humidity;
    ImageView mweatherIcon;
    LocationManager mLocationManager;
    LocationListener mLocationListner;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        findViews(root);

        return root;
    }

    public void findViews(View root){
        weatherState = root.findViewById(R.id.weatherCondition);
        Temperature = root.findViewById(R.id.temperature);
        minmaxTemperature = root.findViewById(R.id.minmax_temp);
        wind = root.findViewById(R.id.wind);
        pressure = root.findViewById(R.id.pressure);
        humidity = root.findViewById(R.id.humidity);
        mweatherIcon = root.findViewById(R.id.weatherIcon);
        NameofCity = root.findViewById(R.id.cityName);
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent mIntent=getActivity().getIntent();
        String city= mIntent.getStringExtra("City");
        if(city!=null)
        {
            getWeatherForNewCity(city);
        }
        else
        {
            getWeatherForCurrentLocation();
        }
    }

    private void getWeatherForNewCity(String city)
    {
        RequestParams params=new RequestParams();
        params.put("q",city);
        params.put("appid", WeatherData.APP_ID);
        params.put("lang", "ru");
        loadWeather(params);

    }

    private void getWeatherForCurrentLocation() {

        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mLocationListner = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                String Latitude = String.valueOf(location.getLatitude());
                String Longitude = String.valueOf(location.getLongitude());

                RequestParams params =new RequestParams();
                params.put("lat" ,Latitude);
                params.put("lon",Longitude);
                params.put("appid", WeatherData.APP_ID);
                params.put("lang", "ru");
                loadWeather(params);

            }

            @Override
            public void onProviderEnabled(String provider) {
                getWeatherForCurrentLocation();
            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(getActivity(),"Не удается найти местоположение",Toast.LENGTH_SHORT).show();
            }
        };


        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, WeatherData.REQUEST_CODE);
            return;
        }
        mLocationManager.requestLocationUpdates(Location_Provider, WeatherData.MIN_TIME, WeatherData.MIN_DISTANCE, mLocationListner);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if(requestCode== WeatherData.REQUEST_CODE)
        {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                // Toast.makeText(HomeFragment.this,"Locationget Succesffully",Toast.LENGTH_SHORT).show();
                getWeatherForCurrentLocation();
            }
            else
            {
                Toast.makeText(getActivity(),"Разрешите доступ",Toast.LENGTH_SHORT).show();
            }
        }


    }

    private  void loadWeather(RequestParams params)
    {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WeatherData.WEATHER_URL,params,new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                WeatherData weatherD= WeatherData.fromJson(response);
                updateUI(weatherD);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getActivity(),"Проблема с соединением",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private  void updateUI(WeatherData weather){

        Temperature.setText(weather.getmTemperature().toString());
        minmaxTemperature.setText(weather.getminmaxTemperature());
        humidity.setText(String.valueOf(weather.getHumidity()));
        wind.setText(String.valueOf(weather.getWind()));
        pressure.setText(String.valueOf(weather.getPressure()));
        NameofCity.setText(weather.getMcity());
        weatherState.setText(weather.getmWeatherType());
        int resourceID=getResources().getIdentifier(weather.getMicon(),"drawable",getActivity().getPackageName());
        mweatherIcon.setImageResource(resourceID);

    }

    @Override
    public void onPause() {
        super.onPause();
        if(mLocationManager!=null)
        {
            mLocationManager.removeUpdates(mLocationListner);
        }
    }
}