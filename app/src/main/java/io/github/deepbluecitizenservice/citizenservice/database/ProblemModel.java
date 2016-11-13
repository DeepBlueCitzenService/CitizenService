package io.github.deepbluecitizenservice.citizenservice.database;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class ProblemModel{

    public String url, creatorKey, description, locationAddress, solutionUrl;
    public double locationX, locationY;
    public long SLA, timeCreated;
    public int status, category;

    static final String  SOLVED_PROBLEM = "solved";
    static final String OPEN_PROBLEM = "open";

    ProblemModel(){

    }

    ProblemModel(String url, int status, double locationX, double locationY, String locationAddress,
                 String creatorKey, long SLA, long timeCreated, String description, int category){

        this.url             = url;
        this.status          = status;
        this.locationX       = locationX;
        this.locationY       = locationY;
        this.creatorKey      = creatorKey;
        this.SLA             = SLA;
        this.timeCreated     = timeCreated;
        this.description     = description;
        this.category        = category;
        this.locationAddress = locationAddress;
    }
}