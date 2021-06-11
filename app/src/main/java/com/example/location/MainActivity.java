package com.example.location;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.BackoffPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

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
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    Button btnGetLastLocation, btnGetAddress, btnGetBackgroundLocation;
    TextView txtResult;
    private String[] foregroundLocationPermissions = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    private String[] backgroundLocationPermissions =
            {Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION};
    private PermissionManager permissionManager;
    private LocationManager locationManager;
    private IntentFilter localBroadcastIntentFilter;
    private WorkRequest foregroundWorkRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGetLastLocation = findViewById(R.id.btnGetLastLocation);
        btnGetBackgroundLocation = findViewById(R.id.btnGetBackgroundLocation);
        btnGetAddress = findViewById(R.id.btnGetAddress);
        txtResult = findViewById(R.id.txtResult);

        permissionManager = PermissionManager.getInstance(this);
        locationManager = LocationManager.getInstance(this);

        localBroadcastIntentFilter = new IntentFilter();
        localBroadcastIntentFilter.addAction("foreground_location");

        btnGetLastLocation.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!permissionManager.checkPermissions(foregroundLocationPermissions)) {
                    permissionManager.askPermissions(MainActivity.this,
                            foregroundLocationPermissions, 100);
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

        btnGetBackgroundLocation.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!permissionManager.checkPermissions(backgroundLocationPermissions)) {
                    permissionManager.askPermissions(MainActivity.this,
                            backgroundLocationPermissions, 200);
                } else {
                    startLocationWork();
                }
            }
        });
    }

    @Override protected void onResume() {
        super.onResume();
        locationManager.startLocationUpdates();
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(foregroundLocationBroadCastReceiver,
                localBroadcastIntentFilter);
    }

    @Override protected void onPause() {
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(foregroundLocationBroadCastReceiver);
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
        } else if (permissionManager.handlePermissionResult(MainActivity.this, 200, permissions,
                grantResults)) {
            startLocationWork();
        }
    }

    BroadcastReceiver foregroundLocationBroadCastReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            Log.d("TAG", "Broadcasted");
            Toast.makeText(MainActivity.this,
                    intent.getStringExtra("location"), Toast.LENGTH_SHORT).show();
        }
    };

    private void startLocationWork() {
        foregroundWorkRequest = new OneTimeWorkRequest.Builder(BackgroundLocationWork.class)
                .addTag("LocationWork")
                .setBackoffCriteria(BackoffPolicy.LINEAR,
                        OneTimeWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.SECONDS)
                .build();

        WorkManager.getInstance(MainActivity.this).enqueue(foregroundWorkRequest);
    }
}