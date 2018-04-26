package com.example.lenovocom.mismap;

import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;


import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, View.OnClickListener {

    private GoogleMap mMap;
    EditText editText;
    Button save;
    Button poly;
    public String etString;
    ArrayList<Marker> markers = new ArrayList<Marker>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        bindViews();

    }

    private void bindViews() {
        editText = (EditText) findViewById(R.id.et);
        save = (Button) findViewById(R.id.save);
        poly = (Button) findViewById(R.id.poly);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        Toast.makeText(this, "fill edit text then long click", Toast.LENGTH_LONG).show();


        LatLng begin = new LatLng(50.980862, 11.332365);
       // mMap.addMarker(new MarkerOptions().position(begin).title("Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(begin));
        mMap.setOnMapLongClickListener(this);

        poly.setOnClickListener(this);


    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        save.setOnClickListener(this);


        PreferenceManager.getDefaultSharedPreferences(this).edit().
                putString(latLng.toString(), etString).apply();

        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(PreferenceManager.getDefaultSharedPreferences(this).
                getString(latLng.toString(), "def val")));
        markers.add(marker);
        //Toast.makeText(this, markers.size(), Toast.LENGTH_SHORT).show();


    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.save) {
            etString = editText.getText().toString();
            editText.setText("");
            Toast.makeText(this, "The next  marker text is saved.", Toast.LENGTH_SHORT).show();


        } else if (view.getId() == R.id.poly) {
            poly.setText("End Polygon");
            PolygonOptions polygonOptions = new PolygonOptions()
                    .strokeColor(Color.BLACK).strokeWidth(4)
                    .fillColor(0x700FFFFF);
// reference for transparency of polygon's color : https://stackoverflow.com/questions/14326482/android-maps-v2-polygon-transparency


            for (int i = 0; i < markers.size(); i++) {
                polygonOptions.add(markers.get(i).getPosition());
            }
            Polygon polygon = mMap.addPolygon(polygonOptions);

            Marker marker = mMap.addMarker(new MarkerOptions().position(centroid(polygonOptions.getPoints())).title(area(polygon.getPoints())));


        }
    }

    private LatLng centroid(List<LatLng> points) {
        // Ref: https://stackoverflow.com/questions/18440823/how-do-i-calculate-the-center-of-a-polygon-in-google-maps-android-api-v2

        int nPts = points.size();
        double x = 0;
        double y = 0;
        double f;
        int j = nPts - 1;
        LatLng p1;
        LatLng p2;

        for (int i = 0; i < nPts; j = i++) {
            p1 = points.get((i));
            p2 = points.get(j);
            f = p1.latitude * p2.longitude - p2.latitude * p1.longitude;
            x += (p1.latitude + p2.latitude) * f;
            y += (p1.longitude + p2.longitude) * f;
        }

        f = aForCentroid(points) * 6;

        LatLng centroid = new LatLng(x / f, y / f);
        return centroid;


    }


    private static double aForCentroid(List<LatLng> arr) {
        double area = 0;
        int nPts = arr.size();
        int j = nPts - 1;
        LatLng p1;
        LatLng p2;
        for (int i = 0; i < nPts; j = i++) {

            p1 = arr.get(i);
            p2 = arr.get(j);
            area += p1.latitude * p2.longitude;
            area -= p1.longitude * p2.latitude;
        }
        area /= 2;


        return area;
    }



    // Reference to area of polygon : https://github.com/jillesvangurp/geogeometry/blob/master/src/main/java/com/jillesvangurp/geo/GeoGeometry.java

    public String area(List<LatLng> polygon) {

        double total = 0;
        LatLng previous = polygon.get(0);
        double area;

        LatLng center = centroid(polygon);
        double xRef = center.longitude;
        double yRef = center.latitude;


        for (int i = 1; i < polygon.size(); i++) {
            LatLng current = polygon.get(i);
            // convert to cartesian coordinates in meters, note this not very exact
            double x1 = ((previous.longitude - xRef) * (6378137 * Math.PI / 180)) * Math.cos(yRef * Math.PI / 180);
            double y1 = (previous.latitude - yRef) * (Math.toRadians(6378137));
            double x2 = ((current.longitude - xRef) * (6378137 * Math.PI / 180)) * Math.cos(yRef * Math.PI / 180);
            double y2 = (current.latitude - yRef) * (Math.toRadians(6378137));

            // calculate crossproduct
            total += x1 * y2 - x2 * y1;
            previous = current;
        }


        area = 0.000001 * 0.5 * Math.abs(total);

        if (area< 1 ){
            return area* 1000000 + "square meter";
        }else return area + "square meter";
    }


}
