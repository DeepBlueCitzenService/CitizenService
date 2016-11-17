package io.github.deepbluecitizenservice.citizenservice;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import io.github.deepbluecitizenservice.citizenservice.database.CustomDatabase;
import io.github.deepbluecitizenservice.citizenservice.database.ProblemModel;
import io.github.deepbluecitizenservice.citizenservice.permission.StoragePermission;
import io.github.deepbluecitizenservice.citizenservice.service.GPSService;
import io.github.deepbluecitizenservice.citizenservice.tensorflow.Classifier;
import io.github.deepbluecitizenservice.citizenservice.tensorflow.ImageClassifier;
import io.github.deepbluecitizenservice.citizenservice.tensorflow.TensorFlow;

public class PhotoFragment extends Fragment {
    private final static String TAG = "PhotoFragment";
    private final static int GALLERY_CALL = 200;
    private final static int CAMERA_CALL = 100;
    private final static int PICKER_CALL = 400;

    private ImageView mImageView;
    private OnPhotoListener mListener;

    private View view;

    //Set these values before upload is available
    private String imagePath ="", locationAddress = "";
    private boolean hasLocation = false;
    private boolean hasCategory = false;
    private double locationX = 0, locationY = 0;
    private int category = 0;
    private String description = "";

    private GPSService gpsService;

    private TensorFlow tensorFlow;

