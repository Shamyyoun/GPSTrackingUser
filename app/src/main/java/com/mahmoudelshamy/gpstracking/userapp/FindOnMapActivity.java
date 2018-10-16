package com.mahmoudelshamy.gpstracking.userapp;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

import com.devspark.appmsg.AppMsg;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import datamodels.Constants;
import datamodels.Trip;
import json.JsonReader;
import utils.InternetUtil;

/**
 * Created by Shamyyoun on 5/28/2015.
 */
public class FindOnMapActivity extends ActionBarActivity {
    private Trip trip;
    private GoogleMap map;
    private List<Marker> markers;

    private Dialog dialog;
    private Button buttonCancel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_on_map);

        initComponents();
    }

    /**
     * method, used to initialize components
     */
    private void initComponents() {
        trip = (Trip) getIntent().getSerializableExtra(Constants.KEY_TRIP);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        markers = new ArrayList<Marker>();

        // ---init map view----
        // add start marker to map
        LatLng startCoordinate = new LatLng(trip.getStartLat(), trip.getStartLng());
        Marker startMarker = map.addMarker(new MarkerOptions()
                .title(getString(R.string.start_location))
                .snippet(trip.getStartLocationTitle())
                .position(startCoordinate)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        markers.add(startMarker);

        // check to add end marker
        if (trip.getEndLat() != 0.0 && trip.getEndLng() != 0.0) {
            LatLng endCoordinate = new LatLng(trip.getEndLat(), trip.getEndLng());
            Marker endMarker = map.addMarker(new MarkerOptions()
                    .title(getString(R.string.end_location))
                    .snippet(trip.getEndLocationTitle())
                    .position(endCoordinate)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            markers.add(endMarker);
        }

        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                // zoom in showing all markers after map loaded
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker marker : markers) {
                    builder.include(marker.getPosition());
                }
                LatLngBounds bounds = builder.build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 200);
                map.animateCamera(cameraUpdate);
            }
        });

        // ----init dialog----
        dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_fetching_path);
        dialog.setTitle(R.string.fetching_path);
        buttonCancel = (Button) dialog.findViewById(R.id.button_cancel);
        buttonCancel.setTypeface(Typeface.createFromAsset(getAssets(), "roboto_l.ttf"));

        // execute path task
        new PathTask().execute();
    }

    private class PathTask extends AsyncTask<Void, Void, Void> {
        private String response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // check internet
            if (!InternetUtil.isConnected(getApplicationContext())) {
                // show error
                showAppMsg(R.string.no_internet_connection);

                dialog.dismiss();
                cancel(true);
                return;
            }

            // --conditions is okay--
            // add click listener to cancel button
            buttonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancel(true);
                    dialog.dismiss();
                }
            });
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // create json reader
            String url = AppController.END_POINT + "/get_trip_path.php?trip_id=" + trip.getId();
            JsonReader jsonReader = new JsonReader(url);

            // execute request
            response = jsonReader.sendGetRequest();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // hide dialog
            dialog.dismiss();

            // validate response
            if (response == null) {
                showAppMsg(R.string.connection_error_try_again);
                return;
            }

            // ---response is valid---
            // create PolyOptions object
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color(Color.parseColor("#2261EA"));
            // add start point
            polylineOptions.add(new LatLng(trip.getStartLat(), trip.getStartLng()));

            // handle it in PolylineOptions object
            int pointsSize = 0;
            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    double latitude = jsonObject.getDouble("lat");
                    double longitude = jsonObject.getDouble("lng");

                    polylineOptions.add(new LatLng(latitude, longitude));
                    pointsSize++;
                }
            } catch (Exception e) {
                // show error msg
                showAppMsg(R.string.connection_error_try_again);
                return;
            }

            // add end point
            polylineOptions.add(new LatLng(trip.getEndLat(), trip.getEndLng()));

            // check polyline options size
            if (pointsSize == 0) {
                // no points added >> show msg
                showAppMsg(R.string.no_path_available);
            } else {
                // draw path on map
                map.addPolyline(polylineOptions);
            }
        }
    }

    /**
     * method, used to show app msg
     */
    private void showAppMsg(int msgResId) {
        AppMsg.cancelAll(this);
        AppMsg appMsg = AppMsg.makeText(this, msgResId, AppMsg.STYLE_CONFIRM);
        appMsg.show();
    }
}
