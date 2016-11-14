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

    private String id;

    static final String  SOLVED_PROBLEM = "solved";
    static final String OPEN_PROBLEM = "open";

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
}