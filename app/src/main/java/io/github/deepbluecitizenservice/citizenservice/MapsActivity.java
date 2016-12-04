package io.github.deepbluecitizenservice.citizenservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import io.github.deepbluecitizenservice.citizenservice.service.GPSService;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String MAP_PROBLEM = "MapProblem";

    private GoogleMap googleMap;
    private ProblemModel singleProblem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        singleProblem = intent.getParcelableExtra(MAP_PROBLEM);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setInfoWindowAdapter(new MapInfoAdapter(this));

        if(singleProblem != null){
            Marker marker = addMarker(singleProblem);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15));
        }
        else {
            GPSService gpsService = new GPSService(this, findViewById(R.id.map), googleMap);
            if(gpsService.isGPSPermissionGranted() && gpsService.isGPSEnabled()) {
                //noinspection MissingPermission
                googleMap.setMyLocationEnabled(true);
            }
            loadAllProblems();
        }
    }

    private void loadAllProblems(){
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
                            addMarker(problem);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    private Marker addMarker(ProblemModel problem){
        LatLng problemLatLng = new LatLng(problem.locationX, problem.locationY);
        BitmapDescriptor markerIcon = BitmapDescriptorFactory.defaultMarker();

        switch (problem.category){
            case ProblemModel.CATEGORY_TRAFFIC:
                markerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA);
                break;
            case ProblemModel.CATEGORY_GARBAGE:
                markerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
                break;
            case ProblemModel.CATEGORY_POTHOLES:
                markerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
                break;
        }

        Marker marker = googleMap.addMarker(new MarkerOptions().position(problemLatLng).icon(markerIcon));
        marker.setTag(problem);
        return marker;
    }
}
