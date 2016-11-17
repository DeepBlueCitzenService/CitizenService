package io.github.deepbluecitizenservice.citizenservice;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import io.github.deepbluecitizenservice.citizenservice.database.CustomDatabase;

public class SolutionDialogActivity extends AppCompatActivity {
    private String imageKey, mSolutionImagePath="";

    private ImageView mSolutionImageView;
    private LinearLayout mButtons, mImageLayout;

    private FABProgressCircle fabCircle;
    private FloatingActionButton mFab;
    private  boolean isClicked;

    private final static int CAMERA_CALL = 100, GALLERY_CALL=200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_solution);

        Bundle params = getIntent().getExtras();

        ImageView problemImage, cameraButton, galleryButton;

        mFab = (FloatingActionButton) findViewById(R.id.fab_problem_dialog);
        mFab.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int color = params.getInt(SettingsFragment.SP_THEME);
            mFab.setBackgroundTintList(ColorStateList.valueOf(color));
        }

        mButtons     = (LinearLayout) findViewById(R.id.solution_dialog_button_container);
        mImageLayout = (LinearLayout) findViewById(R.id.solution_image_container);

        problemImage       = (ImageView) findViewById(R.id.dialog_problem_image_view);
        mSolutionImageView = (ImageView) findViewById(R.id.dialog_solution_image_view);
        cameraButton       = (ImageView) findViewById(R.id.solution_dialog_camera);
        galleryButton      = (ImageView) findViewById(R.id.solution_dialog_gallery);

        String imageUrl = (String) params.get(SLANotification.URL_KEY);
        imageKey        = (String) params.get(SLANotification.PROBLEM_KEY);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(startCamera, CAMERA_CALL);
            }
        });

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(startGallery, GALLERY_CALL);
            }
        });

        fabCircle = (FABProgressCircle) findViewById(R.id.solution_dialog_progress_fab);

        mFab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(mSolutionImagePath.length()>0 && !isClicked) {
                    isClicked = true;
                    fabCircle.show();
                    mFab.setOnClickListener(null);

                    new AsyncTask<Void, Void, Void>(){
                        @Override
                        protected Void doInBackground(Void... params) {
                            handleSolutionUpload(imageKey);
                            return null;
                        }
                    }.execute();
                }
            }
        });

        StorageReference ref = FirebaseStorage.getInstance().getReference(imageUrl);
        Glide
                .with(this)
                .using(new FirebaseImageLoader())
                .load(ref)
                .centerCrop()
                .crossFade()
                .into(problemImage);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== CAMERA_CALL){
            if(resultCode == Activity.RESULT_OK){
                Bitmap bitmap = handleCameraUpload(data);

                mButtons.setVisibility(View.GONE);
                mImageLayout.setVisibility(View.VISIBLE);
                //TODO:
                //Analyse then allow upload
                //Probably should start a progress dialog here
                //with async task
                if(analyseImage(bitmap)){
                    mFab.setVisibility(View.VISIBLE);
                }
            }
        }

        //Get image from Gallery
        if(requestCode== GALLERY_CALL){
            if(resultCode== Activity.RESULT_OK) {
                Bitmap bitmap = handleGalleryUpload(data);

                mButtons.setVisibility(View.GONE);
                mImageLayout.setVisibility(View.VISIBLE);

                if(analyseImage(bitmap)){
                    mFab.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public boolean analyseImage(Bitmap bitmap){
        //TODO: Check whether solution is done
        return true;
    }

    private Bitmap handleCameraUpload(Intent data) {
        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
        mSolutionImageView.setImageBitmap(bitmap);
        try {
            File outputDir = getCacheDir();
            File outFile = new File(outputDir,"tmpfile.jpg");
            FileOutputStream fos = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            mSolutionImagePath = outFile.getPath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private Bitmap handleGalleryUpload(Intent data){
        mSolutionImagePath = getFilePathFromGallery(data);
        //Change image using setImageBitmap
        Bitmap bitmap = BitmapFactory.decodeFile(mSolutionImagePath);
        mSolutionImageView.setImageBitmap(bitmap);

        return bitmap;
    }

    private String getFilePathFromGallery(Intent data){
        Uri selectedImage = data.getData();
        String picturePath = "";

        //Get filepath
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);

        if(cursor!=null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();
        }

        return picturePath;
    }

    private void handleSolutionUpload(final String problemId){

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final CustomDatabase db = new CustomDatabase(FirebaseDatabase.getInstance().getReference());

        //Time stamps are unique
        final Long tsLong = System.currentTimeMillis()/1000;
        final String ts = tsLong.toString();
        final String userName = user==null? "guest": user.getEmail();

        //Create a unique file reference in FireBase
        final StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference()
                .child(userName+"/solvedProblems/problem-"+ts+".jpg");

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                db.updateProblemToSolved(user.getUid(),
                        problemId, userName +"/solvedProblems/problem-"+ts+".jpg", tsLong);

                return null;
            }
        }.execute();

        Uri file = Uri.fromFile(new File(mSolutionImagePath));
        UploadTask uploadTask = storageRef.putFile(file);

        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                fabCircle.beginFinalAnimation();

                try {
                    Thread.sleep(500);
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }

                finish();
            }
        });
    }
}