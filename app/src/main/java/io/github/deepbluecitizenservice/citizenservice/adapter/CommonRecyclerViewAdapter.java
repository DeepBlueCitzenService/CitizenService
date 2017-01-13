package io.github.deepbluecitizenservice.citizenservice.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.design.widget.Snackbar;
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
import io.github.deepbluecitizenservice.citizenservice.MainActivity;
import io.github.deepbluecitizenservice.citizenservice.MapsActivity;
import io.github.deepbluecitizenservice.citizenservice.R;
import io.github.deepbluecitizenservice.citizenservice.SLANotification;
import io.github.deepbluecitizenservice.citizenservice.SolutionDialogActivity;
import io.github.deepbluecitizenservice.citizenservice.database.ProblemModel;

public class CommonRecyclerViewAdapter extends RecyclerView.Adapter<CommonRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private List<ProblemModel> problemList;
    private HashSet<String> problemIds;
    private String FragmentTAG;
    private RecyclerView recyclerView;

    public CommonRecyclerViewAdapter(RecyclerView rv, Context context, List<ProblemModel> problems, String FragmentTAG){
        this.recyclerView = rv;
        this.context = context;
        this.problemList = problems;
        this.problemIds = new HashSet<>();
        this.FragmentTAG = FragmentTAG;
    }

    public void clear() {
        int size = this.problemList.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                this.problemList.remove(0);
            }

            this.notifyItemRangeRemoved(0, size);
        }
        problemIds.clear();
    }

    public void addProblem(ProblemModel problemModel, String id){
        problemModel.setKey(id);
        problemList.add(problemModel);
        notifyItemInserted(problemList.size() - 1);
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
        holder.categoryTV.setText(problem.getCategory(context));
        holder.periodTV.setText(problem.getPeriod(context));
        holder.descriptionTV.setText(problem.description);

        String status = checkIfProblemIsSolved(problem);
        if(status != null)
            holder.noOfImagesTV.setText(status);
        else
            holder.noOfImagesTV.setVisibility(View.GONE);

        StorageReference refProblem = FirebaseStorage.getInstance().getReference(problem.url);
        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(refProblem)
                .override(400, 300)
                .placeholder(R.drawable.image_placeholder)
                .crossFade()
                .into(holder.imageView);

        Glide.with(context)
                .load(problem.creatorURL)
                .placeholder(R.drawable.ic_person)
                .crossFade()
                .into(holder.userImage);

        setExpandButtonListener(holder.expandButton, holder.descriptionTV);
        setLocationClickListener(holder.locationTV, problem);
        setImageClickListener(holder.imageView, problem);

        if(FragmentTAG.equals(MainActivity.HOME_TAG)) {
            setSolutionButtonListener(holder.addSolutionButton, problem);
        } else{
            holder.addSolutionButton.setVisibility(View.GONE);
        }
    }

    private String checkIfProblemIsSolved(ProblemModel p){
        if(p.status == ProblemModel.STATUS_SOLVED){
            return context.getString(R.string.problem_solved);
        }
        else{
            return null;
        }
    }

    private void setImageClickListener(final ImageView img, final ProblemModel p){
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Bitmap bitmap = ((GlideBitmapDrawable) img.getDrawable()).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] imageBytes = stream.toByteArray();

                    ArrayList<String> imageUrls = new ArrayList<>();
                    if(p.status == ProblemModel.STATUS_SOLVED){
                        imageUrls.add(p.url);
                        imageUrls.add(p.solutionUrl);
                    }

                    Intent intent = new Intent(context, ExpImageActivity.class);
                    intent.putStringArrayListExtra(ExpImageActivity.URL_LIST, imageUrls);
                    intent.putExtra(ExpImageActivity.IMAGE_PARCEL, imageBytes);
                    context.startActivity(intent);
                }
                catch (ClassCastException e) {
                    Snackbar.make(recyclerView, R.string.image_not_loaded, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setLocationClickListener(TextView locationTV, final ProblemModel problem) {
        locationTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MapsActivity.class);
                intent.putExtra(MapsActivity.MAP_PROBLEM, problem);
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

    private void setSolutionButtonListener(final ImageView v, final ProblemModel problem){
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startSolutionDialog = new Intent(context, SolutionDialogActivity.class)
                        .putExtra(SLANotification.PROBLEM_KEY, problem.getKey())
                        .putExtra(SLANotification.URL_KEY, problem.url);

                context.startActivity(startSolutionDialog);
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
        ImageView addSolutionButton;

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
            addSolutionButton = (ImageView) itemView.findViewById(R.id.add_solution_button);
        }
    }
}
