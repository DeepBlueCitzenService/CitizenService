package io.github.deepbluecitizenservice.citizenservice;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import io.github.deepbluecitizenservice.citizenservice.data.Problem;
import io.github.deepbluecitizenservice.citizenservice.database.CustomDatabase;
import io.github.deepbluecitizenservice.citizenservice.permission.StoragePermission;
import io.github.deepbluecitizenservice.citizenservice.service.GPSService;

public class PhotoFragment extends Fragment {
    private final String TAG = "PhotoFragment";
    private final static int GALLERY_CALL = 200;
    private final static int CAMERA_CALL = 100;
    private final static int PICKER_CALL = 400;

    private ImageView mImageView;
    private OnPhotoListener mListener;

    private View view;

    //Set these values before upload is available
    private String imagePath ="", locationAddress = "";
    private boolean hasLocation = false;
    private double locationX = 0, locationY = 0;
    private int category = 0;
    private String description = "";

    private GPSService gpsService;

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
                                setImageCategory(Problem.CATEGORY_TRAFFIC);
                                break;
                            case R.id.radio_garbage:
                                setImageCategory(Problem.CATEGORY_GARBAGE);
                                break;
                            case R.id.radio_potholes:
                                setImageCategory(Problem.CATEGORY_POTHOLES);
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

//        if(imagePath.length()>0 && mImageView!=null){
//            mImageView.setImageBitmap(BitmapFactory.decodeFile(imagePath));
//        }

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
            Log.d(TAG, "Camera activity Result code "+ resultCode);
            if(resultCode == Activity.RESULT_OK){
                handleCameraUpload(data);
                //TODO : TensorFlow Comes Here
                setImageCategory(Problem.CATEGORY_GARBAGE);
            }
        }

        //Get image from Gallery
        if(requestCode== GALLERY_CALL){
            Log.d(TAG, "Gallery activity "+"Result code "+ resultCode);
             if(resultCode== Activity.RESULT_OK) {
                 handleGalleryUpload(data);
                 //TODO : TensorFlow Comes Here
                 setImageCategory(Problem.CATEGORY_GARBAGE);
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

    private void handleCameraUpload(Intent data) {
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
    }

    private void handleGalleryUpload(Intent data){
        imagePath = getFilePathFromGallery(data);

        //Get data URI
        Log.d(TAG, "Picture path: "+imagePath);

        //Change image using setImageBitmap
        if(mImageView!=null) {
            try{
                Glide.with(getActivity())
                        .load(new File(imagePath))
                        .override(450, 300)
                        .centerCrop()
                        .crossFade()
                        .into(mImageView);
            }

            catch(OutOfMemoryError e){
                Log.d(TAG, "Image too large");
            }

            catch (Exception e){
                Log.d(TAG, "Other error");
                e.printStackTrace();
            }
        }
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
        categoryTV.setText(Problem.getCategory(category));
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

        //Get the username to ensure saving to correct folder
        String userName = "";
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //Time stamps are unique
        Long tsLong = System.currentTimeMillis()/1000;
        final String ts = tsLong.toString();
        final int tsInt = Integer.parseInt(ts);

        //Emails are unique
        if(user!=null){
            userName = user.getEmail();
        }

        //Create a unique file reference in FireBase
        StorageReference storageRef = FirebaseStorage.getInstance()
                                            .getReference()
                                            .child(userName+"/openProblems/problem-"+ts+".jpg");

        Uri file = Uri.fromFile(new File(imagePath));

        updateDatabase(userName+"/openProblems/problem-"+ts+".jpg", tsLong);

        //Start uploading in the background
        UploadTask uploadTask = storageRef.putFile(file);

        imagePath = "";
        locationY = -1;
        locationX = -1;
        locationAddress = "";
        hasLocation = false;

        mImageView.setImageBitmap(null);
        TextView locationTV = (TextView) getActivity().findViewById(R.id.location_tv);
        locationTV.setText("");

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
        CustomDatabase db = new CustomDatabase(FirebaseDatabase.getInstance().getReference());

        db.createProblem(url, Problem.STATUS_UNSOLVED, locationX, locationY, locationAddress,
                FirebaseAuth.getInstance().getCurrentUser().getUid(), 604800000L, timeCreated, description,
                category);
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
        Log.d(TAG, "ONDETACH");
        if(gpsService != null)
            gpsService.stopUsingGPS();
        mListener = null;

        imagePath = "";
        locationY = -1;
        locationX = -1;
        locationAddress = "";
        hasLocation = false;

        Log.d(TAG, "Detaching");
//        if(mImageView.getDrawable()!=null) {
//            Log.d(TAG, "Removing Bitmap");
//            mImageView.setImageBitmap(null);
//        }
    }

    public interface OnPhotoListener {
        void changeView(int toWhere);
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
            Log.d(TAG, "Upload button clicked");
            TextView descriptionTV = (TextView) view.findViewById(R.id.problem_description);
            description = String.valueOf(descriptionTV.getText());
            if(imagePath.length() <= 0){
                Snackbar.make(view, "Please Load Image", Snackbar.LENGTH_LONG).show();
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
}
