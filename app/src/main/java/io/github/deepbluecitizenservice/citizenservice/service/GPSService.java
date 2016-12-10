package io.github.deepbluecitizenservice.citizenservice.service;

import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import io.github.deepbluecitizenservice.citizenservice.R;
import io.github.deepbluecitizenservice.citizenservice.permission.LocationPermission;

public class GPSService extends Service implements LocationListener {

    private LocationManager manager;
    private Location location;
    private double latitude;
    private double longitude;

    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60;

    private Context context;
    private LocationPermission permission;
    private View baseView;
    private GoogleMap googleMap = null;


    public GPSService(Context context, View baseView) {
        this.context = context;
        this.permission = new LocationPermission(context);
        this.baseView = baseView;
        getLocation();
    }

    public GPSService(Context context, View baseView, GoogleMap map) {
        this(context, baseView);
        this.googleMap = map;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressWarnings("MissingPermission")
    public Location getLocation() {
        location = null;

        manager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        isGPSEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        permission.askPermissions(baseView);

        if (permission.isGranted() && (isGPSEnabled || isNetworkEnabled)) {
            if (isNetworkEnabled) {
                manager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (manager != null) {
                    location = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            }
            if (isGPSEnabled) {
                if (location == null) {
                    manager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (manager != null) {
                        location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
            }
        }
        else if(!isGPSEnabled && !isNetworkEnabled){
            showSettingsAlert();
        }

        return location;
    }

    public boolean isGPSPermissionGranted(){
        return permission.isGranted();
    }

    public boolean isGPSEnabled(){
        return isGPSEnabled || isNetworkEnabled;
    }

    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        return latitude;
    }

    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        return longitude;
    }

    @SuppressWarnings("MissingPermission")
    public void stopUsingGPS() {
        if (manager != null) {
            permission.askPermissions(baseView);
            if(permission.isGranted()){
                manager.removeUpdates(GPSService.this);
            }

        }
    }

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.gps_disabled_dialog_title));

        // Setting Dialog Message
        alertDialog.setMessage(getString(R.string.gps_disabled_dialog_details));

        // On pressing Settings button
        alertDialog.setPositiveButton(getString(R.string.gps_disabled_settings_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(googleMap != null && isGPSEnabled() && isGPSPermissionGranted()){
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
            googleMap.animateCamera(cameraUpdate);
            //noinspection MissingPermission
            manager.removeUpdates(this);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }
}
