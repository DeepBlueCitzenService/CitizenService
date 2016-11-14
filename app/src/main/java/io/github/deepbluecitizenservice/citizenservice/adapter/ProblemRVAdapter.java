//package io.github.deepbluecitizenservice.citizenservice.adapter;
//
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import java.io.ByteArrayOutputStream;
//import java.util.List;
//
//import io.github.deepbluecitizenservice.citizenservice.ExpImageActivity;
//import io.github.deepbluecitizenservice.citizenservice.MapsActivity;
//import io.github.deepbluecitizenservice.citizenservice.R;
//import io.github.deepbluecitizenservice.citizenservice.data.Problem;
//
//public class ProblemRVAdapter extends RecyclerView.Adapter<ProblemRVAdapter.ViewHolder> {
//
//    private Context context;
//    private List<Problem> problemList;
//
//    public ProblemRVAdapter(Context context, List<Problem> problems){
//        this.context = context;
//        this.problemList = problems;
//    }
//
//    @Override
//    public ProblemRVAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View card = LayoutInflater.from(context).inflate(R.layout.problem_card, null);
//        return new ViewHolder(card);
//    }
//
//    @Override
//    public void onBindViewHolder(ProblemRVAdapter.ViewHolder holder, int position) {
//        Problem problem = problemList.get(position);
//
//        holder.userNameTV.setText(problem.getCreator());
//        holder.locationTV.setText(problem.getRawLocation());
//        holder.categoryTV.setText(problem.getCategory());
//        holder.periodTV.setText(problem.getPeriod());
//        holder.descriptionTV.setText(problem.getDescription());
//
//        String noOfImagesString = getNoOfImagesText(problem);
//        if(noOfImagesString != null)
//            holder.noOfImagesTV.setText(noOfImagesString);
//        else
//            holder.noOfImagesTV.setVisibility(View.GONE);
//
//        problem.setTVLocation(holder.locationTV);
//
//        holder.imageView.setImageBitmap(problem.getMainImage());
//
//        setExpandButtonListener(holder.expandButton, holder.descriptionTV);
//        setLocationClickListener(holder.locationTV, problem);
//        setImageClickListener(holder.imageView, problem);
//    }
//
//    private String getNoOfImagesText(Problem p){
//        int no = p.getNoOfImages();
//        if(no == 0) return null;
//        else if(no == 1) return "+1 Image";
//        else return "+" + no + " Images";
//    }
//
//    private void setImageClickListener(ImageView img, final Problem p){
//        img.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Bitmap bitmap = p.getMainImage();
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//                byte[] imageBytes = stream.toByteArray();
//
//                Intent intent = new Intent(context, ExpImageActivity.class);
//                intent.putStringArrayListExtra(ExpImageActivity.URL_LIST, p.getImageUrls());
//                intent.putExtra(ExpImageActivity.IMAGE_PARCEL, imageBytes);
//                context.startActivity(intent);
//            }
//        });
//    }
//
//    private void setLocationClickListener(TextView locationTV, final Problem problem) {
//        locationTV.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String title = problem.getCreator() + " (" + problem.getCategory() + ")";
//
//                Intent intent = new Intent(context, MapsActivity.class);
//                intent.putExtra(MapsActivity.MAP_LOC_X, problem.locationX);
//                intent.putExtra(MapsActivity.MAP_LOC_Y, problem.locationY);
//                intent.putExtra(MapsActivity.MAP_SNIPPET, problem.getPeriod());
//                intent.putExtra(MapsActivity.MAP_TITLE, title);
//
//                context.startActivity(intent);
//            }
//        });
//    }
//
//    private void setExpandButtonListener(final ImageView v, final TextView tv) {
//        v.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(tv.getVisibility() == View.VISIBLE){
//                    tv.setVisibility(View.GONE);
//                    v.setImageDrawable(ContextCompat
//                            .getDrawable(context, R.drawable.ic_keyboard_arrow_down));
//                }
//                else {
//                    tv.setVisibility(View.VISIBLE);
//                    v.setImageDrawable(ContextCompat
//                            .getDrawable(context, R.drawable.ic_keyboard_arrow_up));
//                }
//            }
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return problemList.size();
//    }
//
//    class ViewHolder extends RecyclerView.ViewHolder {
//
//        TextView userNameTV;
//        TextView locationTV;
//        TextView categoryTV;
//        TextView periodTV;
//        TextView descriptionTV;
//        TextView noOfImagesTV;
//
//        ImageView userImage;
//        ImageView imageView;
//        ImageView expandButton;
//
//        ViewHolder(View itemView) {
//            super(itemView);
//
//            userNameTV = (TextView) itemView.findViewById(R.id.user_name_tv);
//            locationTV = (TextView) itemView.findViewById(R.id.location_tv);
//            categoryTV = (TextView) itemView.findViewById(R.id.category_tv);
//            periodTV = (TextView) itemView.findViewById(R.id.time_period_tv);
//            descriptionTV = (TextView) itemView.findViewById(R.id.description_tv);
//            noOfImagesTV = (TextView) itemView.findViewById(R.id.no_of_images_tv);
//
//
//            userImage = (ImageView) itemView.findViewById(R.id.user_image);
//            imageView = (ImageView) itemView.findViewById(R.id.image_view);
//
//            expandButton = (ImageView) itemView.findViewById(R.id.arrow_button);
//        }
//    }
//}