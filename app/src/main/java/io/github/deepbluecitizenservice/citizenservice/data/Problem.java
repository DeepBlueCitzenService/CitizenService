package io.github.deepbluecitizenservice.citizenservice.data;

import android.graphics.Bitmap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Problem {

    private long id;
    private int status;
    public double locationX;
    public double locationY;
    private String locationAddress;
    private String creator;
    private int category;
    private long sla; // Time in milli-seconds
    private long timeCreated; // Time in milli-seconds
    private String description;
    private Bitmap mainImage;
    private ArrayList<String> imageUrls;


    public static final int STATUS_UNSOLVED = 0;
    public static final int STATUS_SOLVED = 1;

    public static final int CATEGORY_TRAFFIC = 0;
    public static final int CATEGORY_GARBAGE = 1;
    public static final int CATEGORY_POTHOLES = 2;

    public Problem(int id, int status, double locX, double locY,
                   String address, String creator, int category, long sla, long timeCreated,
                   String description, Bitmap mainImage, ArrayList<String> imageUrls){
        this.id = id;
        this.status = status;
        this.locationX = locX;
        this.locationY = locY;
        this.locationAddress = address;
        this.creator = creator;
        this.category = category;
        this.sla = sla;
        this.timeCreated = timeCreated;
        this.description = description;
        this.mainImage = mainImage;
        this.imageUrls = imageUrls;
    }

    public String getCreator() {
        //TODO : Make sure creator is true name of user
        return creator;
    }

    public String getRawLocation() {
        return locationX + ", " + locationY;
    }

    public String getCategory() {
        return getCategory(this.category);
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

    public String getPeriod(){
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yy", Locale.US);
        String fromDate = formatter.format(new Date(timeCreated));
        String toDate = formatter.format(new Date(timeCreated + sla));
        return fromDate + " - " + toDate;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<String> getImageUrls() {
        return imageUrls;
    }

    public Bitmap getMainImage() {
        return mainImage;
    }

    public int getNoOfImages(){
        if(imageUrls == null) return 0;
        return imageUrls.size();
    }

    public String getLocationAddress() {
        return locationAddress;
    }
}
