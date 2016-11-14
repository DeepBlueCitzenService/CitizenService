package io.github.deepbluecitizenservice.citizenservice.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;

import io.github.deepbluecitizenservice.citizenservice.MapsActivity;
import io.github.deepbluecitizenservice.citizenservice.R;
import io.github.deepbluecitizenservice.citizenservice.data.Problem;

public class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder>{
    private Context context;
    private List<Problem> problemList;


    public HomeRecyclerViewAdapter(Context context, List <Problem> problemList){
        this.context = context;
        this.problemList = problemList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View card = LayoutInflater.from(context).inflate(R.layout.problem_card, null);
        return new HomeRecyclerViewAdapter.ViewHolder(card);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Problem problem = problemList.get(position);
        HashMap <String, Object> Getter = problem.getDetails();

        holder.userNameTV.setText((String)Getter.get(Problem.USER_NAME));
        holder.locationTV.setText((String)Getter.get(Problem.LOCATIONADDRESS));
        holder.categoryTV.setText((String)Getter.get(Problem.CATEGORY));
        holder.periodTV.setText((String) Getter.get(Problem.TIMECREATED));
        holder.descriptionTV.setText((String)Getter.get(Problem.DESCRIPTION));

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl((String)Getter.get(Problem.URL));
        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(ref)
                .crossFade()
                .into(holder.imageView);

        Glide.with(context)
                .load((String)Getter.get(Problem.USER_URL))
                .crossFade()
                .into(holder.userImage);

        setExpandButtonListener(holder.expandButton, holder.descriptionTV);

        setLocationClickListener(holder.locationTV,
                (String)Getter.get(Problem.USER_NAME), (String)Getter.get(Problem.CATEGORY),
                (Double)Getter.get(Problem.LOCATIONX), (Double)Getter.get(Problem.LOCATIONY),
                (String)Getter.get(Problem.TIMECREATED));

        //setImageClickListener(holder.imageView, problem);
    }

    @Override
    public int getItemCount() {
        return problemList.size();
    }

//    public void setImageClickListener(ImageView image){
//        Bitmap bitmap =Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
//
//    }

    private void setLocationClickListener(TextView locationTV, final String creator, final String category,
                                          final double locationX, final double locationY, final String timeCreated) {
        locationTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = creator + " (" + category + ")";

                Intent intent = new Intent(context, MapsActivity.class);
                intent.putExtra(MapsActivity.MAP_LOC_X, locationX);
                intent.putExtra(MapsActivity.MAP_LOC_Y, locationY);
                intent.putExtra(MapsActivity.MAP_SNIPPET, timeCreated);
                intent.putExtra(MapsActivity.MAP_TITLE, title);

                context.startActivity(intent);
            }
        });
    }

    private void setExpandButtonListener(final ImageView v, final TextView tv) {
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tv.getVisibility() == View.VISIBLE){
                    tv.setVisibility(View.GONE);
                    v.setImageDrawable(ContextCompat
                            .getDrawable(context, R.drawable.ic_keyboard_arrow_down));
                }
                else {
                    tv.setVisibility(View.VISIBLE);
                    v.setImageDrawable(ContextCompat
                            .getDrawable(context, R.drawable.ic_keyboard_arrow_up));
                }
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        String id;

        TextView userNameTV;
        TextView locationTV;
        TextView categoryTV;
        TextView periodTV;
        TextView descriptionTV;
        TextView noOfImagesTV;

        ImageView userImage;
        ImageView imageView;
        ImageView expandButton;

        public ViewHolder(View itemView) {
            super(itemView);
            userNameTV = (TextView) itemView.findViewById(R.id.user_name_tv);
            locationTV = (TextView) itemView.findViewById(R.id.location_tv);
            categoryTV = (TextView) itemView.findViewById(R.id.category_tv);
            periodTV = (TextView) itemView.findViewById(R.id.time_period_tv);
            descriptionTV = (TextView) itemView.findViewById(R.id.description_tv);
            noOfImagesTV = (TextView) itemView.findViewById(R.id.no_of_images_tv);
            userImage = (ImageView) itemView.findViewById(R.id.user_image);
            imageView = (ImageView) itemView.findViewById(R.id.image_view);
            expandButton = (ImageView) itemView.findViewById(R.id.arrow_button);
        }

        public void setId(String id){
            this.id = id;
        }
    }

}