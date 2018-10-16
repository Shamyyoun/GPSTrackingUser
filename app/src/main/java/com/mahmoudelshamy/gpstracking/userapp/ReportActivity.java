package com.mahmoudelshamy.gpstracking.userapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;

import com.devspark.appmsg.AppMsg;

import java.util.ArrayList;
import java.util.List;

import adapters.TripsAdapter;
import datamodels.Constants;
import datamodels.Trip;
import datamodels.Vehicle;
import json.JsonReader;
import json.TripsHandler;
import utils.InternetUtil;
import views.ProgressActivity;
import views.SlideExpandableListView;

/**
 * Created by Shamyyoun on 5/28/2015.
 */
public class ReportActivity extends ProgressActivity {
    private Vehicle vehicle;
    private SlideExpandableListView listTrips;
    private List<AsyncTask> tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initComponents();
    }

    /**
     * method, used to init components
     */
    private void initComponents() {
        vehicle = (Vehicle) getIntent().getSerializableExtra(Constants.KEY_VEHICLE);
        listTrips = (SlideExpandableListView) findViewById(R.id.list_trips);
        tasks = new ArrayList<AsyncTask>();

        // customize actionbar
        getSupportActionBar().setElevation(0);
        setTitle(vehicle.getName() + " " + getString(R.string.trips));

        // load data
        new TripsTask().execute();
    }

    /**
     * overriden method, used to return activity layout resource
     */
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_report;
    }

    /**
     * overriden method, used to refresh content
     */
    @Override
    protected void onRefresh() {
        new TripsTask().execute();
    }

    /**
     * overriden method
     */
    @Override
    protected void onDestroy() {
        for (AsyncTask task : tasks) {
            task.cancel(true);
        }

        // cancel all app msgs
        AppMsg.cancelAll();

        super.onDestroy();
    }

    /**
     * sub class used to load trips from server
     */
    private class TripsTask extends AsyncTask<Void, Void, Void> {
        private ReportActivity activity;
        private String response;

        private TripsTask() {
            activity = ReportActivity.this;

            tasks.add(this); // hold reference to this task, to destroy it if required
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // check internet connection
            if (!InternetUtil.isConnected(activity)) {
                showError(R.string.no_internet_connection);

                cancel(true);
                return;
            }

            // show progress
            showProgress();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // create json reader
            String url = AppController.END_POINT + "/get_trips.php?vehicle_id=" + vehicle.getId();
            JsonReader jsonReader = new JsonReader(url);

            // execute request
            response = jsonReader.sendGetRequest();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // validate response
            if (response == null) {
                // show error msg
                showError(R.string.connection_error_try_again);

                return;
            }

            // ---response is valid---
            // handle it in trip list
            TripsHandler handler = new TripsHandler(response);
            final List<Trip> trips = handler.handle();

            // check handling operation result
            if (trips == null) {
                // show error msg
                showError(R.string.connection_error_try_again);

                return;
            }

            // check trips size
            if (trips.size() == 0) {
                showEmpty(R.string.no_trips_available);
            } else {
                // set list adapter
                TripsAdapter adapter = new TripsAdapter(activity, R.layout.list_trips_item, R.layout.list_trips_subitem, trips);
                listTrips.setAdapter(adapter);

                // add on item click listener
                listTrips.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                    @Override
                    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                        if (listTrips.isGroupExpanded(groupPosition)) {
                            listTrips.collapseGroupWithAnimation(groupPosition);
                        } else {
                            // check if trip has ended or not
                            Trip trip = trips.get(groupPosition);
                            if (trip.isEnded()) {
                                // ended
                                listTrips.expandGroupWithAnimation(groupPosition);
                            } else {
                                // not ended
                                AppMsg.cancelAll();
                                AppMsg.makeText(ReportActivity.this, R.string.not_ended_yet, AppMsg.STYLE_CONFIRM).show();
                            }
                        }

                        return true;
                    }
                });

                showMain();
            }
        }
    }
}
