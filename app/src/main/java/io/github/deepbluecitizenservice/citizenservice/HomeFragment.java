package io.github.deepbluecitizenservice.citizenservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseIndexRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

import io.github.deepbluecitizenservice.citizenservice.database.ProblemHolder;
import io.github.deepbluecitizenservice.citizenservice.database.ProblemModel;

public class HomeFragment extends Fragment {

    private FirebaseRecyclerAdapter mAdapter;
    //private OnFragmentInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }


    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView rv = (RecyclerView) v.findViewById(R.id.home_recycle_view);
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("problems");
        final DatabaseReference keyref = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("openProblems");
        final StorageReference storage = FirebaseStorage.getInstance().getReference();

        mAdapter = new FirebaseIndexRecyclerAdapter<ProblemModel, ProblemHolder>
                (ProblemModel.class, R.layout.problem_card, ProblemHolder.class, keyref, ref) {
            @Override
            protected void populateViewHolder(ProblemHolder viewHolder, ProblemModel model, int position) {

                final HashMap<String, Object> mp = model.getDetails();

                viewHolder.setFields((String) mp.get(ProblemModel.USER_NAME),
                        (String)mp.get(ProblemModel.LOCATIONADDRESS),
                        (String)mp.get(ProblemModel.CATEGORY),
                        (String)mp.get(ProblemModel.TIMECREATED),
                        (String)mp.get(ProblemModel.DESCRIPTION));

                View v = viewHolder.getView();
                Glide.with(getActivity())
                        .using(new FirebaseImageLoader())
                        .load(storage.child((String)mp.get(ProblemModel.URL)))
                        .override(400, 300)
                        .centerCrop()
                        .crossFade()
                        .into((ImageView)v.findViewById(R.id.image_view));

                Glide.with(getActivity())
                        .load((String)mp.get(ProblemModel.USER_URL))
                        .into((ImageView)v.findViewById(R.id.user_image));

                TextView locationTV = (TextView) v.findViewById(R.id.location_tv);

                locationTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String title = mp.get(ProblemModel.USER_NAME) + " (" + mp.get(ProblemModel.CATEGORY) + ")";

                        Intent intent = new Intent(getActivity(), MapsActivity.class);
                        intent.putExtra(MapsActivity.MAP_LOC_X, (double)mp.get(ProblemModel.LOCATIONX));
                        intent.putExtra(MapsActivity.MAP_LOC_Y, (double) mp.get(ProblemModel.LOCATIONY));
                        intent.putExtra(MapsActivity.MAP_SNIPPET, (long) mp.get(ProblemModel.TIMECREATEDLONG));
                        intent.putExtra(MapsActivity.MAP_TITLE, title);

                        startActivity(intent);
                    }
                });

                final ImageView expandButton = (ImageView) v.findViewById(R.id.arrow_button);
                final TextView descriptionTV = (TextView) v.findViewById(R.id.description_tv);

                expandButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(descriptionTV.getVisibility() == View.VISIBLE){
                            descriptionTV.setVisibility(View.GONE);
                            expandButton.setImageDrawable(ContextCompat
                                    .getDrawable(getActivity(), R.drawable.ic_keyboard_arrow_down));
                        }
                        else {
                            descriptionTV.setVisibility(View.VISIBLE);
                            expandButton.setImageDrawable(ContextCompat
                                    .getDrawable(getActivity(), R.drawable.ic_keyboard_arrow_up));
                        }
                    }
                });
            }
        };
        rv.setAdapter(mAdapter);

        return v;
    }

//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
        mAdapter.cleanup();
    }
//
//    /**
//     * This interface must be implemented by activities that contain this
//     * fragment to allow an interaction in this fragment to be communicated
//     * to the activity and potentially other fragments contained in that
//     * activity.
//     * <p>
//     * See the Android Training lesson <a href=
//     * "http://developer.android.com/training/basics/fragments/communicating.html"
//     * >Communicating with Other Fragments</a> for more information.
//     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }
}
