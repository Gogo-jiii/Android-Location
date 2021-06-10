package com.example.location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.ResolvableApiException;

public class MainActivity extends AppCompatActivity implements LocationManager.LocationSettingsCallBack {

    Button btnGetLocation;
    TextView txtResult;
    private String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
    private PermissionManager permissionManager;
    private LocationManager locationManager;
    private final int CHECK_LOCATION_SETTINGS = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGetLocation = findViewById(R.id.btnGetLocation);
        txtResult = findViewById(R.id.txtResult);

        permissionManager = PermissionManager.getInstance(this);
        locationManager = LocationManager.getInstance(this);

        btnGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!permissionManager.checkPermissions(permissions)) {
                    permissionManager.askPermissions(MainActivity.this, permissions, 100);
                } else {
                    //permission granted
                    locationManager.createLocationRequest();
                }
            }
        });
    }

    @Override protected void onResume() {
        super.onResume();
        locationManager.startLocationUpdates();
    }

    @Override protected void onPause() {
        super.onPause();
        locationManager.stopLocationUpdates();
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                     @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionManager.handlePermissionResult(MainActivity.this, 100, permissions,
                grantResults)) {
            //permission granted
            locationManager.createLocationRequest();
        }

    }

    @Override public void onLocationSettingsSuccessful() {
        getLastLocation();
    }

    @Override public void onLocationSettingsFailure(Exception e) {
        if (e instanceof ResolvableApiException) {
            try {
                ((ResolvableApiException) e).startResolutionForResult(MainActivity.this,
                        CHECK_LOCATION_SETTINGS);
            } catch (Exception exception) {

            }
        }
    }

    private void getLastLocation() {
        Location location = locationManager.getLastLocation();
        if (location != null) {
            txtResult.setText("Latitude: " + location.getLatitude() + "\n" +
                    "Longitude: " + location.getLongitude());
        } else {
            txtResult.setText("Could not fetch location, please try again!");
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode,
                                              @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHECK_LOCATION_SETTINGS && resultCode == RESULT_OK) {
            getLastLocation();
        }
    }
}