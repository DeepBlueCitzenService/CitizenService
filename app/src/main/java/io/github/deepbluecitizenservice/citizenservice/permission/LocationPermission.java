package io.github.deepbluecitizenservice.citizenservice.permission;

import android.Manifest;
import android.content.Context;

public class LocationPermission extends Permission {

    public LocationPermission(Context context){
        super(context, "Location");
    }

    private String[] getLocationPermissions(){
        return new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
    }

    @Override
    void setPermissions() {
        this.permissions = getLocationPermissions();
    }
}
