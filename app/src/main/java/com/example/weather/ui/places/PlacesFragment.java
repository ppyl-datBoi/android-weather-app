package com.example.weather.ui.places;

import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.weather.MainActivity;
import com.example.weather.R;
import com.example.weather.database.DatabaseHelper;
import com.example.weather.WeatherData;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class PlacesFragment extends Fragment {

    private static final String TAG = "CardListActivity";
    private CardArrayAdapter cardArrayAdapter;
    private ListView listView;
    private LinearLayout linearLayout;
    DatabaseHelper mDatabaseHelper;
    SearchView searchView;
    TextView cardCityTitle;
    TextView cardTempTitle;
    ImageView cardWeatherIcon;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseHelper = new DatabaseHelper(getContext());
        View root = inflater.inflate(R.layout.fragment_places, container, false);

        listView = (ListView) root.findViewById(R.id.card_listView);
        listView.addHeaderView(new View(getContext()));
        listView.addFooterView(new View(getContext()));

        cardArrayAdapter = new CardArrayAdapter(getContext(), R.layout.list_item_card);
        Cursor data = mDatabaseHelper.getData();
        while(data.moveToNext()){
            Card card = new Card("15" + "°C",data.getString(1));
            cardArrayAdapter.add(card);
            RequestParams params=new RequestParams();
            params.put("q",data.getString(1));
            params.put("appid",WeatherData.APP_ID);
            params.put("lang", "ru");
            findWeather(params, data.getPosition());
        }

        listView.setAdapter(cardArrayAdapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                TextView city = (TextView) view.findViewById(R.id.cardCityTitle);
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.putExtra("City", city.getText().toString());
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TextView city = (TextView) view.findViewById(R.id.cardCityTitle);
                mDatabaseHelper.deleteName(city.getText().toString());
                Card card= cardArrayAdapter.getItem(position - 1);
                cardArrayAdapter.remove(card);
                listView.setAdapter(cardArrayAdapter);
                return false;
            }
        });

        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        searchView = root.findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();
                List<Address> addressList = null;
                if (location != null  || !location.equals("")){
                    Geocoder geocoder = new Geocoder(getContext());
                    try {
                        addressList =  geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (addressList.size() == 0){
                        Toast.makeText(getActivity(),"Нет данных",Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    Address address = addressList.get(0);
                    boolean isAdded = mDatabaseHelper.addData(address.getLocality());
                    if(!isAdded) {
                        Toast.makeText(getActivity(),"Место уже добавлено",Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    Card card = new Card("-" + "°C",address.getLocality());
                    cardArrayAdapter.add(card);
                    listView.setAdapter(cardArrayAdapter);
                    UpdateWeatherList();
                    searchView.setQuery("", false);

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

        });

        return root;
    }


    private  void findWeather(RequestParams params, Integer index)
    {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WeatherData.WEATHER_URL,params,new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                WeatherData weatherD= WeatherData.fromJson(response);
                updateUI(weatherD, index);
            }


            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                //super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });

    }
    private  void updateUI(WeatherData weather, Integer index){

        View card = getViewByPosition(index, listView);
        cardTempTitle = card.findViewById(R.id.cardTempTitle);
        cardCityTitle = card.findViewById(R.id.cardCityTitle);
        cardWeatherIcon = card.findViewById(R.id.cardWeatherIcon);
        if (cardTempTitle == null || cardTempTitle == null) {
            new android.os.Handler(Looper.getMainLooper()).postDelayed(
                    new Runnable() {
                        public void run() {
                       updateUI(weather, index);
                        }
                    },
                    300);
        } else {
            cardCityTitle.setText(weather.getMcity());
            cardTempTitle.setText(weather.getmTemperature().toString() + " °C");
            int resourceID=getResources().getIdentifier(weather.getMicon(),"drawable",getActivity().getPackageName());
            cardWeatherIcon.setImageResource(resourceID);
            int b = 0;
        }

    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int childIndex = pos - firstListItemPosition;
        return listView.getChildAt(childIndex + 1);
//        }
    }

    public  void UpdateWeatherList(){
        Cursor data = mDatabaseHelper.getData();
        while(data.moveToNext()){
            RequestParams params=new RequestParams();
            params.put("q",data.getString(1));
            params.put("appid",WeatherData.APP_ID);
            params.put("lang", "ru");
            findWeather(params, data.getPosition());
        }
    }

}