package io.github.deepbluecitizenservice.citizenservice.permission;

import android.Manifest;
import android.content.Context;

import io.github.deepbluecitizenservice.citizenservice.R;

public class StoragePermission extends Permission {

    public StoragePermission(Context context){
        super(context, context.getString(R.string.permission_group_storage));
    }

    private String[] getStoragePermissions(){
        return new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
    }

    @Override
    void setPermissions() {
        this.permissions = getStoragePermissions();
    }
}
