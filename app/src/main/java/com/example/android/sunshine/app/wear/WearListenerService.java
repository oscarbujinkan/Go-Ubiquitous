package com.example.android.sunshine.app.wear;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.example.android.sunshine.app.Utility;
import com.example.android.sunshine.app.data.WeatherContract;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import static com.google.android.gms.wearable.PutDataRequest.WEAR_URI_SCHEME;

/**
 * Created by oscarBudo on 22-08-16.
 */
public class WearListenerService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<DataApi.DeleteDataItemsResult> {
    public static final String START_ACTIVITY_PATH = "/forecast-wear";
    private GoogleApiClient mGoogleApiClient;
    private ForecastWear mForecastWear;
    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };
    private static final int MAX_TEMP=0;
    private static final int MIN_TEMP=1;
    private static final int WEATHER_ID=2;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        sendForecastToWear();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if(messageEvent.getPath().equals(START_ACTIVITY_PATH)) {
            sendForecastToWear();
        }
    }
    @Override
    public void onResult(DataApi.DeleteDataItemsResult deleteDataItemsResult) {

    }
    public void sendForecastToWear(){
         mForecastWear=getWeather();
        if(mForecastWear!=null) {
            PutDataMapRequest putDataMapReq = PutDataMapRequest.create(START_ACTIVITY_PATH);
            putDataMapReq.getDataMap().putString("max", mForecastWear.max_temp);
            putDataMapReq.getDataMap().putString("min", mForecastWear.min_temp);
            putDataMapReq.getDataMap().putInt("id", mForecastWear.weather_id);


            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        }


    }

    private ForecastWear getWeather(){
        ForecastWear fw=new ForecastWear();
        String locationSetting = Utility.getPreferredLocation(this);
        Uri weather= WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting,System.currentTimeMillis());
        Cursor cursor=getContentResolver().query(weather,FORECAST_COLUMNS,null,null, WeatherContract.WeatherEntry.COLUMN_DATE+" ASC");
        if(cursor!=null){
            if(!cursor.moveToFirst()){
                cursor.close();
            }else{
                fw.max_temp=String.valueOf((int) cursor.getDouble(MAX_TEMP));
                fw.min_temp=String.valueOf((int) cursor.getDouble(MIN_TEMP));
                fw.weather_id =cursor.getInt(WEATHER_ID);
                return fw;
            }
        }

        return null;
    }
}
