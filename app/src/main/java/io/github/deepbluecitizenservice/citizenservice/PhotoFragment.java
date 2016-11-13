package io.github.deepbluecitizenservice.citizenservice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

public class PhotoFragment extends Fragment {
    private final String TAG = "PhotoFragment";
    public final static int GALLERY_CALL = 200;
    public final static int CAMERA_CALL = 100;

    private ImageView mImageView;
    //private OnPhotoListener mListener;

    public PhotoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photo, container, false);

        Button cameraButton = (Button) view.findViewById(R.id.PhotoFragmentCamera);
        Button galleryButton = (Button) view.findViewById(R.id.PhotoFragmentGallery);
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
                 String picturepath = getFilePathFromGallery(data);

                 //Get data URI
                 Log.d(TAG, "Picture path: "+picturepath);

                 //Change image using setImageBitmap
                 if(mImageView!=null) {
                     try{
                         Bitmap bitmap = BitmapFactory.decodeFile(picturepath);
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

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnPhotoListener) {
//            mListener = (OnPhotoListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnPhotoListener");
//        }
//    }
//
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }
//
//    public interface OnPhotoListener {
//        void onPhotoInteraction(@Nullable Intent data, int caller);
//    }
}
