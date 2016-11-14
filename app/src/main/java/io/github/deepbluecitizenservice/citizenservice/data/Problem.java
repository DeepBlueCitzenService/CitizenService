package io.github.deepbluecitizenservice.citizenservice.data;

import android.graphics.Bitmap;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class Problem {
    private String url;
    private String userURL;
    private String id;
    private int status;

    public double locationX;
    public double locationY;
    private String locationAddress;
    private String userName;
    private String creatorKey;
    private String category;
    private String creator;

    private long sla; // Time in milli-seconds
    private String timeCreated; // Time in milli-seconds
    private String description;
    private Bitmap mainImage;


    public static final int STATUS_UNSOLVED = 0;
    public static final int STATUS_SOLVED = 1;

    public static final int CATEGORY_TRAFFIC = 0;
    public static final int CATEGORY_GARBAGE = 1;
    public static final int CATEGORY_POTHOLES = 2;

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
            CATEGORY    = "category",
            USER_URL    = "userUrl",
            USER_NAME   = "userName";

    public Problem(String url, String id, int status, double locationX, double locationY, String locationAddress,
                   String creatorKey, long SLA, long timeCreated, String description, int category,
                   String userURL, String userName){
        this.url             = (url);
        this.id              = id;
        this.status          = status;
        this.locationX       = locationX;
        this.locationY       = locationY;
        this.creatorKey      = creatorKey;
        this.sla             = SLA;
        this.timeCreated     = setPeriod(timeCreated);
        this.description     = description;
        this.category        = getCategory(category);
        this.locationAddress = locationAddress;
        this.userURL         = (userURL);
        this.userName        = userName;
    }

    public static String getCategory(int category){
        String result = null;
        switch (category){
            case CATEGORY_TRAFFIC:
                result =  "Traffic";
                break;
            case CATEGORY_GARBAGE:
                result = "Garbage";
                break;
            case CATEGORY_POTHOLES:
                result = "Potholes";
                break;
        }
        return result;
    }

    private String setPeriod(long timeCreated){
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yy", Locale.US);
        String fromDate = formatter.format(new Date(timeCreated));
        String toDate = formatter.format(new Date(timeCreated + sla));
        return fromDate + " - " + toDate;
    }

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
        result.put(USER_URL, userURL);
        result.put(USER_NAME, userName);
        return  result;
    }
}
