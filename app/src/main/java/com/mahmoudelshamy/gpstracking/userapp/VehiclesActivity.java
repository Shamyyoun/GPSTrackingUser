package com.mahmoudelshamy.gpstracking.userapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.devspark.appmsg.AppMsg;

import java.util.ArrayList;
import java.util.List;

import adapters.VehiclesAdapter;
import database.VehicleDAO;
import datamodels.Constants;
import datamodels.User;
import datamodels.Vehicle;
import json.JsonReader;
import utils.InternetUtil;
import utils.ViewUtil;
import views.SlideExpandableListView;


public class VehiclesActivity extends ActionBarActivity implements View.OnClickListener, VehiclesAdapter.VehicleListener {
    // vehicles operations constants
    private static final int OPERATION_ADD = 1;
    private static final int OPERATION_UPDATE = 2;
    private static final int OPERATION_DELETE = 3;

    // main objects
    private boolean cameFromMain;
    private VehicleDAO dao;
    private List<Vehicle> vehicles;
    private VehiclesAdapter vehiclesAdapter;
    private Typeface typeface;

    // main views
    private View mainView;
    private SlideExpandableListView listVehicles;
    private View emptyView;
    private TextView textNoVehicles;
    private TextView textTapToAdd;
    private ImageButton buttonAddEmptyView;

    // vehicle dialog views
    private Dialog dialogVehicle;
    private View layoutVehicleContent;
    private EditText textIdVehicle;
    private EditText textPassword;
    private EditText textName;
    private EditText textPurpose;
    private EditText textLicenceNumber;
    private EditText textNumber;
    private EditText textColor;
    private EditText textYear;
    private EditText textModel;
    private EditText textBrand;
    private Button buttonOkVehicle;
    private Button buttonCancelVehicle;
    private ProgressBar progressBarVehicle;
    private TextView textSuccessVehicle;
    private TextView textOperation; // used to hold operation type when executing vehicle task
    private TextView textPositionVehicle; // used to hold vehicle's position in listview when executing vehicle task

    // delete vehicle dialog objects
    private Dialog dialogDeleteVehicle;
    private View layoutDeleteDialogContent;
    private Button buttonOkDeleteVehicle;
    private Button buttonCancelDeleteVehicle;
    private ProgressBar progressBarDeleteVehicle;
    private TextView textSuccessDeleteVehicle;
    private TextView textIdDeleteVehicle;
    private TextView textPositionDeleteVehicle;

    private List<AsyncTask> tasks; // used to hold running tasks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicles);

