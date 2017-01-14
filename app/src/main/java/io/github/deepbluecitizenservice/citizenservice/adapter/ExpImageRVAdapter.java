package io.github.deepbluecitizenservice.citizenservice.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import io.github.deepbluecitizenservice.citizenservice.R;

public class ExpImageRVAdapter extends RecyclerView.Adapter<ExpImageRVAdapter.ViewHolder> {

    private Context context;
    private List<String> imageUrlList;
    private ImageView expandedImage;
    private View baseView;

    public ExpImageRVAdapter(Context context, List<String> bitmapList, ImageView expandedImage, View baseView){
        this.context = context;
        this.imageUrlList = bitmapList;
        this.expandedImage = expandedImage;
        this.baseView = baseView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int size = parent.getHeight();
        ImageView imageView = new ImageView(context);
        ViewGroup.LayoutParams lp = new RecyclerView.LayoutParams(size, size);
        imageView.setLayoutParams(lp);
        return new ViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        StorageReference imageRef = FirebaseStorage.getInstance()
                .getReference(imageUrlList.get(position));

        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(imageRef)
                .asBitmap()
                .placeholder(R.drawable.image_placeholder_white)
                .into(holder.imageView);

        setImageClickListener(holder.imageView);
    }

    private void setImageClickListener(final ImageView imageView){
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                    expandedImage.setImageBitmap(bitmap);
                }
                catch (ClassCastException e){
                    Snackbar.make(baseView, R.string.image_not_loaded, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUrlList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
      }
    }
}
