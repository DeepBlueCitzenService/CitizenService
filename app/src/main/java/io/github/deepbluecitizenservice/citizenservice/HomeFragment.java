package io.github.deepbluecitizenservice.citizenservice;

import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.List;

import io.github.deepbluecitizenservice.citizenservice.adapter.HomeRecyclerViewAdapter;
import io.github.deepbluecitizenservice.citizenservice.database.ProblemModel;

public class HomeFragment extends Fragment {

    //private FirebaseRecyclerAdapter mAdapter;
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

        List<ProblemModel> problemModelList = new LinkedList<>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            return v;
        }

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("problems");
        //final DatabaseReference keyref = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("openProblems");
        //final StorageReference storage = FirebaseStorage.getInstance().getReference();

        RecyclerView rv = (RecyclerView) v.findViewById(R.id.home_recycle_view);
        final HomeRecyclerViewAdapter adapter = new HomeRecyclerViewAdapter(getContext(), problemModelList);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.addItemDecoration(new SpacingDecoration(8));
        rv.setAdapter(adapter);

        //TODO : is "timeCreated" correct? But it gives result in ascending order; Nevermind it's easy and we can do it later
        ref.orderByChild("timeCreated").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                for(final DataSnapshot ds : dataSnapshot.getChildren()){
                    new AsyncTask<Void, Void, Void>() {

                        ProblemModel user;
                        @Override
                        protected Void doInBackground(Void... voids) {
                            user = ds.getValue(ProblemModel.class);
                            try {
                                //TODO : make thread sleep until image is downloaded instead on 1.5 sec
                                Thread.sleep(1500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        public void onPostExecute(Void result){
                            adapter.addProblem(user);
                        }

                    }.execute();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*
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
        */

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
        //mAdapter.cleanup();
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

    private class SpacingDecoration extends RecyclerView.ItemDecoration {
        private int spacing;

        SpacingDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            outRect.bottom = spacing;
            outRect.top = spacing;

        }
    }
}
