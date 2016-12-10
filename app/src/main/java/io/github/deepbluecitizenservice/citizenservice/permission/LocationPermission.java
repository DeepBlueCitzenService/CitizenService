package io.github.deepbluecitizenservice.citizenservice.permission;

import android.Manifest;
import android.content.Context;

import io.github.deepbluecitizenservice.citizenservice.R;

public class LocationPermission extends Permission {

    public LocationPermission(Context context){
        super(context, context.getString(R.string.permission_group_location));
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
