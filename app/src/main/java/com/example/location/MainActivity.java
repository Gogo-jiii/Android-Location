package com.example.location;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Button btnGetLastLocation, btnGetAddress;
    TextView txtResult;
    private String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
    private PermissionManager permissionManager;
    private LocationManager locationManager;
    private IntentFilter localBroadcastIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGetLastLocation = findViewById(R.id.btnGetLastLocation);
        btnGetAddress = findViewById(R.id.btnGetAddress);
        txtResult = findViewById(R.id.txtResult);

        permissionManager = PermissionManager.getInstance(this);
        locationManager = LocationManager.getInstance(this);

        localBroadcastIntentFilter = new IntentFilter();
        localBroadcastIntentFilter.addAction("local_broadcast");

        btnGetLastLocation.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!permissionManager.checkPermissions(permissions)) {
                    permissionManager.askPermissions(MainActivity.this, permissions, 100);
                } else {
                    if (locationManager.isLocationEnabled()) {
                        Location location = locationManager.getLastLocation();
                        if (location != null) {
                            txtResult.setText("Last Location: \n" + "Lat: " + location.getLatitude() + "\n" +
                                    "Long: " + location.getLongitude());
                        } else {
                            Toast.makeText(MainActivity.this, "Could not fetch location.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        locationManager.createLocationRequest();
                    }
                }
            }
        });

        btnGetAddress.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                Location location = locationManager.getLastLocation();
                if (location != null) {
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude()
                                , location.getLongitude(), 1);

                        Address address = addresses.get(0);

                        String strAddress = "Addressline: " + address.getAddressLine(0) + "\n" +
                                "Admin Area: " + address.getAdminArea() + "\n" +
                                "Country Name: " + address.getCountryName() + "\n" +
                                "Feature Name: " + address.getFeatureName() + "\n" +
                                "Locality: " + address.getLocality() + "\n";

                        txtResult.setText(strAddress);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Please try again!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override protected void onResume() {
        super.onResume();
        locationManager.startLocationUpdates();
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(localBroadCastReceiver,
                localBroadcastIntentFilter);
    }

    @Override protected void onPause() {
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(localBroadCastReceiver);
        locationManager.stopLocationUpdates();
        super.onPause();
    }

    @Override public void onRequestPermissionsResult(int requestCode,
                                                     @NonNull String[] permissions,
                                                     @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionManager.handlePermissionResult(MainActivity.this, 100, permissions,
                grantResults)) {
            locationManager.createLocationRequest();
        }
    }

    BroadcastReceiver localBroadCastReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            Log.d("TAG", "Broadcasted");
            Toast.makeText(MainActivity.this,
                    intent.getStringExtra("location"), Toast.LENGTH_SHORT).show();
        }
    };

}