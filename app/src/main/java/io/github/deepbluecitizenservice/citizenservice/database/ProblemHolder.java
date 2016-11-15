package io.github.deepbluecitizenservice.citizenservice.database;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import io.github.deepbluecitizenservice.citizenservice.R;

public class ProblemHolder extends RecyclerView.ViewHolder{
    private View mView;

    public ProblemHolder(View itemView) {
        super(itemView);
        mView = itemView;
    }

    public View getView(){
        return mView;
    }

    public void setFields(String UserName, String location, String category, String period, String description) {
        TextView userNameTV, locationTV, categoryTV, periodTV, descriptionTV;
        userNameTV = (TextView) mView.findViewById(R.id.user_name_tv);
        locationTV = (TextView) mView.findViewById(R.id.location_tv);
        categoryTV = (TextView) mView.findViewById(R.id.category_tv);
        periodTV   = (TextView) mView.findViewById(R.id.time_period_tv);
        descriptionTV = (TextView) mView.findViewById(R.id.description_tv);

        userNameTV.setText(UserName);
        locationTV.setText(location);
        categoryTV.setText(category);
        periodTV.setText(period);
        descriptionTV.setText(description);
    }
}
