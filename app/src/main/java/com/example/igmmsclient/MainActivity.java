package com.example.igmmsclient;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.igmmsclient.URL.URL;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity {


    public static final int default_check = 30000;
    public static final int fastest_check = 5000;
    private static final int PERMISSION_FINE_LOCATION = 99;
    TextView tv_lat, tv_lon, tv_accuracy, tv_address, tv_speed, tv_sensor, tv_altitude, tv_updates, connectText;
    Button status, ConnectButton;
    Switch sw_locationupdates, sw_gps;
    LocationCallback locationCallBack;
    String lat, lon;
    private RequestQueue requestQueue;





    //google api client
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);





            //give each  UI variable a value
        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        sw_gps = findViewById(R.id.sw_gps);
        sw_locationupdates = findViewById(R.id.sw_locationsupdates);
        status = findViewById(R.id.button);
        ConnectButton =findViewById(R.id.connectbtn);
        connectText =findViewById(R.id.connecttxt);



        // assigning variables to locationrequest

        locationRequest = new LocationRequest();

        // time limit before default check
        locationRequest.setInterval(default_check);

        // how quickly the app should check for location when  set to most frequent update
        locationRequest.setFastestInterval(fastest_check);

        locationRequest.setPriority(locationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        //method called when the update time interval is met
        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                // saving the new data from locationResult in the location variable
                Location location = locationResult.getLastLocation();
                update_UI_values(location);
            }
        };

        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_gps.isChecked()) {
                    locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText("using GPS sensors");
                } else {
                    locationRequest.setPriority(locationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("Using towers + wifi");
                }
            }
        });
        sw_locationupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if statement for turning on and off location tracking
                if (sw_locationupdates.isChecked()) {
                    startLocationUpdates();
                      tv_sensor.setText("You are being tracked");


                } else {
                    stopLocationUpdates();
                    tv_sensor.setText("");
                }
            }


        });
//        connect button on click action listener
        ConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectText.getText().toString();
            }
        });

        update_GPS();

//        status button click event handler
        status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println(tv_lat.getText());
                Log.d("LOGGED CORDINATES", "onClick: " + tv_lat.getText() + tv_lon.getText())  ;
            }
        });
         requestQueue = Volley.newRequestQueue(MainActivity.this);

    }
    // on create method ends here


private void Connectbtn (String url){

}

//    location updates enabled and disabled below
    @SuppressLint("SetTextI18n")
    private void stopLocationUpdates() {

        tv_lat.setText("your location is private");
        tv_lon.setText("your location is private");
        tv_speed.setText("your location is private");
        tv_altitude.setText("your location is private");
        tv_accuracy.setText("your location is private");
        tv_address.setText("your location is private");
        tv_sensor.setText("your location is private");
        tv_updates.setText("your location is private");

        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {

        tv_lat.setText("your location is being tracked");

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
//        update GPS fields function
        update_GPS();

//        function to updatelocation values in my local database fires when functions startLocationUpdates fires
        updateLocation();

    }





    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @org.jetbrains.annotations.NotNull String[] permissions, @NonNull @org.jetbrains.annotations. NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    update_GPS();
                }
                else {
                    Toast.makeText(this, "This app wants to know your location", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }


    }


    private void update_GPS(){
        //get user permission
        //get client location
        //update the UI for the user with new location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                //after getting permission save the data in the location variable
                public void onSuccess(Location location) {

                    // method update_UI_values uses the location variable to update the UI
                    update_UI_values(location);


                }
            });
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);


        }
    }

    //updating all the UI elements values

    private void update_UI_values(Location location) {
        //   update_UI_values(location);
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));


        if (location.hasAltitude()){
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        }
        else {
            tv_altitude.setText("Not available");
        }

        if (location.hasSpeed()){
            tv_speed.setText(String.valueOf(location.getSpeed()));
        }
        else {
            tv_speed.setText("Not available");
        }
        Geocoder geocoder = new Geocoder(MainActivity.this);
        try {
            List<Address> addresses= geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            tv_address.setText(addresses.get(0).getAddressLine(0));
        }
        catch (Exception e){
            tv_address.setText("Unable to find your street address");
        }

        lat = String.valueOf(location.getLatitude());
        lon = String.valueOf(location.getLongitude());
    }



//    the function to update location data to my local database updateLocation();
    public void updateLocation(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL.updateLocation,
                new Response.Listener<String>() {
                    public void onResponse(String response) {

//                        Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG).show();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse response = error.networkResponse;
                        if (error instanceof ServerError && response != null) {
                            try {
                                String res = new String(response.data,
                                        HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                                JSONObject obj = new JSONObject(res);
                            } catch (UnsupportedEncodingException e1) {
                                e1.printStackTrace();
                            } catch (JSONException e2) {
                                e2.printStackTrace();
                            }
                        }

//                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
//            hash map to send data to my local data base
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("longitude", String.valueOf(lon));
                params.put("latitude", String.valueOf(lat));
                params.put("animal_tag","999");

                return params;
            }
        };
        // Creating RequestQueue.
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);


    }
}