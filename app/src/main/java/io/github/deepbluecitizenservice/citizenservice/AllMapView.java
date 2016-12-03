package io.github.deepbluecitizenservice.citizenservice;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.github.deepbluecitizenservice.citizenservice.adapter.MapInfoAdapter;
import io.github.deepbluecitizenservice.citizenservice.database.ProblemModel;

import static io.github.deepbluecitizenservice.citizenservice.R.id.map;
import static io.github.deepbluecitizenservice.citizenservice.database.QueryModel.QUERY_SIZE;

public class AllMapView extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //TODO: Fix set-my-location
        mMap.setMyLocationEnabled(true);
        mMap.setInfoWindowAdapter(new MapInfoAdapter(this));

        final DatabaseReference ref = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("problems");

        ref.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                            ProblemModel problem = ds.getValue(ProblemModel.class);
                            LatLng problemLatLng = new LatLng(problem.locationX, problem.locationY);
                            Marker marker = mMap.addMarker(new MarkerOptions().position(problemLatLng));
                            marker.setTag(problem);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }
}
