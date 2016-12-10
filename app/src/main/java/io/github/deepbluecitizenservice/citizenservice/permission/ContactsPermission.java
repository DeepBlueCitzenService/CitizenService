package io.github.deepbluecitizenservice.citizenservice.permission;

import android.Manifest;
import android.content.Context;

import io.github.deepbluecitizenservice.citizenservice.R;

public class ContactsPermission extends Permission {

    public ContactsPermission(Context context){
        super(context, context.getString(R.string.permission_group_contacts));
    }

    private String[] getContactsPermissions(){
        return new String[]{
                Manifest.permission.READ_CONTACTS
        };
    }

    @Override
    void setPermissions() {
        this.permissions = getContactsPermissions();
    }
}
