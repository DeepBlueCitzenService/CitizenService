package io.github.deepbluecitizenservice.citizenservice;

import android.app.Activity;
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
import android.support.v4.app.Fragment;

import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class PhotoFragment extends Fragment {
    private final String TAG = "PhotoFragment";
    private final static int GALLERY_CALL = 200;
    private final static int CAMERA_CALL = 100;
    private String imagePath ="";
    private boolean hasLocation = false;
    private ImageView mImageView;
    private OnPhotoListener mListener;

    public PhotoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        imagePath= "";
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photo, container, false);

        Button cameraButton = (Button) view.findViewById(R.id.PhotoFragmentCamera);
        Button galleryButton = (Button) view.findViewById(R.id.PhotoFragmentGallery);
        Button uploadButton = (Button) view.findViewById(R.id.PhotoFragmentUpload);
        Button locationButton = (Button) view.findViewById(R.id.PhotoFragmentLocation);

        mImageView = (ImageView) view.findViewById(R.id.PhotoFragmentImageView);

        //Handle button clicks
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Camera button clicked");

                Intent startCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(startCamera, CAMERA_CALL);
            }
        });

        galleryButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d(TAG, "Gallery Button clicked");
                Intent startGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(startGallery, GALLERY_CALL);
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
                if(hasLocation && imagePath.length()>0)
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

    //Get the current location and store it in a global variable
    private void setCurrentLocation(){
        hasLocation= true;
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
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(userName+"/openProblems/problem-"+ts+".jpg");
        Uri file = Uri.fromFile(new File(imagePath));

        //Start uploading in the background
        UploadTask uploadTask = storageRef.putFile(file);

        //Change to home view
        mListener.changeView(0);

        //Handle upload task listeners
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "Upload successful");
                //mListener.onPhotoUploadComplete(true);
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Upload unsuccessful");
                //mListener.onPhotoUploadComplete(false);
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
        void onPhotoUploadComplete(boolean wasSuccessful);
        void changeView(int toWhere);
    }
}
