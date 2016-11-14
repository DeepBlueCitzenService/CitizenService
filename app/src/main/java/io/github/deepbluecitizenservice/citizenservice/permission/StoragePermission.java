package io.github.deepbluecitizenservice.citizenservice.permission;

import android.Manifest;
import android.content.Context;

public class StoragePermission extends Permission {

    public StoragePermission(Context context){
        super(context, "Storage");
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
