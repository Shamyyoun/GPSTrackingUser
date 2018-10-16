package com.mahmoudelshamy.gpstracking.userapp;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.devspark.appmsg.AppMsg;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import adapters.SpinnerAdapter;
import database.UserDAO;
import database.VehicleDAO;
import datamodels.Constants;
import datamodels.Trip;
import datamodels.User;
import datamodels.Vehicle;
import json.JsonReader;
import json.LocationHandler;
import json.VehiclesHandler;
import utils.InternetUtil;
import utils.ViewUtil;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    // top bar objects
    private ImageView imageReport;
    private ImageView imageTurn;
    private ImageView imageTrip;
    private ImageView imageVehicles;
    private ImageView imageSettings;

    // main objects
    private Typeface typeface;
    private List<Vehicle> vehicles;
    private Vehicle selectedVehicle;
    private GoogleMap map;
    private Spinner spinnerVehicle;
    private Marker vehicleMarker;

    // turn dialog objects
    private Dialog dialogTurn;
    private View layoutContentTurn;
    private View layoutTurn;
    private ImageButton buttonTurnOn;
    private ImageButton buttonTurnOff;
    private Button buttonCloseTurn;
    private View layoutEnterPassword;
    private EditText textPassword;
    private Button buttonOkTurn;
    private Button buttonBack;
    private ProgressBar progressBarTurn;
    private TextView textSuccessTurn;
    private TextView textTurn;

    // trip dialog objects
    private Dialog dialogTrip;
    private View layoutContentTrip;
    private ImageButton buttonStartTrip;
    private ImageButton buttonEndTrip;
    private Button buttonCloseTrip;
    private ProgressBar progressBarTrip;
    private TextView textSuccessTrip;

    // tracking interval dialog
    private Dialog dialogTrackingInterval;
    private View layoutContentTrackingInterval;
    private EditText textTrackingInterval;
    private Button buttonOk;
    private Button buttonCancel;
    private ProgressBar progressBarTrackingInterval;
    private TextView textSuccessTrackingInterval;

    private Handler handler;
    private Runnable runnable;
    private VehicleDAO vehicleDAO;

    private MessageReceiver mReceiver; // used to listen for messages
    private List<AsyncTask> tasks; // used to hold running tasks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponents();
    }

    /**
     * method, used to initialize components
     */
    private void initComponents() {
        imageReport = (ImageView) findViewById(R.id.image_report);
        imageTurn = (ImageView) findViewById(R.id.image_turn);
        imageTrip = (ImageView) findViewById(R.id.image_trip);
        imageVehicles = (ImageView) findViewById(R.id.image_vehicles);
        imageSettings = (ImageView) findViewById(R.id.image_settings);

        typeface = Typeface.createFromAsset(getAssets(), "roboto_l.ttf");
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        spinnerVehicle = (Spinner) findViewById(R.id.spinner_vehicle);

        handler = new Handler();
        vehicleDAO = new VehicleDAO(this);
        tasks = new ArrayList<AsyncTask>();

        mReceiver = new MessageReceiver();
        registerReceiver(mReceiver, new IntentFilter(getPackageName() + ".TRIP_RESPONDED"));

        // get vehicles
        vehicleDAO.open();
        vehicles = vehicleDAO.getAll();
        vehicleDAO.close();

        // update ui
        updateUI();

        // load vehicles from server in background
        new VehiclesTask().execute();

        registerForContextMenu(imageSettings);

        // add listener
        imageReport.setOnClickListener(this);
        imageTurn.setOnClickListener(this);
        imageTrip.setOnClickListener(this);
        imageVehicles.setOnClickListener(this);
        imageSettings.setOnClickListener(this);
    }

    /**
     * method, used to initialize turn dialog
     */
    private void initTurnDialog() {
        dialogTurn = new Dialog(this);
        dialogTurn.setTitle(getString(R.string.turn) + " " + selectedVehicle.getName());
        dialogTurn.setContentView(R.layout.dialog_turn);
        layoutContentTurn = dialogTurn.findViewById(R.id.layout_content);
        layoutTurn = dialogTurn.findViewById(R.id.layout_turn);
        buttonTurnOn = (ImageButton) dialogTurn.findViewById(R.id.button_turnOn);
        buttonTurnOff = (ImageButton) dialogTurn.findViewById(R.id.button_turnOff);
        buttonCloseTurn = (Button) dialogTurn.findViewById(R.id.button_close_turn);
        layoutEnterPassword = dialogTurn.findViewById(R.id.layout_enterPassword);
        textPassword = (EditText) dialogTurn.findViewById(R.id.text_password);
        buttonOkTurn = (Button) dialogTurn.findViewById(R.id.button_okTurn);
        buttonBack = (Button) dialogTurn.findViewById(R.id.button_back);
        progressBarTurn = (ProgressBar) dialogTurn.findViewById(R.id.progressBar);
        textSuccessTurn = (TextView) dialogTurn.findViewById(R.id.text_success);
        textTurn = (TextView) dialogTurn.findViewById(R.id.text_turn);

        // customize fonts
        buttonCloseTurn.setTypeface(typeface);
        textSuccessTurn.setTypeface(typeface);
        textPassword.setTypeface(typeface);
        buttonOkTurn.setTypeface(typeface);
        buttonBack.setTypeface(typeface);

        // add listeners
        buttonTurnOn.setOnClickListener(this);
        buttonTurnOff.setOnClickListener(this);
        buttonCloseTurn.setOnClickListener(this);
        buttonOkTurn.setOnClickListener(this);
        buttonBack.setOnClickListener(this);
    }

    /**
     * method, used to initialize trip dialog
     */
    private void initTripDialog() {
        dialogTrip = new Dialog(this);
        dialogTrip.setTitle(getString(R.string.trip) + " " + selectedVehicle.getName());
        dialogTrip.setContentView(R.layout.dialog_trip);
        layoutContentTrip = dialogTrip.findViewById(R.id.layout_content);
        buttonStartTrip = (ImageButton) dialogTrip.findViewById(R.id.button_startTrip);
        buttonEndTrip = (ImageButton) dialogTrip.findViewById(R.id.button_endTrip);
        buttonCloseTrip = (Button) dialogTrip.findViewById(R.id.button_close_trip);
        progressBarTrip = (ProgressBar) dialogTrip.findViewById(R.id.progressBar);
        textSuccessTrip = (TextView) dialogTrip.findViewById(R.id.text_success);

        // customize fonts
        buttonCloseTrip.setTypeface(typeface);
        textSuccessTrip.setTypeface(typeface);

        // add listeners
        buttonStartTrip.setOnClickListener(this);
        buttonEndTrip.setOnClickListener(this);
        buttonCloseTrip.setOnClickListener(this);

        // set initials
        buttonStartTrip.setEnabled(selectedVehicle.getTripStatus() == Trip.STATUS_END);
        buttonEndTrip.setEnabled(selectedVehicle.getTripStatus() == Trip.STATUS_START);
    }

    /**
     * method, used to initialize tracking interval dialog
     */
    private void initTrackingIntervalDialog() {
        dialogTrackingInterval = new Dialog(this);
        dialogTrackingInterval.setTitle(R.string.update_tracking_interval);
        dialogTrackingInterval.setContentView(R.layout.dialog_tracking_interval);
        layoutContentTrackingInterval = dialogTrackingInterval.findViewById(R.id.layout_content);
        textTrackingInterval = (EditText) dialogTrackingInterval.findViewById(R.id.text_trackingInterval);
        buttonOk = (Button) dialogTrackingInterval.findViewById(R.id.button_ok);
        buttonCancel = (Button) dialogTrackingInterval.findViewById(R.id.button_cancel);
        progressBarTrackingInterval = (ProgressBar) dialogTrackingInterval.findViewById(R.id.progressBar);
        textSuccessTrackingInterval = (TextView) dialogTrackingInterval.findViewById(R.id.text_success);

        // customize fonts
        textTrackingInterval.setTypeface(typeface);
        buttonOk.setTypeface(typeface);
        buttonCancel.setTypeface(typeface);
        textSuccessTrackingInterval.setTypeface(typeface);

        // set old tracking interval
        int trackingInterval = AppController.getInstance(getApplicationContext()).getActiveUser().getTrackingInterval();
        textTrackingInterval.setText("" + trackingInterval);

        // add listeners
        buttonOk.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);
    }

    /**
     * overriden method, used to handle click listener
     */
    @Override
    public void onClick(View v) {
        Intent intent;

        switch (v.getId()) {
            case R.id.image_report:
                // start report activity
                intent = new Intent(this, ReportActivity.class);
                intent.putExtra(Constants.KEY_VEHICLE, selectedVehicle);
                startActivity(intent);
                break;

            case R.id.image_turn:
                // show turn dialog
                initTurnDialog();
                dialogTurn.show();
                break;

            case R.id.image_trip:
                // show trip dialog
                initTripDialog();
                dialogTrip.show();
                break;

            case R.id.image_vehicles:
                // open vehicles activity
                intent = new Intent(this, VehiclesActivity.class);
                intent.putExtra(Constants.KEY_CAME_FROM_MAIN, true);
                startActivityForResult(intent, Constants.REQ_CODE_VEHICLES);
                break;

            case R.id.image_settings:
                openContextMenu(imageSettings);
                break;

            case R.id.button_turnOn:
                // hide turn panel and show enter password panel
                ViewUtil.showView(layoutTurn, false, View.INVISIBLE);
                ViewUtil.showView(layoutEnterPassword, true);
                textTurn.setText("true");
                break;

            case R.id.button_turnOff:
                // hide turn panel and show enter password panel
                ViewUtil.showView(layoutTurn, false, View.INVISIBLE);
                ViewUtil.showView(layoutEnterPassword, true);
                textTurn.setText("false");
                break;

            case R.id.button_close_turn:
                // close turn dialog
                dialogTurn.dismiss();
                break;

            case R.id.button_okTurn:
                // validate password
                String password = textPassword.getText().toString();
                if (password.isEmpty()) {
                    textPassword.setError(getString(R.string.enter_your_password));
                } else {
                    // check password
                    if (password.equals(AppController.getInstance(this).getActiveUser().getPassword())) {
                        // --valid password--
                        // hide keyboard
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(textPassword.getWindowToken(), 0);

                        // execute turn task
                        boolean turn = textTurn.getText().equals("true");
                        new TurnTask(turn).execute();
                    } else {
                        // invalid password >> show error
                        textPassword.setError(getString(R.string.invalid_password));
                    }
                }

                break;

            case R.id.button_back:
                // hide enter password panel and show turn panel
                ViewUtil.showView(layoutEnterPassword, false, View.INVISIBLE);
                ViewUtil.showView(layoutTurn, true);
                // remove text in textPassword
                textPassword.setText("");
                break;

            case R.id.button_startTrip:
                // execute trip task
                new TripTask(true).execute();
                break;

            case R.id.button_endTrip:
                // execute trip task
                new TripTask(false).execute();
                break;

            case R.id.button_close_trip:
                // close trip dialog
                dialogTrip.dismiss();
                break;

            case R.id.button_ok:
                // execute update tracking interval task
                String trackingInterval = textTrackingInterval.getText().toString();
                new UpdateTrackingIntervalTask(trackingInterval).execute();
                break;

            case R.id.button_cancel:
                // close tracking interval dialog
                dialogTrackingInterval.dismiss();

                break;
        }
    }

    /**
     * method, used to update UI after getting vehicles
     */
    private void updateUI() {
        // check user type
        if (AppController.getInstance(this).getActiveUser().getType() == User.TYPE_PERSONAL) {
            // hide vehicles spinner
            spinnerVehicle.setVisibility(View.GONE);

            // set selected vehicle to first vehicle and zoom to it
            selectedVehicle = vehicles.get(0);
            zoomToSelectedVehicle();
        } else {
            // get selected vehicle's position
            int selectedPosition = spinnerVehicle.getSelectedItemPosition();

            // set spinner adapter
            SpinnerAdapter adapter = new SpinnerAdapter(this, R.layout.spinner_vehicle_item, vehicles);
            adapter.setTypeface(Typeface.createFromAsset(getAssets(), "roboto_m.ttf"));
            spinnerVehicle.setAdapter(adapter);
            spinnerVehicle.setSelection(selectedPosition);

            // add listener
            spinnerVehicle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                    // set selected vehicle and zoom to it
                    selectedVehicle = vehicles.get(position);
                    zoomToSelectedVehicle();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
    }

    /**
     * method, used to zoom to selected vehicle's location
     */
    private void zoomToSelectedVehicle() {
        // get vehicle location
        LatLng coordinate = new LatLng(selectedVehicle.getLatitude(), selectedVehicle.getLongitude());

        // check marker
        if (vehicleMarker == null) {
            // not added yet >> add marker now to map
            vehicleMarker = map.addMarker(new MarkerOptions()
                            .title(selectedVehicle.getName())
                            .snippet(selectedVehicle.getPurpose())
                            .position(coordinate)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_icon))
            );
        } else {
            // just change marker options
            vehicleMarker.setTitle(selectedVehicle.getName());
            vehicleMarker.setSnippet(selectedVehicle.getPurpose());
            vehicleMarker.setPosition(coordinate);
        }

        // animate camera to vehicle location
        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(coordinate, 20);
        map.animateCamera(location, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                // when zooming finish >> start update location task
                handler.removeCallbacks(runnable);
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        new LocationTask().execute();
                        int trackingInterval = AppController.getInstance(getApplicationContext()).getActiveUser().getTrackingInterval() * 1000;
                        handler.postDelayed(this, trackingInterval);
                    }
                };
                handler.post(runnable);
            }

            @Override
            public void onCancel() {
            }
        });
    }

    /**
     * method, used to change selected vehicle's marker position
     */
    private void updateVehicleMarker() {
        // update marker's position
        LatLng coordinate = new LatLng(selectedVehicle.getLatitude(), selectedVehicle.getLongitude());
        vehicleMarker.setPosition(coordinate);

        // animate camera to vehicle's location
        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(coordinate, map.getCameraPosition().zoom);
        map.animateCamera(location);
    }

    /**
     * overriden method
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
    }

    /**
     * overriden method
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.item_update_profile:
                intent = new Intent(this, UpdateProfileActivity.class);
                startActivity(intent);
                return true;

            case R.id.item_trackingInterval:
                initTrackingIntervalDialog();
                dialogTrackingInterval.show();
                return true;

            case R.id.item_logout:
                // remove vehicles from database
                vehicleDAO.open();
                vehicleDAO.deleteAll();
                vehicleDAO.close();

                // remove saved used from DB
                UserDAO userDAO = new UserDAO(this);
                userDAO.open();
                userDAO.deleteAll();
                userDAO.close();

                // make active user null
                AppController.getInstance(this).setActiveUser(null);

                // goto login activity
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);

                finish();
                return true;

            case R.id.item_exit:
                finish();
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * sub class, used to get vehicle's location from server
     */
    private class LocationTask extends AsyncTask<Void, Void, Void> {
        private MainActivity activity;
        private String response;

        private LocationTask() {
            activity = MainActivity.this;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // check internet connection
            if (!InternetUtil.isConnected(activity)) {
                cancel(true);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            // create json reader
            String url = AppController.END_POINT + "/get_location.php?vehicle_id=" + selectedVehicle.getId();
            JsonReader jsonReader = new JsonReader(url);

            // execute request
            response = jsonReader.sendGetRequest();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // validate response
            if (response != null) {
                // response is valid >> handle it
                LocationHandler handler = new LocationHandler(response);
                double[] location = handler.handle();

                // check handling operation result
                if (location != null) {
                    // update selected vehicles location
                    selectedVehicle.setLocation(location);

                    // update marker on map
                    updateVehicleMarker();
                }
            }
        }
    }

    /**
     * sub class, used to get vehicles from server
     */
    private class VehiclesTask extends AsyncTask<Void, Void, Void> {
        private User user;
        private MainActivity activity;
        private String response;

        private VehiclesTask() {
            activity = MainActivity.this;
            user = AppController.getInstance(activity).getActiveUser();

            // save reference to this task, to destroy it if required
            tasks.add(this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // check internet connection
            if (!InternetUtil.isConnected(activity)) {
                // show error
                showAppMsg(R.string.no_internet_connection, AppMsg.STYLE_CONFIRM);

                cancel(true);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            // create json reader
            String url = AppController.END_POINT + "/get_vehicles.php?username=" + user.getUsername();
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
                return;
            }

            // ---response is valid---
            // handle it
            VehiclesHandler handler = new VehiclesHandler(response);
            List<Vehicle> vehicles = handler.handle();

            // check handling operation result
            if (vehicles == null) {
                return;
            }

            // check user's type
            if (user.getType() == User.TYPE_PERSONAL) {
                // personal >> change vehicles list to first vehicle only
                Vehicle firstVehicle = vehicles.get(0);
                vehicles.clear();
                vehicles.add(firstVehicle);
            }

            // save new vehicles in database
            vehicleDAO.open();
            vehicleDAO.deleteAll();
            vehicleDAO.add(vehicles);
            vehicleDAO.close();

            activity.vehicles = vehicles;

            // update UI
            updateUI();
        }
    }

    /**
     * sub class, used to send turn on/off msg to vehicle
     */
    private class TurnTask extends AsyncTask<Void, Void, Void> {
        private boolean turnOn;

        private MainActivity activity;
        private String response;

        private TurnTask(boolean turnOn) {
            this.turnOn = turnOn;
            activity = MainActivity.this;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // check internet connection
            if (!InternetUtil.isConnected(activity)) {
                // show error
                Toast.makeText(activity, R.string.no_internet_connection, Toast.LENGTH_LONG).show();

                cancel(true);
                return;
            }

            // all conditions is okay >> show progress
            showProgress(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // create json reader
            String url = AppController.END_POINT + "/send_turn_msg.php?vehicle_id=" + selectedVehicle.getId()
                    + "&turn_msg=" + (turnOn ? "A" : "a");
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
                showError(getString(R.string.connection_error_try_again));

                return;
            }

            // get success key from response
            boolean success;
            try {
                JSONObject jsonObject = new JSONObject(response);
                success = jsonObject.getInt("success") == 1 ? true : false;
            } catch (JSONException e) {
                success = false;
            }

            // check success
            if (success) {
                // show success msg
                showSuccess(turnOn ? R.string.will_be_turned_on_soon : R.string.will_be_turned_off_soon);
            } else {
                String msg = getString(R.string.unable_to_contact) + " " + selectedVehicle.getName();
                showError(msg);
            }
        }

        private void showProgress(boolean show) {
            ViewUtil.showView(progressBarTurn, show);
            ViewUtil.showView(layoutContentTurn, !show, View.INVISIBLE);

            // change cancelable property of turn on dialog
            dialogTurn.setCancelable(!show);
        }

        private void showError(String msg) {
            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();

            // hide progress
            showProgress(false);
        }

        private void showSuccess(int msgResId) {
            // set text success
            textSuccessTurn.setText(selectedVehicle.getName() + " " + activity.getString(msgResId));

            // hide progress
            showProgress(false);

            ViewUtil.showView(textSuccessTurn, true);
            ViewUtil.showView(progressBarTurn, false);
            ViewUtil.showView(layoutContentTurn, false, View.INVISIBLE);
        }

    }

    /**
     * sub class, used to send trip msg to vehicle
     */
    private class TripTask extends AsyncTask<Void, Void, Void> {
        private MainActivity activity;
        private boolean start;
        private String time;

        private String response;

        private TripTask(boolean start) {
            this.start = start;
            activity = MainActivity.this;

            // get formatted time
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            time = sdf.format(new Date());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // check internet connection
            if (!InternetUtil.isConnected(activity)) {
                // show error
                Toast.makeText(activity, R.string.no_internet_connection, Toast.LENGTH_LONG).show();

                cancel(true);
                return;
            }

            // all conditions is okay >> show progress
            showProgress(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // create json reader
            String url = AppController.END_POINT + "/send_trip_msg_to_vehicle.php?vehicle_id=" + selectedVehicle.getId()
                    + "&trip_msg=" + (start ? "start" : "end") + "&time=" + time;
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
                showError(getString(R.string.connection_error_try_again));
                return;
            }

            // get success key from response
            boolean success;
            try {
                JSONObject jsonObject = new JSONObject(response);
                success = jsonObject.getInt("success") == 1 ? true : false;
            } catch (JSONException e) {
                success = false;
            }

            // check success
            if (!success) {
                // show error msg
                String msg = getString(R.string.unable_to_contact) + " " + selectedVehicle.getName();
                showError(msg);
            }
        }

        private void showProgress(boolean show) {
            ViewUtil.showView(progressBarTrip, show);
            ViewUtil.showView(layoutContentTrip, !show, View.INVISIBLE);

            // change cancelable property of turn on dialog
            dialogTrip.setCancelable(!show);
        }

        private void showError(String msg) {
            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();

            // hide progress
            showProgress(false);
        }

    }

    /**
     * sub class, used to update tracking interval on server
     */
    private class UpdateTrackingIntervalTask extends AsyncTask<Void, Void, Void> {
        private MainActivity activity;
        private int trackingInterval;
        private String response;

        private UpdateTrackingIntervalTask(String trackingInterval) {
            activity = MainActivity.this;
            try {
                this.trackingInterval = Integer.parseInt(trackingInterval);
            } catch (Exception e) {
                this.trackingInterval = 0;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // validate inputs
            if (trackingInterval == 0) {
                // show error
                textTrackingInterval.setError(activity.getString(R.string.invalid_tracking_interval));

                cancel(true);
                return;
            }

            // hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(textTrackingInterval.getWindowToken(), 0);

            // check internet connection
            if (!InternetUtil.isConnected(activity)) {
                // show error
                Toast.makeText(activity, R.string.no_internet_connection, Toast.LENGTH_LONG).show();

                cancel(true);
                return;
            }

            // all conditions is okay >> show progress
            showProgress(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // create json reader
            String url = AppController.END_POINT + "/update_tracking_interval.php?username="
                    + AppController.getInstance(activity).getActiveUser().getUsername()
                    + "&tracking_interval=" + trackingInterval;
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
                showError(R.string.connection_error_try_again);

                return;
            }

            // check response
            if (response.equals(Constants.JSON_MSG_SUCCESS)) {
                // show success msg
                showSuccess();

                // update tracking interval in run time
                AppController.getInstance(activity).getActiveUser().setTrackingInterval(trackingInterval);

                // update user's new tracking interval
                UserDAO userDAO = new UserDAO(activity);
                userDAO.open();
                userDAO.update(AppController.getInstance(activity).getActiveUser());
                userDAO.close();
            } else {
                showError(R.string.connection_error_try_again);
            }
        }

        private void showProgress(boolean show) {
            ViewUtil.showView(progressBarTrackingInterval, show);
            ViewUtil.showView(layoutContentTrackingInterval, !show, View.INVISIBLE);

            // change cancelable property of turn on dialog
            dialogTrackingInterval.setCancelable(!show);
        }

        private void showError(int errorMsgRes) {
            Toast.makeText(activity, errorMsgRes, Toast.LENGTH_LONG).show();

            // hide progress
            showProgress(false);
        }

        private void showSuccess() {
            // hide progress
            showProgress(false);

            // show success
            ViewUtil.showView(textSuccessTrackingInterval, true);
            ViewUtil.showView(progressBarTrackingInterval, false);
            ViewUtil.showView(layoutContentTrackingInterval, false, View.INVISIBLE);
        }

    }

    /**
     * overriden method
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.REQ_CODE_VEHICLES:
                // check result
                if (resultCode == RESULT_OK) {
                    // get vehicles from database
                    vehicleDAO.open();
                    vehicles = vehicleDAO.getAll();
                    vehicleDAO.close();

                    // check vehicles size
                    if (vehicles.size() != 0) {
                        // update ui
                        updateUI();
                    } else {
                        finish();
                    }
                }

                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * method, used to show app msg
     */
    private void showAppMsg(int msgResId, AppMsg.Style style) {
        AppMsg.cancelAll(this);
        AppMsg appMsg = AppMsg.makeText(this, msgResId, style);
        appMsg.setParent(R.id.layout_content);
        appMsg.show();
    }

    @Override
    public void onDestroy() {
        // stop running tasks
        handler.removeCallbacks(runnable);

        for (AsyncTask task : tasks) {
            task.cancel(true);
        }

        // update vehicles locations in database
        for (Vehicle vehicle : vehicles) {
            vehicleDAO.open();
            vehicleDAO.update(vehicle);
            vehicleDAO.close();
        }

        // make active user is null
        AppController.getInstance(this).setActiveUser(null);

        // unregister receiver
        unregisterReceiver(mReceiver);

        super.onDestroy();
    }

    /*
     * sub class, used to listen for messages
	 */
    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                // get values
                Bundle extras = intent.getExtras();
                int message = extras.getInt(Constants.KEY_MESSAGE);
                String vehicleId = extras.getString(Constants.KEY_VEHICLE_ID);

                // hide progress
                ViewUtil.showView(progressBarTrip, false);
                ViewUtil.showView(layoutContentTrip, true, View.INVISIBLE);
                dialogTrip.setCancelable(true);

                if (message == Constants.MESSAGE_TRIP_ERROR) {
                    // show error msg
                    int msgResId = selectedVehicle.getTripStatus() == Trip.STATUS_END
                            ? R.string.error_starting_trip_check_your_vehicles_options
                            : R.string.error_ending_trip_check_your_vehicles_options;
                    Toast.makeText(getApplicationContext(), msgResId, Toast.LENGTH_LONG).show();

                } else if (message == Constants.MESSAGE_TRIP_STARTED || message == Constants.MESSAGE_TRIP_ENDED) {
                    // show success message
                    int msgResId = message == Constants.MESSAGE_TRIP_STARTED ? R.string.has_started_trip_successfully : R.string.has_ended_current_trip_successfully;
                    textSuccessTrip.setText(selectedVehicle.getName() + " " + getString(msgResId));
                    ViewUtil.showView(textSuccessTrip, true);
                    ViewUtil.showView(progressBarTrip, false);
                    ViewUtil.showView(layoutContentTrip, false, View.INVISIBLE);

                    // update trip status in runtime
                    int status = message == Constants.MESSAGE_TRIP_STARTED ? Trip.STATUS_START : Trip.STATUS_END;
                    if (selectedVehicle.getId().equals(vehicleId)) {
                        selectedVehicle.setTripStatus(status);
                    } else {
                        // loop for vehicles
                        for (Vehicle vehicle : vehicles) {
                            if (vehicle.getId().equals(vehicleId)) {
                                vehicle.setTripStatus(status);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}
