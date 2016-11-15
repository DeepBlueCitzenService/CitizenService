package io.github.deepbluecitizenservice.citizenservice;

import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import io.github.deepbluecitizenservice.citizenservice.adapter.CommonRecyclerViewAdapter;
import io.github.deepbluecitizenservice.citizenservice.database.ProblemModel;

public class HomeFragment extends Fragment {
    private long lastSeen = -1;
    private long counter, size;
    private boolean allAdded = false;
    private final static int QUERY_SIZE = 3, OFFSET_VIEW= 2;

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
        RecyclerView rv = (RecyclerView) v.findViewById(R.id.home_recycle_view);

        final CommonRecyclerViewAdapter adapter = new CommonRecyclerViewAdapter(getContext(), problemModelList);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        rv.setLayoutManager(linearLayoutManager);
        rv.addItemDecoration(new SpacingDecoration(8));
        rv.setAdapter(adapter);

        makeQuery(0- (System.currentTimeMillis()/1000), ref, adapter);

        rv.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(allAdded &&
                        linearLayoutManager.getItemCount() <=
                                linearLayoutManager.findLastVisibleItemPosition() + OFFSET_VIEW){
                    Log.d("SCROLL", "MAKING A QUERY");
                    makeQuery(lastSeen, ref, adapter);
                }
            }
        });

        return v;
    }

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

    private void makeQuery(long startAt, final DatabaseReference ref, final CommonRecyclerViewAdapter adapter){
        allAdded = false;
        counter = 0;
        //TODO : is "timeCreated" correct? But it gives result in ascending order; Nevermind it's easy and we can do it later
        ref.orderByChild("timeCreated").startAt(startAt+1).limitToFirst(QUERY_SIZE).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                size = dataSnapshot.getChildrenCount();
                if(size==0) allAdded = true;

                for(final DataSnapshot ds : dataSnapshot.getChildren()){
                    new AsyncTask<Void, Void, Boolean>() {

                        ProblemModel user;
                        @Override
                        protected Boolean doInBackground(Void... voids) {
                            if(!adapter.isAdded(ds.getKey())){
                                user = ds.getValue(ProblemModel.class);
                                if(lastSeen<user.timeCreated) lastSeen= user.timeCreated;
                                return true;
                            }
                            return false;
                        }

                        @Override
                        public void onPostExecute(Boolean result){
                            if(result){
                                adapter.addProblem(user, ds.getKey());
                                counter+=1;
                                allAdded = counter>=size;
                            }
                        }

                    }.execute();
                }
                ref.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
