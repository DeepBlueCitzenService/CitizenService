package io.github.deepbluecitizenservice.citizenservice;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import java.util.List;

import io.github.deepbluecitizenservice.citizenservice.adapter.ExpImageRVAdapter;
import io.github.deepbluecitizenservice.citizenservice.views.ZoomImageView;

public class ExpImageActivity extends AppCompatActivity {

    public static final String URL_LIST = "urlList";
    public static final String IMAGE_PARCEL = "imageBytes";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exp_image);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        ZoomImageView imageView = (ZoomImageView) findViewById(R.id.expanded_image);

        Intent intent = getIntent();
        List<String> urlList = intent.getStringArrayListExtra(URL_LIST);
        byte[] imgBytes = getIntent().getByteArrayExtra(IMAGE_PARCEL);
        Bitmap bmp = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);
        imageView.setImageBitmap(bmp);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.image_recycle_view);
        RecyclerView.LayoutManager manager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView.Adapter adapter = new ExpImageRVAdapter(this, urlList, imageView);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        ImageView closeButton = (ImageView) findViewById(R.id.image_close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}
