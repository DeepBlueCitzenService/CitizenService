package io.github.deepbluecitizenservice.citizenservice.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import io.github.deepbluecitizenservice.citizenservice.R;
import io.github.deepbluecitizenservice.citizenservice.data.Problem;
import io.github.deepbluecitizenservice.citizenservice.database.ProblemModel;

public class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder>{
    private Context context;
    private List<ProblemModel> problemList;


    public HomeRecyclerViewAdapter(){

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View card = LayoutInflater.from(context).inflate(R.layout.problem_card, null);
        return new HomeRecyclerViewAdapter.ViewHolder(card);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ProblemModel problem = problemList.get(position);
        HashMap <String, Object> Getter = problem.getDetails();

        holder.userNameTV.setText(Getter.get(ProblemModel.));
        holder.locationTV.setText(problem.getRawLocation());
        holder.categoryTV.setText(problem.getCategory());
        holder.periodTV.setText(problem.getPeriod());
        holder.descriptionTV.setText(problem.getDescription());

    }

    @Override
    public int getItemCount() {
        return 0;
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