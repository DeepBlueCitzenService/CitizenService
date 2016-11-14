package io.github.deepbluecitizenservice.citizenservice.permission;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

abstract class Permission implements ActivityCompat.OnRequestPermissionsResultCallback {

    private Context context;
    private final int PERMISSION_REQUEST = 1;
    private boolean isGranted = false;
    private String permissionGroup;
    String[] permissions;

    Permission(Context context, String permissionGroup){
        this.context = context;
        this.permissionGroup = permissionGroup;
        setPermissions();
    }

    abstract void setPermissions();

    public boolean isGranted(){
        return isGranted;
    }

    private boolean isPermissionGranted(){
        boolean grantedResult = true;
        int result;

        for(String permission : permissions) {
            result = ContextCompat.checkSelfPermission(context, permission);
            grantedResult &= (result == PackageManager.PERMISSION_GRANTED);
        }
        this.isGranted = grantedResult;
        return isGranted;
    }

    private boolean shouldShowRationales(){
        boolean result = false;
        for(String permission : permissions){
            result |= ActivityCompat
                    .shouldShowRequestPermissionRationale((Activity)context, permission);
        }
        return result;
    }

    public void askPermissions(View v){
        if(!isPermissionGranted()){
            if(shouldShowRationales()){
                Snackbar snackbar = Snackbar.make(v,
                        "Please Give " + permissionGroup + " Permissions for Proper Functioning",
                        Snackbar.LENGTH_LONG);
                snackbar.setAction("Ok", okListener());
                snackbar.show();
            }
            else {
                requestPermissions();
            }
        }
    }

    private View.OnClickListener okListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions();
            }
        };
    }

    @Override
    public void onRequestPermissionsResult
            (int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST:
                isGranted = verifyPermissions(grantResults);
                break;
        }
    }

    private void requestPermissions(){
        for(String permission : permissions){
            ActivityCompat.requestPermissions((Activity)context,
                    new String[]{permission}, PERMISSION_REQUEST);
        }
    }


    private static boolean verifyPermissions(int[] grantResults) {
        if(grantResults.length < 1) return false;
        for (int result : grantResults)
            if (result != PackageManager.PERMISSION_GRANTED)
                return false;
        return true;
    }
}
