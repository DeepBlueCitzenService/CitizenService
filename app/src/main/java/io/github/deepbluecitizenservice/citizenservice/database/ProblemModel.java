package io.github.deepbluecitizenservice.citizenservice.database;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@IgnoreExtraProperties
public class ProblemModel implements Parcelable{
    public String url, creatorKey, description, locationAddress,
            creatorName, creatorURL, solutionUrl;
    public double locationX, locationY;
    public long sla, timeCreated, negTimeCreated;
    public int status, category;

    private String key;

    static final String  SOLVED_PROBLEM = "solved";
    static final String OPEN_PROBLEM = "open";

    public static final int CATEGORY_TRAFFIC = 0;
    public static final int CATEGORY_GARBAGE = 1;
    public static final int CATEGORY_POTHOLES = 2;

    public static final int STATUS_UNSOLVED = 0;
    public static final int STATUS_SOLVED = 1;

    ProblemModel(){

    }

    ProblemModel(String url, int status, double locationX, double locationY, String locationAddress,
                 String creatorKey, long SLA, long timeCreated, String description, int category,
                 String creatorName, String creatorURL, String solutionUrl){

        this.url             = url;
        this.status          = status;
        this.locationX       = locationX;
        this.locationY       = locationY;
        this.creatorKey      = creatorKey;
        this.sla             = SLA;
        this.timeCreated     = timeCreated;
        this.negTimeCreated  = 0-timeCreated;
        this.description     = description;
        this.category        = category;
        this.locationAddress = locationAddress;
        this.creatorName     = creatorName;
        this.creatorURL      = creatorURL;
        this.solutionUrl     = solutionUrl;
    }

    protected ProblemModel(Parcel in) {
        url = in.readString();
        creatorKey = in.readString();
        description = in.readString();
        locationAddress = in.readString();
        creatorName = in.readString();
        creatorURL = in.readString();
        solutionUrl = in.readString();
        locationX = in.readDouble();
        locationY = in.readDouble();
        sla = in.readLong();
        timeCreated = in.readLong();
        negTimeCreated = in.readLong();
        status = in.readInt();
        category = in.readInt();
        key = in.readString();
    }

    @Exclude
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(creatorKey);
        dest.writeString(description);
        dest.writeString(locationAddress);
        dest.writeString(creatorName);
        dest.writeString(creatorURL);
        dest.writeString(solutionUrl);
        dest.writeDouble(locationX);
        dest.writeDouble(locationY);
        dest.writeLong(sla);
        dest.writeLong(timeCreated);
        dest.writeLong(negTimeCreated);
        dest.writeInt(status);
        dest.writeInt(category);
        dest.writeString(key);
    }

    @Exclude
    @Override
    public int describeContents() {
        return 0;
    }

    @Exclude
    public static final Creator<ProblemModel> CREATOR = new Creator<ProblemModel>() {
        @Override
        public ProblemModel createFromParcel(Parcel in) {
            return new ProblemModel(in);
        }

        @Override
        public ProblemModel[] newArray(int size) {
            return new ProblemModel[size];
        }
    };

    @Exclude
    public String getPeriod(){
        long time = timeCreated*1000;
        long after;

        after = sla * 24 * 60 * 60* 1000;

        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yy", Locale.US);
        String fromDate = formatter.format(new Date(time));

        String toDate = formatter.format(new Date(time + after));
        return fromDate + " - " + toDate;
    }

    @Exclude
    public static String getCategory(int category){

        switch(category){
            case CATEGORY_POTHOLES:
                return "Potholes";
            case CATEGORY_GARBAGE:
                return "Garbage";
            case CATEGORY_TRAFFIC:
                return "Traffic";
        }

        return "none";
    }

    @Exclude
    public static int getCategory(String category){
        if(category.equalsIgnoreCase("Potholes")){
            return CATEGORY_POTHOLES;
        }
        else if(category.equalsIgnoreCase("Garbage")){
            return  CATEGORY_GARBAGE;
        }
        else if(category.equalsIgnoreCase("Traffic")){
            return CATEGORY_TRAFFIC;
        }
        else return -1;
    }

    @Exclude
    public String getCategory(){
        return getCategory(category);
    }

    @Exclude
    public String getKey(){
        return this.key;
    }

    @Exclude
    public void setKey(String key){
        this.key = key;
    }
}