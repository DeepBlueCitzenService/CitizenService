package io.github.deepbluecitizenservice.citizenservice.database;

import android.net.Uri;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;

@IgnoreExtraProperties
public class ProblemModel{
    public String url, creatorKey, description, locationAddress, solutionUrl;
    public double locationX, locationY;
    public long sla, timeCreated;
    public int status, category;

    private String id, userName;
    private Uri userURL;

    static final String  SOLVED_PROBLEM = "solved";
    static final String OPEN_PROBLEM = "open";

    public static final String
            URL = "URL",
            ID = "id",
            STATUS = "status",
            LOCATIONX = "locationX",
            LOCATIONY = "locationY",
            LOCATIONADDRESS = "location",
            CREATOR = "creatorKey",
            SLA     = "SLA",
            TIMECREATED = "timeCreated",
            DESCRIPTION = "description",
            CATEGORY    = "category";


    ProblemModel(){

    }

    ProblemModel(String url, String id, int status, double locationX, double locationY, String locationAddress,
                 String creatorKey, long SLA, long timeCreated, String description, int category){

        this.url             = url;
        this.id              = id;
        this.status          = status;
        this.locationX       = locationX;
        this.locationY       = locationY;
        this.creatorKey      = creatorKey;
        this.sla             = SLA;
        this.timeCreated     = timeCreated;
        this.description     = description;
        this.category        = category;
        this.locationAddress = locationAddress;
    }

    @Exclude
    public String getId(){
        return this.id;
    }

    @Exclude
    public HashMap<String, Object> getDetails(){
        HashMap<String, Object> result = new HashMap<>();
        result.put(URL, url);
        result.put(ID, id);
        result.put(STATUS, status);
        result.put(LOCATIONADDRESS, locationAddress);
        result.put(LOCATIONX, locationX);
        result.put(LOCATIONY, locationY);
        result.put(SLA, sla);
        result.put(TIMECREATED, timeCreated);
        result.put(DESCRIPTION, description);
        result.put(CATEGORY, category);
        result.put(CREATOR, creatorKey);
        return  result;
    }

    @Exclude
    public void setUser(Uri URL, String userName){
        this.userURL = URL;
    }
}