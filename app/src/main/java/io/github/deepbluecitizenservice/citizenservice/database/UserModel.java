package io.github.deepbluecitizenservice.citizenservice.database;

import android.net.Uri;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;

@IgnoreExtraProperties
public class UserModel{
    public String email, name, photoURL;

    public UserModel(){

    }

    public UserModel(String name, String email, Uri photoURL){
        this.email = email;
        this.name  = name;
        this.photoURL = photoURL.toString();
    }
}