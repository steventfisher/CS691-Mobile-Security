package com.example.steve.wapdetect;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import im.delight.android.location.SimpleLocation;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity implements View.OnClickListener{

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private final LatLng mDefaultLocation = new LatLng(39.529633, -119.813803);
    private SimpleLocation location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        location = new SimpleLocation(this);

        if (!location.hasLocationEnabled()){
            SimpleLocation.openSettings(this);
        }
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();

        Toast.makeText(MainActivity.this, "Latitude: "+latitude, Toast.LENGTH_SHORT).show();
        Toast.makeText(MainActivity.this, "Longitude: "+longitude, Toast.LENGTH_SHORT).show();
        Log.d("Lat:", String.valueOf(latitude));
        Log.d("Long:", String.valueOf(longitude));
        textStatus = (TextView) findViewById(R.id.textStatus);
        btnScan = (Button) findViewById(R.id.btnScan);
        btnScan.setOnClickListener(this);
        lv = (ListView)findViewById(R.id.list);

        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled() == false)
        {
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);
        }
        this.adapter = new SimpleAdapter(MainActivity.this, arraylist, R.layout.row, new String[] { ITEM_KEY }, new int[] { R.id.list_value });
        lv.setAdapter(this.adapter);

        registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context c, Intent intent)
            {
                results = wifi.getScanResults();
                size = results.size();
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        /*new Thread() {
            @Override
            public void run() {
                makeGetRequest(latitude, longitude);
            }
        }.start();*/

    }
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }

    }
    @Override
    protected void onResume() {
        super.onResume();

        // make the device update its location
        getLocationPermission();
        location.beginUpdates();
    }

    /*@Override
    protected void onPause() {
        // stop location updates (saves battery)
        location.endUpdates();

        super.onPause();
    }*/

    public String[][] makeGetRequest(double lat, double lon) {

        double latrange1;
        double latrange2;
        double longrange1;
        double longrange2;
        JSONObject results1;
        JSONArray tmpObj = new JSONArray();
        JSONObject tmpObj1;
        String tmp = new String();
        //double mLatitude;
        //double mLongitude;
        //private Location mLastKnownLocation = inLastKnown;

        //mLatitude = mLastKnownLocation.getLatitude();
        //mLongitude = mLastKnownLocation.getLongitude();
        /*latrange1 = 39.5336;
        latrange2 = 39.5436;
        longrange1 = -119.8121;
        longrange2 = -119.8221;*/
        /*double mlat = (double)Math.round(39.529633*10000)/10000;
        double mlong = (double)Math.round(-119.813803*10000)/10000;*/
        double mlat = (double)Math.round(lat*10000)/10000;
        double mlong = (double)Math.round(lon*10000)/10000;
        String[][] tStrings = new String[2][2];
        String[][] tmpString = new String[2][2];
        latrange1 = mlat - 0.0005;
        latrange2 = mlat + 0.0005;
        longrange1 = mlong  - 0.0005;
        longrange2 = mlong + 0.0005;

        Log.d("lat1", String.valueOf(latrange1));
        Log.d("lat2", String.valueOf(latrange2));
        Log.d("long1", String.valueOf(longrange1));
        Log.d("long2", String.valueOf(longrange2));



        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet("https://api.wigle.net/api/v2/network/search?onlymine=false&"
                + "latrange1=" + String.valueOf(latrange1) + "&latrange2=" + String.valueOf(latrange2)
                + "&longrange1=" + String.valueOf(longrange1) + "&longrange2=" + String.valueOf(longrange2)
                + "&freenet=false&paynet=false");

        //request.setHeader(new BasicHeader("Content-Type","application/json"));
        request.setHeader(new BasicHeader("Authorization","ADDED AUTHORIZATION KEY"));

        HttpResponse response;
        try {
            response = client.execute(request);

            String server_response = EntityUtils.toString(response.getEntity());

            try {
                results1 = new JSONObject(server_response);
                if(results1.getJSONObject("totalResults").toString() != "0") {
                    tmpObj = results1.getJSONArray("results");
                    tmpObj1 = tmpObj.getJSONObject(0);
                    tmp = tmpObj1.getString("ssid");
                    tmpString = new String[tmpObj.length()+1][2];
                    for(int i = 0; i < tmpObj.length();i++){
                        tmpObj.getJSONObject(i);
                        tmpString[i][0] = tmpObj1.getString("ssid");
                        tmpString[i][1] = tmpObj1.getString("netid");
                    }
                }

            }
            catch (Exception je)
            {
                //Log.d("Error w/file: ", je.getMessage());
            }

            tStrings[0][0] = "pride";
            tStrings[0][1] = "58:6d:8f:67:ef:39";
            tStrings[1][0] = "2WIRE271";
            tStrings[1][1] = "00:0F:23:D1:C1:CC";

            Log.d("Response of GET request", response.toString());
            Log.d("Result", response.getStatusLine().toString());
            Log.d("Server Response", server_response);
            Log.d("First", tmp );

            //Log.d("first",tmp);
        } //catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
       // } catch (IOException e) {
            // TODO Auto-generated catch block
       //     e.printStackTrace();
        //}
        catch (Exception e)
        { }
        //return tStrings;
        if(tmpString != null) {
            return tmpString;
        }
        else{
            tmpString[0][0] = "None";
            return tmpString;
        }
    }
    WifiManager wifi;
    ListView lv;
    TextView textStatus;
    Button btnScan;
    int size = 0;
    List<ScanResult> results;

    String ITEM_KEY = "key";
    ArrayList<HashMap<String, String>> arraylist = new ArrayList<HashMap<String, String>>();
    SimpleAdapter adapter;

    public void onClick(View view)
    {
        arraylist.clear();
        wifi.startScan();

        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        //final String[][] testString;

        /*new Thread() {
            @Override
            public void run() {*/
                String[][] testString1 = makeGetRequest(latitude, longitude);

            /*}
        }.start();*/
        //testString = testString1;
        Toast.makeText(this, "Scanning...." + size, Toast.LENGTH_SHORT).show();
        try
        {
            size = size - 1;
            int tmpsize = size - 1;
            while (size >= 0)
            {
                HashMap<String, String> item = new HashMap<String, String>();
                int count = 0;
                for(int i = 0; i < testString1.length; i++){
                    String testS = results.get(size).SSID;
                    String testS2 = testString1[i][0];
                    Log.d("test", testS);
                    if(Arrays.asList(results).contains(testS2))
                    {
                        item.put(ITEM_KEY, results.get(size).SSID + "-" + results.get(size).BSSID + " IN LIST");
                        //arraylist.add(item);
                        //adapter.notifyDataSetChanged();
                    } else {
                        item.put(ITEM_KEY, results.get(size).SSID + "  " + results.get(size).BSSID + " NOT IN LIST");
                        //arraylist.add(item);
                        //adapter.notifyDataSetChanged();
                    }
                    /*if(testS.equals(testS2))
                      {
                          item.put(ITEM_KEY, results.get(size).SSID + "  " + results.get(size).BSSID + " IN LIST");
                          count = 1;
                          arraylist.add(item);
                          //size--;
                          adapter.notifyDataSetChanged();
                      }*/
                  }
                /*if(count != 1){
                      item.put(ITEM_KEY, results.get(size).SSID + "  " + results.get(size).BSSID + " NOT IN LIST");
                      arraylist.add(item);
                      //size--;

                  }*/

                //item.put(ITEM_KEY, results.get(size).SSID + "  " + results.get(size).BSSID);

                arraylist.add(item);
                size--;
                adapter.notifyDataSetChanged();
                //adapter.notifyDataSetChanged();
            }
        }
        catch (Exception e)
        { }
    }
}
