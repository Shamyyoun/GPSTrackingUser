package com.mahmoudelshamy.gpstracking.userapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import database.UserDAO;
import database.VehicleDAO;
import datamodels.Constants;
import datamodels.User;
import datamodels.Vehicle;
import json.JsonReader;
import json.UserHandler;
import json.VehiclesHandler;
import utils.InternetUtil;
import utils.ViewUtil;


public class LoginActivity extends ActionBarActivity implements View.OnClickListener {
    private Typeface typeface;

    // login views
    private View layoutLogin;
    private EditText textUsername;
    private EditText textPassword;
    private CheckBox checkRememberMe;
    private Button buttonLogin;
    private ProgressBar progressBarLogin;

    // bottom views
    private TextView textSignup;
    private TextView textGetPassword;

    // get password dialog objects
    private Dialog dialogGetPassword;
    private View layoutGetPasswordContent;
    private EditText textGPUsername;
    private Button buttonOk;
    private Button buttonCancel;
    private ProgressBar progressBarGetPassword;
    private TextView textSuccess;

    private VehicleDAO vehicleDAO;
    private List<AsyncTask> tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initComponents();
    }

    /**
     * method, used to initialize components
     */
    private void initComponents() {
        // init main views
        layoutLogin = findViewById(R.id.layout_login);
        textUsername = (EditText) findViewById(R.id.text_username);
        textPassword = (EditText) findViewById(R.id.text_password);
        checkRememberMe = (CheckBox) findViewById(R.id.check_rememberMe);
        buttonLogin = (Button) findViewById(R.id.button_login);
        progressBarLogin = (ProgressBar) findViewById(R.id.progress_login);
        textSignup = (TextView) findViewById(R.id.text_signup);
        textGetPassword = (TextView) findViewById(R.id.text_getPassword);

        vehicleDAO = new VehicleDAO(this);
        tasks = new ArrayList<AsyncTask>();

        // customize fonts
        typeface = Typeface.createFromAsset(getAssets(), "roboto_l.ttf");
        textUsername.setTypeface(typeface);
        textPassword.setTypeface(typeface);
        checkRememberMe.setTypeface(typeface);
        buttonLogin.setTypeface(typeface);
        textSignup.setTypeface(typeface);
        textGetPassword.setTypeface(typeface);

        // customize hints
        String color = String.format("#%06X", 0xFFFFFF & getResources().getColor(R.color.dark_white));
        textUsername.setHint(Html.fromHtml("<font color='" + color + "'>" + getString(R.string.username) + "</font>"));
        textPassword.setHint(Html.fromHtml("<font color='" + color + "'>" + getString(R.string.password) + "</font>"));

        // add listeners
        buttonLogin.setOnClickListener(this);
        textSignup.setOnClickListener(this);
        textGetPassword.setOnClickListener(this);
    }

    /**
     * method, used to initialize get password dialog
     */
    private void initGetPasswordDialog() {
        // init views
        dialogGetPassword = new Dialog(this);
        dialogGetPassword.setTitle(R.string.get_password);
        dialogGetPassword.setContentView(R.layout.dialog_get_password);
        layoutGetPasswordContent = dialogGetPassword.findViewById(R.id.layout_content);
        textGPUsername = (EditText) dialogGetPassword.findViewById(R.id.text_username);
        buttonOk = (Button) dialogGetPassword.findViewById(R.id.button_ok);
        buttonCancel = (Button) dialogGetPassword.findViewById(R.id.button_cancel);
        progressBarGetPassword = (ProgressBar) dialogGetPassword.findViewById(R.id.progressBar);
        textSuccess = (TextView) dialogGetPassword.findViewById(R.id.text_success);

        // customize fonts
        textGPUsername.setTypeface(typeface);
        buttonOk.setTypeface(typeface);
        buttonCancel.setTypeface(typeface);
        textSuccess.setTypeface(typeface);

        // add click listeners
        buttonOk.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);
    }

    /**
     * overriden method, used to handle click actions
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_login:
                // execute login task
                String username = textUsername.getText().toString();
                String password = textPassword.getText().toString();
                new LoginTask(username, password).execute();
                break;

            case R.id.text_signup:
                // open sign up activity
                Intent intent = new Intent(this, SignupActivity.class);
                startActivityForResult(intent, Constants.REQ_CODE_SIGNUP);
                break;

            case R.id.text_getPassword:
                // open get password dialog
                initGetPasswordDialog();
                dialogGetPassword.show();
                break;

            case R.id.button_ok:
                // execute forgot password task
                String gpPassword = textGPUsername.getText().toString();
                new ForgotPasswordTask(gpPassword).execute();
                break;

            case R.id.button_cancel:
                // dismiss dialog
                dialogGetPassword.dismiss();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.REQ_CODE_SIGNUP:
                // check result code
                if (resultCode == RESULT_OK) {
                    // show success msg
                    Toast.makeText(this, R.string.account_created_successfully, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    /**
     * sub class, used to send login request
     */
    private class LoginTask extends AsyncTask<Void, Void, Void> {
        private String username;
        private String password;

        private LoginActivity activity;
        private String response;

        private LoginTask(String username, String password) {
            this.username = username;
            this.password = password;

            activity = LoginActivity.this;

            tasks.add(this); // save reference to this task to destroy it if required
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // validate inputs
            username = username.trim();
            password = password.trim();
            if (username.isEmpty()) {
                textUsername.setText("");
                textUsername.setError(getString(R.string.username_cant_be_empty));

                cancel(true);
                return;
            }
            if (password.isEmpty()) {
                textPassword.setText("");
                textPassword.setError(getString(R.string.password_cant_be_empty));

                cancel(true);
                return;
            }

            // check internet connection
            if (!InternetUtil.isConnected(activity)) {
                showError(R.string.no_internet_connection);

                cancel(true);
                return;
            }

            // all conditions is true >> show progress
            showProgress(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // create json reader
            String url = AppController.END_POINT + "/user_login.php";
            JsonReader jsonReader = new JsonReader(url);

            // prepare parameters
            List<NameValuePair> parameters = new ArrayList<>(2);
            parameters.add(new BasicNameValuePair("username", username));
            parameters.add(new BasicNameValuePair("password", password));

            // execute request
            response = jsonReader.sendGetRequest(parameters);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // ensure response is not null
            if (response == null) {
                showError(R.string.connection_error_try_again);

                return;
            }

            if (response.equals(Constants.JSON_MSG_ERROR)) {
                // invalid username or password
                showError(R.string.invalid_username_or_password);

                return;
            }

            if (response.equals(Constants.JSON_MSG_NOT_VERIFIED)) {
                // not verified
                showError(R.string.check_your_mail_and_activate_account);

                return;
            }

            // --response is valid, handle it--
            UserHandler handler = new UserHandler(response);
            User user = handler.handle();

            // check handling operation
            if (user == null) {
                showError(R.string.connection_error_try_again);

                return;
            }

            // --user object is valid--
            // save it runtime
            AppController.getInstance(activity.getApplicationContext()).setActiveUser(user);

            if (checkRememberMe.isChecked()) {
                // save it in database
                UserDAO userDAO = new UserDAO(activity);
                userDAO.open();
                userDAO.add(user);
                userDAO.close();
            }

            // register user and send his reg_id to server
            AppController.registerToGCM(activity);

            // execute vehicles task to get vehicles from server
            new VehiclesTask().execute();
        }

        /**
         * sub class, used to get vehicles from server after successful login
         */
        private class VehiclesTask extends AsyncTask<Void, Void, Void> {
            private User user;
            private String response;

            private VehiclesTask() {
                user = AppController.getInstance(activity).getActiveUser();
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

                // create intent to start suitable activity
                Intent intent = new Intent();

                // validate response
                if (response != null) {
                    // handle it
                    VehiclesHandler handler = new VehiclesHandler(response);
                    List<Vehicle> vehicles = handler.handle();

                    // check handling operation result
                    if (vehicles != null) {
                        // delete all vehicles in database
                        vehicleDAO.open();
                        vehicleDAO.deleteAll();

                        // check vehicles size
                        if (vehicles.size() != 0) {
                            // check user's tyye
                            if (user.getType() == User.TYPE_PERSONAL) {
                                // personal >> change vehicles list to first vehicle only
                                Vehicle firstVehicle = vehicles.get(0);
                                vehicles.clear();
                                vehicles.add(firstVehicle);
                            }

                            // save them in database
                            vehicleDAO.add(vehicles);

                            // set intent's activity to MainActivity
                            intent.setClass(activity, MainActivity.class);
                        } else {
                            // set intent's activity to AddVehicleActivity
                            intent.setClass(activity, VehiclesActivity.class);
                        }

                        vehicleDAO.close();
                    } else {
                        // set intent's activity to AddVehicleActivity
                        intent.setClass(activity, VehiclesActivity.class);
                    }
                } else {
                    // set intent's activity to AddVehicleActivity
                    intent.setClass(activity, VehiclesActivity.class);
                }

                // start suitable activity
                activity.startActivity(intent);
                overridePendingTransition(R.anim.main_enter, R.anim.login_exit);
                finish();
            }
        }

        private void showProgress(boolean show) {
            ViewUtil.showView(progressBarLogin, show);
            ViewUtil.showView(layoutLogin, !show, View.INVISIBLE);
        }

        private void showError(int errorMsgRes) {
            Toast.makeText(activity, errorMsgRes, Toast.LENGTH_LONG).show();

            // hide progress
            showProgress(false);
        }
    }

    /**
     * sub class, used to send forgot password request
     */
    private class ForgotPasswordTask extends AsyncTask<Void, Void, Void> {
        private String username;

        private LoginActivity activity;
        private String response;

        private ForgotPasswordTask(String username) {
            this.username = username;

            activity = LoginActivity.this;

            tasks.add(this); // save reference to this task to destroy it if required
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(textGPUsername.getWindowToken(), 0);

            // check internet connection
            if (!InternetUtil.isConnected(activity)) {
                showError(R.string.no_internet_connection);

                cancel(true);
                return;
            }

            // validate inputs
            username = username.trim();
            if (username.isEmpty()) {
                textGPUsername.setText("");
                textGPUsername.setError(getString(R.string.username_cant_be_empty));

                cancel(true);
                return;
            }

            // all conditions is true >> show progress
            showProgress(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // create json reader
            String url = AppController.END_POINT + "/forget_password.php";
            JsonReader jsonReader = new JsonReader(url);

            // prepare parameters
            List<NameValuePair> parameters = new ArrayList<>(1);
            parameters.add(new BasicNameValuePair("username", username));

            // execute request
            response = jsonReader.sendGetRequest(parameters);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // ensure response is not null
            if (response == null) {
                showError(R.string.connection_error_try_again);

                return;
            }

            if (response.equals(Constants.JSON_MSG_ERROR)) {
                // invalid username or password
                showError(R.string.invalid_username);

                return;
            }

            if (response.equals(Constants.JSON_MSG_SUCCESS)) {
                // show success msg
                showSuccess();
            } else {
                // unexpected error
                showError(R.string.connection_error_try_again);
            }
        }

        private void showProgress(boolean show) {
            ViewUtil.showView(progressBarGetPassword, show);
            ViewUtil.showView(layoutGetPasswordContent, !show, View.INVISIBLE);

            // change cancelable property of get password dialog
            dialogGetPassword.setCancelable(!show);
        }

        private void showError(int errorMsgRes) {
            Toast.makeText(activity, errorMsgRes, Toast.LENGTH_LONG).show();

            // hide progress
            showProgress(false);
        }

        private void showSuccess() {
            // hide progress
            showProgress(false);

            ViewUtil.showView(textSuccess, true);
            ViewUtil.showView(progressBarGetPassword, false);
            ViewUtil.showView(layoutGetPasswordContent, false, View.INVISIBLE);
        }
    }

    /**
     * overriden method
     */
    @Override
    protected void onDestroy() {
        // stop all running tasks
        for (AsyncTask task : tasks) {
            task.cancel(true);
        }

        super.onDestroy();
    }
}
