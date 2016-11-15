package io.github.deepbluecitizenservice.citizenservice.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import io.github.deepbluecitizenservice.citizenservice.ExpImageActivity;
import io.github.deepbluecitizenservice.citizenservice.MapsActivity;
import io.github.deepbluecitizenservice.citizenservice.R;
import io.github.deepbluecitizenservice.citizenservice.database.ProblemModel;

public class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private List<ProblemModel> problemList;
    private HashSet<String> problemIds;

    public HomeRecyclerViewAdapter(Context context, List<ProblemModel> problems){
        this.context = context;
        this.problemList = problems;
        this.problemIds = new HashSet<>();
    }

    public void addProblem(ProblemModel problemModel, String id){
        problemList.add(problemModel);
        notifyDataSetChanged();
        problemIds.add(id);
    }

    public boolean isAdded(String key){
        return problemIds.contains(key);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View card = LayoutInflater.from(context).inflate(R.layout.problem_card, null);
        return new ViewHolder(card);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ProblemModel problem = problemList.get(position);

        holder.userNameTV.setText(problem.creatorName);
        holder.locationTV.setText(problem.locationAddress);
        holder.categoryTV.setText(problem.getCategory());
        holder.periodTV.setText(problem.getPeriod());
        holder.descriptionTV.setText(problem.description);

        String noOfImagesString = getNoOfImagesText(problem);
        if(noOfImagesString != null)
            holder.noOfImagesTV.setText(noOfImagesString);
        else
            holder.noOfImagesTV.setVisibility(View.GONE);

        StorageReference refProblem = FirebaseStorage.getInstance().getReference(problem.url);
        Glide.with(context).using(new FirebaseImageLoader()).load(refProblem).into(holder.imageView);
        Glide.with(context).load(problem.creatorURL).into(holder.userImage);

        setExpandButtonListener(holder.expandButton, holder.descriptionTV);
        setLocationClickListener(holder.locationTV, problem);
        setImageClickListener(holder.imageView, problem);
    }

    private String getNoOfImagesText(ProblemModel p){
        //TODO: Needed if we have multiple images
        int no = 0;
        if(no == 0) return null;
        else if(no == 1) return "+1 Image";
        else return "+" + no + " Images";
    }

    private void setImageClickListener(final ImageView img, final ProblemModel p){
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = ((GlideBitmapDrawable) img.getDrawable()).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] imageBytes = stream.toByteArray();

                Intent intent = new Intent(context, ExpImageActivity.class);
                intent.putStringArrayListExtra(ExpImageActivity.URL_LIST, new ArrayList<String>());
                intent.putExtra(ExpImageActivity.IMAGE_PARCEL, imageBytes);
                context.startActivity(intent);
            }
        });
    }

    private void setLocationClickListener(TextView locationTV, final ProblemModel problem) {
        locationTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = problem.creatorName + " (" + problem.getCategory() + ")";

                Intent intent = new Intent(context, MapsActivity.class);
                intent.putExtra(MapsActivity.MAP_LOC_X, problem.locationX);
                intent.putExtra(MapsActivity.MAP_LOC_Y, problem.locationY);
                intent.putExtra(MapsActivity.MAP_SNIPPET, problem.getPeriod());
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

    @Override
    public int getItemCount() {
        return problemList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView userNameTV;
        TextView locationTV;
        TextView categoryTV;
        TextView periodTV;
        TextView descriptionTV;
        TextView noOfImagesTV;

        ImageView userImage;
        ImageView imageView;
        ImageView expandButton;

        ViewHolder(View itemView) {
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
    }
}