        initComponents();
    }

    /**
     * method, used to init components
     */
    private void initComponents() {
        cameFromMain = getIntent().getBooleanExtra(Constants.KEY_CAME_FROM_MAIN, false);
        dao = new VehicleDAO(this);
        typeface = Typeface.createFromAsset(getAssets(), "roboto_l.ttf");

        mainView = findViewById(R.id.view_main);
        listVehicles = (SlideExpandableListView) findViewById(R.id.list_vehicles);
        emptyView = findViewById(R.id.view_empty);
        textNoVehicles = (TextView) emptyView.findViewById(R.id.text_noVehicles);
        textTapToAdd = (TextView) emptyView.findViewById(R.id.text_tapToAdd);
        buttonAddEmptyView = (ImageButton) emptyView.findViewById(R.id.button_add);

        tasks = new ArrayList<AsyncTask>();

        // customize actionbar
        getSupportActionBar().setElevation(0);

        // customize fonts
        Typeface typeface = Typeface.createFromAsset(getAssets(), "roboto_l.ttf");
        textNoVehicles.setTypeface(typeface);
        textTapToAdd.setTypeface(typeface);

        // get vehicles
        dao.open();
        vehicles = dao.getAll();
        dao.close();

        // check vehicles list size
        if (vehicles.size() == 0) {
            // show empty view
            ViewUtil.showView(mainView, false);
            ViewUtil.showView(emptyView, true);
        } else {
            // check user type
            if (AppController.getInstance(this).getActiveUser().getType() == User.TYPE_PERSONAL) {
                // personal >> change vehicles list to first vehicle only
                Vehicle firstVehicle = vehicles.get(0);
                vehicles.clear();
                vehicles.add(firstVehicle);
            }
        }

        // set vehicles adapter
        vehiclesAdapter = new VehiclesAdapter(this, R.layout.list_vehicles_item, R.layout.list_vehicles_subitem, vehicles, this);
        listVehicles.setAdapter(vehiclesAdapter);

        // add listeners
        buttonAddEmptyView.setOnClickListener(this);
        listVehicles.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (listVehicles.isGroupExpanded(groupPosition)) {
                    listVehicles.collapseGroupWithAnimation(groupPosition);
                } else {
                    listVehicles.expandGroupWithAnimation(groupPosition);
                }

                return true;
            }
        });
    }

    /**
     * method, used to init add vehicle dialog
     */
    private void initVehicleDialog(int titleResId) {
        dialogVehicle = new Dialog(this);
        dialogVehicle.setContentView(R.layout.dialog_vehicle);
        dialogVehicle.setTitle(titleResId);
        layoutVehicleContent = dialogVehicle.findViewById(R.id.layout_content);
        textIdVehicle = (EditText) dialogVehicle.findViewById(R.id.text_id);
        textPassword = (EditText) dialogVehicle.findViewById(R.id.text_password);
        textName = (EditText) dialogVehicle.findViewById(R.id.text_name);
        textPurpose = (EditText) dialogVehicle.findViewById(R.id.text_purpose);
        textLicenceNumber = (EditText) dialogVehicle.findViewById(R.id.text_licenceNumber);
        textNumber = (EditText) dialogVehicle.findViewById(R.id.text_number);
        textColor = (EditText) dialogVehicle.findViewById(R.id.text_color);
        textYear = (EditText) dialogVehicle.findViewById(R.id.text_year);
        textModel = (EditText) dialogVehicle.findViewById(R.id.text_model);
        textBrand = (EditText) dialogVehicle.findViewById(R.id.text_brand);
        buttonOkVehicle = (Button) dialogVehicle.findViewById(R.id.button_okVehicle);
        buttonCancelVehicle = (Button) dialogVehicle.findViewById(R.id.button_cancelVehicle);
        progressBarVehicle = (ProgressBar) dialogVehicle.findViewById(R.id.progressBar);
        textSuccessVehicle = (TextView) dialogVehicle.findViewById(R.id.text_success);
        textOperation = (TextView) dialogVehicle.findViewById(R.id.text_operation);
        textPositionVehicle = (TextView) dialogVehicle.findViewById(R.id.text_position);

        // customize fonts
        textIdVehicle.setTypeface(typeface);
        textPassword.setTypeface(typeface);
        textName.setTypeface(typeface);
        textPurpose.setTypeface(typeface);
        textLicenceNumber.setTypeface(typeface);
        textNumber.setTypeface(typeface);
        textColor.setTypeface(typeface);
        textYear.setTypeface(typeface);
        textModel.setTypeface(typeface);
        textBrand.setTypeface(typeface);
        buttonOkVehicle.setTypeface(typeface);
        buttonCancelVehicle.setTypeface(typeface);
        textSuccessVehicle.setTypeface(typeface);

        buttonOkVehicle.setOnClickListener(this);
        buttonCancelVehicle.setOnClickListener(this);
    }

    /**
     * method, used to initialize delete vehicle dialog
     */
    private void initDeleteVehicleDialog() {
        dialogDeleteVehicle = new Dialog(this);
        dialogDeleteVehicle.setTitle(R.string.delete_vehicle);
        dialogDeleteVehicle.setContentView(R.layout.dialog_delete_vehicle);
        layoutDeleteDialogContent = dialogDeleteVehicle.findViewById(R.id.layout_content);
        buttonOkDeleteVehicle = (Button) dialogDeleteVehicle.findViewById(R.id.button_okDeleteVehicle);
        buttonCancelDeleteVehicle = (Button) dialogDeleteVehicle.findViewById(R.id.button_cancelDeleteVehicle);
        progressBarDeleteVehicle = (ProgressBar) dialogDeleteVehicle.findViewById(R.id.progressBar);
        textSuccessDeleteVehicle = (TextView) dialogDeleteVehicle.findViewById(R.id.text_success);
        textIdDeleteVehicle = (TextView) dialogDeleteVehicle.findViewById(R.id.text_vehicleId);
        textPositionDeleteVehicle = (TextView) dialogDeleteVehicle.findViewById(R.id.text_position);

        // customize fonts
        buttonOkDeleteVehicle.setTypeface(typeface);
        buttonCancelDeleteVehicle.setTypeface(typeface);
        textSuccessDeleteVehicle.setTypeface(typeface);

        // add listeners
        buttonOkDeleteVehicle.setOnClickListener(this);
        buttonCancelDeleteVehicle.setOnClickListener(this);
    }

    /**
     * overriden method, used to handle click actions
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_add:
                // show vehicle dialog
                initVehicleDialog(R.string.add_vehicle);
                textOperation.setText("" + OPERATION_ADD);
                textPositionVehicle.setText("0");
                dialogVehicle.show();
                break;

            case R.id.button_okVehicle:
                // get vehicle's data and create object
                String id = textIdVehicle.getText().toString().trim();
                String password = textPassword.getText().toString().trim();
                String name = textName.getText().toString().trim();
                String purpose = textPurpose.getText().toString().trim();
                String color = textColor.getText().toString().trim();
                String model = textModel.getText().toString().trim();
                String brand = textBrand.getText().toString().trim();
                int licenceNumber;
                int number;
                int year;
                try {
                    licenceNumber = Integer.parseInt(textLicenceNumber.getText().toString().trim());
                } catch (Exception e) {
                    licenceNumber = 0;
                }
                try {
                    number = Integer.parseInt(textNumber.getText().toString().trim());
                } catch (Exception e) {
                    number = 0;
                }
                try {
                    year = Integer.parseInt(textYear.getText().toString().trim());
                } catch (Exception e) {
                    year = 0;
                }
                Vehicle vehicle1 = new Vehicle(id, password, name, purpose, licenceNumber, number, color, model, year, brand);

                // get hidden parameters
                int operation1 = 0;
                try {
                    operation1 = Integer.parseInt(textOperation.getText().toString());
                } catch (Exception e) {
                }
                int position1 = 0;
                try {
                    position1 = Integer.parseInt(textPositionVehicle.getText().toString());
                } catch (Exception e) {
                }

                // execute vehicle task with this operation
                new VehicleTask(vehicle1, operation1, position1).execute();
                break;

            case R.id.button_cancelVehicle:
                // dismiss vehicle dialog
                dialogVehicle.dismiss();
                break;

            case R.id.button_okDeleteVehicle:
                // get hidden parameters
                String vehicleId = textIdDeleteVehicle.getText().toString();
                int position2 = 0;
                try {
                    position2 = Integer.parseInt(textPositionDeleteVehicle.getText().toString());
                } catch (Exception e) {
                }

                // create vehicle's object
                Vehicle vehicle2 = new Vehicle(vehicleId);

                // execute vehicle task with this operation
                new VehicleTask(vehicle2, OPERATION_DELETE, position2).execute();
                break;

            case R.id.button_cancelDeleteVehicle:
                // dismiss delete vehicle dialog
                dialogDeleteVehicle.dismiss();
                break;
        }
    }

    /**
     * triggered by VehiclesAdapter to edit vehicle
     */
    @Override
    public void editVehicle(Vehicle vehicle, int position) {
        // init vehicle dialog with passed vehicle
        initVehicleDialog(R.string.edit_vehicle);
        textIdVehicle.setEnabled(false);
        textIdVehicle.setText(vehicle.getId());
        textPassword.setText(vehicle.getPassword());
        textName.setText(vehicle.getName());
        textPurpose.setText(vehicle.getPurpose());
        textLicenceNumber.setText(vehicle.getLicenceNumber() == 0 ? "" : "" + vehicle.getLicenceNumber());
        textNumber.setText(vehicle.getNumber() == 0 ? "" : "" + vehicle.getNumber());
        textColor.setText(vehicle.getColor());
        textYear.setText(vehicle.getYear() == 0 ? "" : "" + vehicle.getYear());
        textModel.setText(vehicle.getModel());
        textBrand.setText(vehicle.getBrand());
        textOperation.setText("" + OPERATION_UPDATE);
        textPositionVehicle.setText("" + position);

        // show the dialog
        dialogVehicle.show();
    }

    /**
     * triggered by VehiclesAdapter to delete vehicle
     */
    @Override
    public void deleteVehicle(Vehicle vehicle, int position) {
        // init delete vehicle dialog
        initDeleteVehicleDialog();

        // set data
        textIdDeleteVehicle.setText(vehicle.getId());
        textPositionDeleteVehicle.setText("" + position);

        // show the dialog
        dialogDeleteVehicle.show();
    }

    /**
     * overriden method
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // check user type
        if (AppController.getInstance(this).getActiveUser().getType() == User.TYPE_ORGANIZATION) {
            // inflate menu
            MenuInflater menuInflater = new MenuInflater(this);
            menuInflater.inflate(R.menu.menu_vehicles, menu);
            return true;
        } else {
            // don't inflate menu
            return super.onCreateOptionsMenu(menu);
        }
    }

    /**
     * overriden method
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_addVehicle:
                // show add vehicle dialog
                initVehicleDialog(R.string.add_vehicle);
                textOperation.setText("" + OPERATION_ADD);
                dialogVehicle.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * sub class used to do operation on vehicle in server
     */
    private class VehicleTask extends AsyncTask<Void, Void, Void> {
        private VehiclesActivity activity;
        private Vehicle vehicle;
        private int operation;
        private int vehiclePosition; // vehicle position in listview

        private String response;

        private VehicleTask(Vehicle vehicle, int operation, int vehiclePosition) {
            activity = VehiclesActivity.this;
            this.vehicle = vehicle;
            this.operation = operation;
            this.vehiclePosition = vehiclePosition;

            tasks.add(this); // hold reference to this task, to destroy it if required
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // check operation
            if (operation == OPERATION_ADD || operation == OPERATION_UPDATE) {
                // hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(textIdVehicle.getWindowToken(), 0);

                // validate inputs
                if (vehicle.getId().isEmpty()) {
                    textIdVehicle.setError(activity.getString(R.string.vehicle_id_cant_be_empty));

                    cancel(true);
                    return;
                }

                if (vehicle.getPassword().isEmpty()) {
                    textPassword.setError(activity.getString(R.string.vehicle_password_cant_be_empty));

                    cancel(true);
                    return;
                }

                if (vehicle.getName().isEmpty()) {
                    textName.setError(activity.getString(R.string.vehicle_name_cant_be_empty));

                    cancel(true);
                    return;
                }
            }

            // check internet connection
            if (!InternetUtil.isConnected(activity)) {
                showError(R.string.no_internet_connection);

                cancel(true);
                return;
            }

            // show progress
            showProgress(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // create suitable url
            String url = AppController.END_POINT;
            if (operation == OPERATION_ADD)
                url += "/add_vehicle.php";
            else if (operation == OPERATION_UPDATE)
                url += "/update_vehicle.php";
            else if (operation == OPERATION_DELETE)
                url += "/delete_vehicle.php";

            // add parameters to url
            if (operation == OPERATION_ADD || operation == OPERATION_UPDATE) {
                url += "?username=" + AppController.getInstance(getApplicationContext()).getActiveUser().getUsername()
                        + "&vehicle_id=" + vehicle.getId() + "&password=" + vehicle.getPassword()
                        + "&name=" + vehicle.getName() + "&purpose=" + vehicle.getPurpose()
                        + "&licence_number=" + vehicle.getLicenceNumber() + "&number=" + vehicle.getNumber()
                        + "&color=" + vehicle.getColor() + "&model=" + vehicle.getModel()
                        + "&year=" + vehicle.getYear() + "&brand=" + vehicle.getBrand();
            } else if (operation == OPERATION_DELETE) {
                url += "?vehicle_id=" + vehicle.getId();
            }

            // create and execute json reader
            JsonReader jsonReader = new JsonReader(url);
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
            if (response.equals(Constants.JSON_MSG_EXISTS)) {
                // user tried to add vehicle and vehicle id exists
                showError(R.string.vehicle_id_exists);

                return;
            }

            if (response.equals(Constants.JSON_MSG_SUCCESS)) {
                // check operation
                dao.open();
                if (operation == OPERATION_ADD) {
                    showSuccess(R.string.vehicle_added_successfully);

                    // add vehicle to database
                    dao.add(vehicle);

                    // add vehicle to vehicles list
                    vehicles.add(vehicle);
                } else if (operation == OPERATION_UPDATE) {
                    showSuccess(R.string.vehicle_updated_successfully);

                    // update vehicle in database
                    dao.update(vehicle);

                    // update vehicle in vehicles list
                    vehicles.set(vehiclePosition, vehicle);
                } else if (operation == OPERATION_DELETE) {
                    showSuccess(R.string.vehicle_deleted_successfully);

                    // delete vehicle from database
                    dao.delete(vehicle.getId());

                    // delete vehicle from vehicles list
                    vehicles.remove(vehiclePosition);
                }
                dao.close();

                // update vehicles adapter
                vehiclesAdapter.notifyDataSetChanged();

                // set ok result
                setResult(RESULT_OK);

                // show/hide empty view
                ViewUtil.showView(emptyView, vehicles.size() == 0);
                ViewUtil.showView(mainView, vehicles.size() != 0);
            } else {
                // user tried to add / update / delete vehicle and operation failed
                showError(R.string.connection_error_try_again);
            }
        }

        private void showProgress(boolean show) {
            // check operation
            if (operation == OPERATION_ADD || operation == OPERATION_UPDATE) {
                ViewUtil.showView(progressBarVehicle, show);
                ViewUtil.showView(layoutVehicleContent, !show, View.INVISIBLE);

                // change cancelable property of add vehicle dialog
                dialogVehicle.setCancelable(!show);
            } else if (operation == OPERATION_DELETE) {
                ViewUtil.showView(progressBarDeleteVehicle, show);
                ViewUtil.showView(layoutDeleteDialogContent, !show, View.INVISIBLE);

                // change cancelable property of add vehicle dialog
                dialogDeleteVehicle.setCancelable(!show);
            }
        }

        private void showError(int errorMsgRes) {
            Toast.makeText(activity, errorMsgRes, Toast.LENGTH_LONG).show();

            // hide progress
            showProgress(false);
        }

        private void showSuccess(int msgResId) {
            // hide progress
            showProgress(false);

            // check operation
            if (operation == OPERATION_ADD || operation == OPERATION_UPDATE) {
                textSuccessVehicle.setText(msgResId);
                ViewUtil.showView(textSuccessVehicle, true);
                ViewUtil.showView(progressBarVehicle, false);
                ViewUtil.showView(layoutVehicleContent, false, View.INVISIBLE);
            } else if (operation == OPERATION_DELETE) {
                textSuccessDeleteVehicle.setText(msgResId);
                ViewUtil.showView(textSuccessDeleteVehicle, true);
                ViewUtil.showView(progressBarDeleteVehicle, false);
                ViewUtil.showView(layoutDeleteDialogContent, false, View.INVISIBLE);
            }
        }

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

    @Override
    public void onBackPressed() {
        // check parent
        if (!cameFromMain) {
            // check vehicles list size
            if (vehicles.size() != 0) {
                // start main activity
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        }

        finish();
    }
}
