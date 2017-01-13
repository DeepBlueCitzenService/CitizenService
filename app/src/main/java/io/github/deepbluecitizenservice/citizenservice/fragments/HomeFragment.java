package io.github.deepbluecitizenservice.citizenservice.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.LinkedList;
import java.util.List;

import io.github.deepbluecitizenservice.citizenservice.LoginActivity;
import io.github.deepbluecitizenservice.citizenservice.MainActivity;
import io.github.deepbluecitizenservice.citizenservice.R;
import io.github.deepbluecitizenservice.citizenservice.adapter.CommonRecyclerViewAdapter;
import io.github.deepbluecitizenservice.citizenservice.database.ProblemModel;
import io.github.deepbluecitizenservice.citizenservice.database.QueryModel;

public class HomeFragment extends Fragment {
    private QueryModel queryModel;
    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        queryModel = new QueryModel();

        List<ProblemModel> problemModelList = new LinkedList<>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            Intent startLoginActivity = new Intent(getActivity(), LoginActivity.class);
            getActivity().startActivity(startLoginActivity);
            return v;
        }

        final DatabaseReference ref = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(user.getUid())
                .child("openProblems");

        final RecyclerView rv = (RecyclerView) v.findViewById(R.id.home_recycle_view);

        final CommonRecyclerViewAdapter adapter = new CommonRecyclerViewAdapter(
                rv,
                getContext(),
                problemModelList,
                MainActivity.HOME_TAG);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.home_view_swipe);

        rv.setLayoutManager(linearLayoutManager);
        rv.addItemDecoration(new QueryModel.SpacingDecoration(8));
        rv.setAdapter(adapter);

        queryModel.makeQuery(0-(System.currentTimeMillis()/1000), ref, adapter, refreshLayout);

        rv.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(queryModel.allAdded &&
                        linearLayoutManager.getItemCount() <=
                                linearLayoutManager.findLastVisibleItemPosition() + QueryModel.OFFSET_VIEW){
                    queryModel.makeQuery(queryModel.lastSeen, ref, adapter, refreshLayout);
                }
            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                queryModel.lastSeen = 0-(System.currentTimeMillis()/1000);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(refreshLayout.isRefreshing()) {
                            refreshLayout.setRefreshing(false);
                            Snackbar.make(refreshLayout, R.string.swipe_network_error,
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }, 5000);
            }
        });

        return v;
    }
}
