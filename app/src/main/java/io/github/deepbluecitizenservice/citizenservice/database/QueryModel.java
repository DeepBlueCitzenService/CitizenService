package io.github.deepbluecitizenservice.citizenservice.database;

import android.graphics.Rect;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import io.github.deepbluecitizenservice.citizenservice.adapter.CommonRecyclerViewAdapter;

public class QueryModel {
    public long lastSeen = 0-System.currentTimeMillis()/1000;
    public long counter, size;
    public boolean allAdded = false;
    public final static int QUERY_SIZE = 3, OFFSET_VIEW= 2;

    public void makeQuery(long startAt, final DatabaseReference ref, final CommonRecyclerViewAdapter adapter){
        allAdded = false;
        counter = 0;

        ref.orderByChild("negTimeCreated").startAt(startAt+1).limitToFirst(QUERY_SIZE).addValueEventListener(new ValueEventListener() {
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
                                if(lastSeen<user.negTimeCreated) lastSeen= user.negTimeCreated;
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

    public static class SpacingDecoration extends RecyclerView.ItemDecoration {
        private int spacing;

        public SpacingDecoration(int spacing) {
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