    public PhotoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "ONCREATEVIEW"+ imagePath +"Length: "+ imagePath.length());

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.activity_add, container, false);

        final Dialog imageSelectDialog = new Dialog(getContext());
        final Dialog categorySelectDialog = new Dialog(getContext());

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.problem_fab);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fab.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.colorAccent)));
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkStoragePermissions()){
                    imageSelectDialog.setContentView(R.layout.dialog_source);

                    ImageView cameraButton = (ImageView) imageSelectDialog.findViewById(R.id.dialog_camera_button);
                    ImageView galleryButton = (ImageView) imageSelectDialog.findViewById(R.id.dialog_gallery_button);

                    cameraButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "Camera button clicked");
                            Intent startCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(startCamera, CAMERA_CALL);
                            imageSelectDialog.dismiss();
                        }
                    });

                    galleryButton.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v){
                            Log.d(TAG, "Gallery Button clicked");
                            Intent startGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(startGallery, GALLERY_CALL);
                            imageSelectDialog.dismiss();
                        }
                    });

                    imageSelectDialog.show();
                }
            }
        });

        ImageView locationButton = (ImageView) view.findViewById(R.id.problem_location_edit);

        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Location button clicked");
                gpsService = new GPSService(getContext(), view);
                if(gpsService.isGPSPermissionGranted() && gpsService.isGPSEnabled()){
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    try {
                        startActivityForResult(builder.build(getActivity()), PICKER_CALL);
                    } catch (GooglePlayServicesRepairableException |
                            GooglePlayServicesNotAvailableException e) {
                        setCurrentLocation(gpsService);
                    }
                }
            }
        });

        ImageView categoryButton = (ImageView) view.findViewById(R.id.problem_category_edit);

        categoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categorySelectDialog.setContentView(R.layout.dialog_category);

                Button okButton = (Button) categorySelectDialog.findViewById(R.id.dialog_category_ok);
                Button cancelButton = (Button) categorySelectDialog.findViewById(R.id.dialog_category_cancel);

                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        RadioGroup radioGroup = (RadioGroup) categorySelectDialog.findViewById(R.id.dialog_category_radiogroup);
                        int idx = radioGroup.getCheckedRadioButtonId();
                        switch (idx){
                            case R.id.radio_traffic:
                                setImageCategory(ProblemModel.CATEGORY_TRAFFIC);
                                hasCategory = true;
                                break;
                            case R.id.radio_garbage:
                                setImageCategory(ProblemModel.CATEGORY_GARBAGE);
                                hasCategory = true;
                                break;
                            case R.id.radio_potholes:
                                setImageCategory(ProblemModel.CATEGORY_POTHOLES);
                                hasCategory = true;
                                break;
                        }
                        categorySelectDialog.dismiss();
                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        categorySelectDialog.dismiss();
                    }
                });

                categorySelectDialog.show();
            }
        });

        mImageView = (ImageView) view.findViewById(R.id.problem_image);

        return view;
    }

    private boolean checkStoragePermissions() {
        StoragePermission permission = new StoragePermission(getContext());
        permission.askPermissions(view);
        return permission.isGranted();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== CAMERA_CALL){
            if(resultCode == Activity.RESULT_OK){
                Bitmap bitmap = handleCameraUpload(data);
                setImageCategory(bitmap);
            }
        }

        //Get image from Gallery
        if(requestCode== GALLERY_CALL){
             if(resultCode== Activity.RESULT_OK) {
                 Bitmap bitmap = handleGalleryUpload(data);
                 setImageCategory(bitmap);
             }
        }

        if(requestCode == PICKER_CALL){
            if(resultCode== Activity.RESULT_OK) {
                Place place = PlacePicker.getPlace(getContext(), data);
                LatLng latLng = place.getLatLng();
                setCurrentLocation(latLng.latitude, latLng.longitude);
            }
        }
    }

    private Bitmap handleCameraUpload(Intent data) {
        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
        Log.d(TAG, (bitmap==null? "Bitmap not loaded":"Bitmap loaded"));
        mImageView.setImageBitmap(bitmap);
        try {
            File outputDir = getContext().getCacheDir();
            File outFile = new File(outputDir,"tmpfile.jpg");
            FileOutputStream fos = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            imagePath = outFile.getPath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private Bitmap handleGalleryUpload(Intent data){
        imagePath = getFilePathFromGallery(data);

        //Get data URI
        Log.d(TAG, "Picture path: "+imagePath);

        //Change image using setImageBitmap
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        mImageView.setImageBitmap(bitmap);

        return bitmap;
    }

    private String getFilePathFromGallery(Intent data){
        Uri selectedImage = data.getData();
        String picturepath = "";

        //Get filepath
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContext().getContentResolver().query(selectedImage, filePathColumn, null, null, null);

        if(cursor!=null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturepath = cursor.getString(columnIndex);
            cursor.close();
        }

        return picturepath;
    }

    private void setCurrentLocation(GPSService gpsService){
        gpsService.getLocation();
        setCurrentLocation(gpsService.getLatitude(), gpsService.getLongitude());
    }

    private void setCurrentLocation(double locX, double locY){
        TextView locationTV = (TextView) view.findViewById(R.id.problem_location_tv);

        locationX = locX;
        locationY = locY;

        locationAddress = String.valueOf(locX + " '" + locY);
        setTVLocation(locationTV);

        hasLocation = true;

        if(locationX == 0 && locationY == 0){
            locationTV.setText("Please Select Location");
            hasLocation = false;
        }
    }

    public void setTVLocation(final TextView tv){
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... voids) {
                String location = locationX + ", " + locationY;
                try {
                    Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                    List<Address> listAddresses = geocoder.getFromLocation(locationX, locationY, 1);
                    if(listAddresses != null && listAddresses.size() > 0){
                        location = listAddresses.get(0).getAddressLine(1);
                        locationAddress = location;
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

    private void setImageCategory(int imageCategory){
        category = imageCategory;
        TextView categoryTV = (TextView) view.findViewById(R.id.problem_category_tv);
        categoryTV.setText(ProblemModel.getCategory(category));
    }

    private void setImageCategory(final Bitmap image){
        final TextView categoryTV = (TextView) view.findViewById(R.id.problem_category_tv);

        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Identifying Image");

        new AsyncTask<Void, Void, Boolean>() {

            private int result = 0;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.show();
                if(tensorFlow == null){
                    tensorFlow = new TensorFlow(getContext().getAssets(), new ImageClassifier());
                }
            }
            @Override
            protected Boolean doInBackground(Void... voids) {
                if(tensorFlow != null){
                    try {
                        tensorFlow.initialize();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if(tensorFlow != null){
                    List<Classifier.Recognition> classifiesList = tensorFlow.classify(image);
                    result = Integer.parseInt(classifiesList.get(0).getId());
                    return true;
                }

                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPostExecute(success);
                category = result;
                categoryTV.setText(success ? ProblemModel.getCategory(result) : "Identification failed");
                hasCategory = success;

                //REMOVE WHEN NOT DEBUGGING!
                //hasCategory = true;

                progressDialog.dismiss();
            }
        }.execute();
    }

    //Handle uploads
    private void handleImageUpload(){
        //Create notifications for file uploads
        final NotificationManager mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getContext());
        mBuilder
                .setContentTitle("Uploading image")
                .setContentText("Upload in Progress")
                .setSmallIcon(R.drawable.ic_file_upload);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //Time stamps are unique
        final Long tsLong = System.currentTimeMillis()/1000;
        final String ts = tsLong.toString();
        final int tsInt = Integer.parseInt(ts);
        final String userName = user==null? "guest": user.getEmail();

        //Create a unique file reference in FireBase
        StorageReference storageRef = FirebaseStorage.getInstance()
                                            .getReference()
                                            .child(userName+"/openProblems/problem-"+ts+".jpg");

        Uri file = Uri.fromFile(new File(imagePath));

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                updateDatabase(userName +"/openProblems/problem-"+ts+".jpg", tsLong);
                return null;
            }
        }.execute();

        //Start uploading in the background
        UploadTask uploadTask = storageRef.putFile(file);

        imagePath = "";
        locationY = -1;
        locationX = -1;
        locationAddress = "";
        hasLocation = false;
        hasCategory = false;

        mImageView.setImageBitmap(null);

        //Change to home view
        mListener.changeView(0);

        //Handle upload task listeners
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "Upload successful");
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Upload unsuccessful");
            }
        })
        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                mBuilder.setContentText("Upload complete")
                        .setProgress(0,0,false);
                mNotificationManager.notify(tsInt, mBuilder.build());

            }
        })
        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                Long progress = Math.round(100.0 * taskSnapshot.getBytesTransferred());
                mBuilder.setProgress(100, progress.intValue(), false);
                mNotificationManager.notify(tsInt, mBuilder.build());
            }
        });
    }

    //Update database with the current problem, attaching it to the user as well
    private void updateDatabase(String url, Long timeCreated){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        CustomDatabase db = new CustomDatabase(ref);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        long SLA= 0;
        String categoryString= "none";

        switch(category){
            case ProblemModel.CATEGORY_GARBAGE:
                SLA= 7;
                categoryString = "Garbage";
                break;
            case ProblemModel.CATEGORY_POTHOLES:
                SLA = 15;
                categoryString = "Potholes";
                break;
            case ProblemModel.CATEGORY_TRAFFIC:
                SLA = 92;
                categoryString = "Traffic";
                break;
        }

        String key = ref.child("problems").push().getKey();
        db.createProblem(key, url, ProblemModel.STATUS_UNSOLVED, locationX, locationY, locationAddress,
                user.getUid(), SLA, timeCreated, description,
                category, user.getDisplayName(), user.getPhotoUrl().toString(), "");

        //FOR DEBUGGING: 2 SECOND DELAY FOR NOTIFICATION
        //setSLANotification(( 2 + (timeCreated))*1000, "Potholes", locationAddress, key, url);

        setSLANotification(((SLA * 24 * 60 * 60) + (timeCreated))*1000, categoryString, locationAddress, key, url);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "ONATTACH");
        if (context instanceof OnPhotoListener) {
            mListener = (OnPhotoListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPhotoListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(gpsService != null)
            gpsService.stopUsingGPS();
        mListener = null;

        imagePath = "";
        locationY = -1;
        locationX = -1;
        locationAddress = "";
        hasLocation = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.add_toolbar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.toolbar_upload) {
            TextView descriptionTV = (TextView) view.findViewById(R.id.problem_description);
            description = String.valueOf(descriptionTV.getText());
            if(imagePath.length() <= 0){
                Snackbar.make(view, "Please Load Image", Snackbar.LENGTH_LONG).show();
            }
            else if(!hasCategory){
                Snackbar.make(view, "Please Select Category", Snackbar.LENGTH_LONG).show();
            }
            else if(!hasLocation){
                Snackbar.make(view, "Please Select Location", Snackbar.LENGTH_LONG).show();
            }
            else {
                handleImageUpload();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setSLANotification(long time, String category, String location, String problemKey, String url){
        Intent setNotificationIntent = new Intent(getActivity(), SLANotification.class);
        setNotificationIntent.putExtra(SLANotification.CATEGORY, category);
        setNotificationIntent.putExtra(SLANotification.LOCATION, location);
        setNotificationIntent.putExtra(SLANotification.PROBLEM_KEY, problemKey);
        setNotificationIntent.putExtra(SLANotification.URL_KEY, url);

        PendingIntent notifyIntent = PendingIntent
                .getBroadcast(getActivity(), (int)System.currentTimeMillis() , setNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) getActivity().getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, time , notifyIntent);
    }

    public interface OnPhotoListener {
        void changeView(int toWhere);
    }
}
