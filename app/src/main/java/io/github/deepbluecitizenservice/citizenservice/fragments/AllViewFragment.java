package io.github.deepbluecitizenservice.citizenservice.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.LinkedList;
import java.util.List;

import io.github.deepbluecitizenservice.citizenservice.GraphDialog;
import io.github.deepbluecitizenservice.citizenservice.MainActivity;
import io.github.deepbluecitizenservice.citizenservice.MapsActivity;
import io.github.deepbluecitizenservice.citizenservice.R;
import io.github.deepbluecitizenservice.citizenservice.adapter.CommonRecyclerViewAdapter;
import io.github.deepbluecitizenservice.citizenservice.database.ProblemModel;
import io.github.deepbluecitizenservice.citizenservice.database.QueryModel;

public class AllViewFragment extends Fragment {
    private QueryModel queryModel;

    public AllViewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_all_view, container, false);
        queryModel = new QueryModel();

        List<ProblemModel> problemModelList = new LinkedList<>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            return v;
        }

        final DatabaseReference ref = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("problems");

        RecyclerView rv = (RecyclerView) v.findViewById(R.id.all_recycle_view);

        final CommonRecyclerViewAdapter adapter = new CommonRecyclerViewAdapter(rv, getContext(), problemModelList, MainActivity.ALL_TAG);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        rv.setLayoutManager(linearLayoutManager);
        rv.addItemDecoration(new QueryModel.SpacingDecoration(8));
        rv.setAdapter(adapter);

        queryModel.makeQuery(0- (System.currentTimeMillis()/1000), ref, adapter);

        rv.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(queryModel.allAdded &&
                        linearLayoutManager.getItemCount() <=
                                linearLayoutManager.findLastVisibleItemPosition() + QueryModel.OFFSET_VIEW){
                    queryModel.makeQuery(queryModel.lastSeen, ref, adapter);
                }
            }
        });

        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.all_view_swipe);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //TODO: Add method to refresh the view
                refreshLayout.setRefreshing(false);
            }
        });

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.all_view_toolbar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.toolbar_all_view_map:
                startActivity(new Intent(getContext(), MapsActivity.class));
                break;
            case R.id.toolbar_all_view_chart:
                GraphDialog dialog = new GraphDialog(getContext(), getString(R.string.graph_all_stats_title), GraphDialog.ALL_PROBLEMS);
                dialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
