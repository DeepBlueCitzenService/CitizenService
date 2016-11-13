package io.github.deepbluecitizenservice.citizenservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String MAP_LOC_X = "MapLocX";
    public static final String MAP_LOC_Y = "MapLocY";
    public static final String MAP_TITLE = "MapTitle";
    public static final String MAP_SNIPPET = "MapSnippet";

    private double locationX = 0;
    private double locationY = 0;

    private String markerTitle = "Location";
    private String markerSnippet = "Location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        this.locationX = intent.getDoubleExtra(MAP_LOC_X,0);
        this.locationY = intent.getDoubleExtra(MAP_LOC_Y,0);
        this.markerTitle = intent.getStringExtra(MAP_TITLE);
        this.markerSnippet = intent.getStringExtra(MAP_SNIPPET);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng markerPosition = new LatLng(locationX, locationY);
        MarkerOptions markerOpt = new MarkerOptions().position(markerPosition)
                .title(markerTitle).snippet(markerSnippet);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, 15));
        googleMap.addMarker(markerOpt);
    }
}
