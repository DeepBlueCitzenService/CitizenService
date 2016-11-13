package io.github.deepbluecitizenservice.citizenservice;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import io.github.deepbluecitizenservice.citizenservice.data.Problem;
import io.github.deepbluecitizenservice.citizenservice.database.CustomDatabase;

public class PhotoFragment extends Fragment {
    private final String TAG = "PhotoFragment";
    private final static int GALLERY_CALL = 200;
    private final static int CAMERA_CALL = 100;

    private ImageView mImageView;
    private OnPhotoListener mListener;

    //Set these values before upload is available
    private String imagePath ="", locationAddress = "";
    private boolean hasLocation = false, hasCategory = false;
    private double locationX, locationY;
    private int category;

    public PhotoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        imagePath= "";
        hasLocation = false;
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_add, container, false);

        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_source);

        ImageView cameraButton = (ImageView) dialog.findViewById(R.id.dialog_camera_button);
        ImageView galleryButton = (ImageView) dialog.findViewById(R.id.dialog_gallery_button);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.problem_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        Toolbar toolbar = ((MainActivity)this.getActivity()).getToolbar();
        ImageView uploadButton = (ImageView) toolbar.findViewById(R.id.toolbar_upload);

        ImageView locationButton = (ImageView) view.findViewById(R.id.problem_location_edit);

        mImageView = (ImageView) view.findViewById(R.id.problem_image);

        //Handle button clicks
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Camera button clicked");

                Intent startCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(startCamera, CAMERA_CALL);
                dialog.dismiss();
            }
        });

        galleryButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d(TAG, "Gallery Button clicked");
                Intent startGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(startGallery, GALLERY_CALL);
                dialog.dismiss();
            }
        });

        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Location button clicked");
                setCurrentLocation();
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Upload button clicked");
                if(hasLocation && imagePath.length()>0 && hasCategory)
                    handleImageUpload();
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== CAMERA_CALL){
            Log.d(TAG, "Camera activity Result code "+ resultCode);
        }

        //Get image from Gallery
        if(requestCode== GALLERY_CALL){
            Log.d(TAG, "Gallery activity "+"Result code "+ resultCode);
             if(resultCode== Activity.RESULT_OK) {
                 handleGalleryUpload(data);
             }
        }
    }

    private void handleGalleryUpload(Intent data){
        imagePath = getFilePathFromGallery(data);

        //Get data URI
        Log.d(TAG, "Picture path: "+imagePath);

        //Change image using setImageBitmap
        if(mImageView!=null) {
            try{
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                Log.d(TAG, (bitmap==null? "Bitmap not loaded":"Bitmap loaded"));
                mImageView.setImageBitmap(bitmap);
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
            int columnindex = cursor.getColumnIndex(filePathColumn[0]);
            picturepath = cursor.getString(columnindex);
            cursor.close();
        }

        return picturepath;
    }

    //TODO: Get the current location and store it in a global variable
    private void setCurrentLocation(){
        locationAddress = "Bandra Kurla Complex";
        locationX = 3.333;
        locationY = 44.5555;
        hasLocation= true;

        //TODO: START DEBUG BLOCK: Remove this after implementation
        getImageCategory();
        //TODO: END DEBUG BLOCK: Remove this after implementation
    }

    //TODO: Set category
    private void getImageCategory(){
        hasCategory = true;
        category = Problem.CATEGORY_GARBAGE;
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
                FirebaseAuth.getInstance().getCurrentUser().getUid(), 7, timeCreated, "A problem",
                Problem.CATEGORY_POTHOLES);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
        mListener = null;
    }

    public interface OnPhotoListener {
        void changeView(int toWhere);
    }
}
