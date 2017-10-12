package com.example.user.sdcard1.service;

import android.net.Uri;
import android.os.AsyncTask;

import com.example.user.sdcard1.data.Channel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by user on 20-03-2017.
 */
public class YahooWeatherService {

    private WeatherServiceCallback callback;
    private String coordinates;
    private Exception error;
    private HttpURLConnection httpURLConnection;
    private URL url;
    private boolean flag;

    public YahooWeatherService(WeatherServiceCallback callback,boolean flag) {
        this.callback = callback;
        this.flag=flag;
    }

    public void refreshWeather(String coordinates){

        this.coordinates=coordinates;

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {

                String ls="(12.8251002,80.0421404)";

                String YQL=String.format("select * from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"%s\")and u='c'",params[0]);

                String endpoint=String.format("https://query.yahooapis.com/v1/public/yql?q=%s&format=json", Uri.encode(YQL));

                try {
                    url=new URL(endpoint);

                    httpURLConnection=(HttpURLConnection)url.openConnection();

                    InputStream inputStream=httpURLConnection.getInputStream();

                    BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream));

                    StringBuilder result=new StringBuilder();

                    String line;

                    while((line=reader.readLine())!=null){

                        result.append(line);
                    }

                    return result.toString();

                }
                catch (Exception e) {
                    error=e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {

                if(s==null && error!=null){

                    callback.serviceFailure(error);

                    return;
                }
                try {
                    JSONObject data=new JSONObject(s);

                    JSONObject queryResults=data.optJSONObject("query");

                    int count=queryResults.optInt("count");

                    if(count==0){

                        callback.serviceFailure(new LocationWeatherException("No weather information found for current location"));
                        return;
                    }

                    Channel channel=new Channel();
                    channel.populate(queryResults.optJSONObject("results").optJSONObject("channel"));

                            if(flag) {

                                callback.serviceSuccess(channel);
                            }
                    else{
                                callback.serviceSuccessForToggle(channel);
                            }
                }
                catch (JSONException e) {
                    callback.serviceFailure(e);
                }
            }
        }.execute(this.coordinates);

    }

    public class LocationWeatherException extends Exception{

        public LocationWeatherException(String detailMessage) {
            super(detailMessage);
        }
    }
}
