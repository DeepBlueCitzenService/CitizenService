package io.github.deepbluecitizenservice.citizenservice.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import io.github.deepbluecitizenservice.citizenservice.R;
import io.github.deepbluecitizenservice.citizenservice.database.ProblemModel;

public class MapInfoAdapter implements GoogleMap.InfoWindowAdapter {

    private Context context;

    private Map<Marker, Bitmap> problemImages = new HashMap<>();
    private Map<Marker, Target<Bitmap>> problemTargets = new HashMap<>();

    private Map<Marker, Bitmap> userImages = new HashMap<>();
    private Map<Marker, Target<Bitmap>> userTargets = new HashMap<>();

    public MapInfoAdapter(Context context) {
        this.context = context;
    }

    @Override
    public View getInfoContents(final Marker marker) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        View view = LayoutInflater.from(context).inflate(R.layout.map_info_view, null);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams
                ((int) (metrics.widthPixels * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(params);
        
        TextView userNameTV = (TextView) view.findViewById(R.id.map_info_user_name_tv);
        TextView periodTV = (TextView) view.findViewById(R.id.map_info_period_tv);
        TextView categoryTV = (TextView) view.findViewById(R.id.map_info_category_tv);
        TextView descriptionTV = (TextView) view.findViewById(R.id.map_info_description_tv);

        ImageView userIV = (ImageView) view.findViewById(R.id.map_info_user_image);
        ImageView problemIV = (ImageView) view.findViewById(R.id.map_info_image_view);

        ProblemModel problem = (ProblemModel) marker.getTag();
        
        userNameTV.setText(problem.creatorName);
        periodTV.setText(problem.getPeriod());
        categoryTV.setText(problem.getCategory(context));
        descriptionTV.setText(problem.description);

        StorageReference refProblem = FirebaseStorage.getInstance().getReference(problem.url);

        Bitmap problemImage = problemImages.get(marker);
        Bitmap userImage = userImages.get(marker);

        if(problemImage == null){
            Glide.with(context).using(new FirebaseImageLoader()).load(refProblem).asBitmap().centerCrop().into(getTarget(marker, true));
        }
        else {
            problemIV.setImageBitmap(problemImage);
        }

        if(userImage == null){
            Glide.with(context).load(problem.creatorURL).asBitmap().into(getTarget(marker, false));
        }
        else {
            userIV.setImageBitmap(userImage);
        }

        return view;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    private Target<Bitmap> getTarget(Marker marker, boolean isProblem) {
        Target<Bitmap> target;
        if(isProblem) target = problemTargets.get(marker);
        else target = userTargets.get(marker);
        if (target == null) {
            target = new InfoTarget(marker, isProblem);
            if(isProblem) problemTargets.put(marker, target);
            else userTargets.put(marker, target);
        }
        return target;
    }

    private class InfoTarget extends SimpleTarget<Bitmap> {
        Marker marker;
        boolean isProblem;
        InfoTarget(Marker marker, boolean isProblem) {
            this.marker = marker;
            this.isProblem = isProblem;
        }
        @Override public void onLoadCleared(Drawable placeholder) {
            if(isProblem) problemImages.remove(marker);
            else userImages.remove(marker);
        }
        @Override public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
            if(isProblem) problemImages.put(marker, resource);
            else userImages.put(marker, resource);
            marker.showInfoWindow();
        }
    }
}
