package io.github.deepbluecitizenservice.citizenservice.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Problem {

    private Context context;

    private String id;
    private int status;
    public double locationX;
    public double locationY;
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

    public Problem(Context context, String id, int status,
                   double locX, double locY, String creator,
                   int category, long sla, long timeCreated,
                   String description, Bitmap mainImage, ArrayList<String> imageUrls){
        this.context = context;
        this.id = id;
        this.status = status;
        this.locationX = locX;
        this.locationY = locY;
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

    public void setTVLocation(final TextView tv){
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... voids) {
                String location = getRawLocation();
                try {
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    List<Address> listAddresses = geocoder.getFromLocation(locationX, locationY, 1);
                    if(listAddresses != null && listAddresses.size() > 0){
                            location = listAddresses.get(0).getAddressLine(1);
                    }
                } catch (IOException e) {
                    return location;
                }
                return location;
            }

            @Override
            protected void onPostExecute(String result) {
                tv.setText(result);
            }
        }.execute();
    }

    public String getCategory() {
        String result = null;
        switch (this.category){
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

    public String getId(){
        return id;
    }
}
