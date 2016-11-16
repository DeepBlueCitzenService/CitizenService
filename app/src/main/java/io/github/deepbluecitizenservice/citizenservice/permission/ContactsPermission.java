package io.github.deepbluecitizenservice.citizenservice.permission;

import android.Manifest;
import android.content.Context;

public class ContactsPermission extends Permission {

    public ContactsPermission(Context context){
        super(context, "Contacts");
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
