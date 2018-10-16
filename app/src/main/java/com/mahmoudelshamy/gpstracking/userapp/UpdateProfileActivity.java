package com.mahmoudelshamy.gpstracking.userapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import database.UserDAO;
import datamodels.Constants;
import datamodels.User;
import json.JsonReader;
import utils.InternetUtil;


public class UpdateProfileActivity extends ActionBarActivity {
    private EditText textName;
    private EditText textType;
    private EditText textUsername;
    private EditText textEmail;
    private EditText textPassword;
    private EditText textRePassword;
    private View layoutOrgData;
    private EditText textAddress;
    private EditText textOrgType;
    private Button buttonUpdate;

    private List<AsyncTask> tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        initComponents();
    }

    /**
     * method, used to initialize components
     */
    private void initComponents() {
        textName = (EditText) findViewById(R.id.text_name);
        textType = (EditText) findViewById(R.id.text_type);
        textUsername = (EditText) findViewById(R.id.text_username);
        textEmail = (EditText) findViewById(R.id.text_email);
        textPassword = (EditText) findViewById(R.id.text_password);
        textRePassword = (EditText) findViewById(R.id.text_rePassword);
        layoutOrgData = findViewById(R.id.layout_orgData);
        textAddress = (EditText) findViewById(R.id.text_address);
        textOrgType = (EditText) findViewById(R.id.text_orgType);
        buttonUpdate = (Button) findViewById(R.id.button_update);
        tasks = new ArrayList<AsyncTask>();

        // customize fonts
        Typeface typeface = Typeface.createFromAsset(getAssets(), "roboto_l.ttf");
        textName.setTypeface(typeface);
        textType.setTypeface(typeface);
        textUsername.setTypeface(typeface);
        textEmail.setTypeface(typeface);
        textPassword.setTypeface(typeface);
        textRePassword.setTypeface(typeface);
        textAddress.setTypeface(typeface);
        textOrgType.setTypeface(typeface);
        buttonUpdate.setTypeface(typeface);

        // customize hints
        String color = String.format("#%06X", 0xFFFFFF & getResources().getColor(R.color.dark_white));
        textName.setHint(Html.fromHtml("<font color='" + color + "'>" + getString(R.string.your_name) + "</font>"));
        textType.setHint(Html.fromHtml("<font color='" + color + "'>" + getString(R.string.account_type) + "</font>"));
        textUsername.setHint(Html.fromHtml("<font color='" + color + "'>" + getString(R.string.username) + "</font>"));
        textEmail.setHint(Html.fromHtml("<font color='" + color + "'>" + getString(R.string.email_address) + "</font>"));
        textPassword.setHint(Html.fromHtml("<font color='" + color + "'>" + getString(R.string.password) + "</font>"));
        textRePassword.setHint(Html.fromHtml("<font color='" + color + "'>" + getString(R.string.re_password) + "</font>"));
        textAddress.setHint(Html.fromHtml("<font color='" + color + "'>" + getString(R.string.address) + "</font>"));
        textOrgType.setHint(Html.fromHtml("<font color='" + color + "'>" + getString(R.string.org_type) + "</font>"));

        // hide keyboard for first activity appear
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // set initial data
        User activeUser = AppController.getInstance(getApplicationContext()).getActiveUser();
        textName.setText(activeUser.getName());
        textType.setText(activeUser.getType() == User.TYPE_PERSONAL ? R.string.personal : R.string.organization);
        textUsername.setText(activeUser.getUsername());
        textEmail.setText(activeUser.getEmail());
        textPassword.setText(activeUser.getPassword());
        textRePassword.setText(activeUser.getPassword());
        if (activeUser.getType() == User.TYPE_ORGANIZATION) {
            // show additional organization info inputs
            layoutOrgData.setVisibility(View.VISIBLE);
            textOrgType.setText(activeUser.getOrgType());
            textAddress.setText(activeUser.getAddress());
        }

        // add listeners
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UpdateTask().execute();
            }
        });
    }

    /**
     * sub class, used to update user's profile
     */
    private class UpdateTask extends AsyncTask<Void, Void, Void> {
        private UpdateProfileActivity activity;
        private String username;
        private String name;
        private String password;
        private String rePassword;
        private String address;
        private String orgType;

        private ProgressDialog progressDialog;
        private String response;

        private UpdateTask() {
            activity = UpdateProfileActivity.this;

            username = textUsername.getText().toString();
            name = textName.getText().toString();
            password = textPassword.getText().toString();
            rePassword = textRePassword.getText().toString();
            address = textAddress.getText().toString();
            orgType = textOrgType.getText().toString();

            progressDialog = new ProgressDialog(activity);
            progressDialog.setMessage(activity.getString(R.string.please_wait));
            progressDialog.setCancelable(false);

            tasks.add(this); // save reference to this task to destroy it if required
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // validate inputs
            name = name.trim();
            password = password.trim();
            rePassword = rePassword.trim();
            address = address.trim();
            orgType = orgType.trim();

            if (name.isEmpty()) {
                textName.setText("");
                textName.setError(getString(R.string.your_name_cant_be_empty));

                cancel(true);
                return;
            }

            if (password.isEmpty()) {
                textPassword.setText("");
                textPassword.setError(getString(R.string.password_cant_be_empty));

                cancel(true);
                return;
            }

            if (!rePassword.equals(password)) {
                textRePassword.setError(getString(R.string.password_doesnt_match));

                cancel(true);
                return;
            }

            // hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(textName.getWindowToken(), 0);

            // check internet connection
            if (!InternetUtil.isConnected(activity)) {
                showMsg(R.string.no_internet_connection);

                cancel(true);
                return;
            }

            // all conditions is true >> show progress
            showProgress(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // create json reader
            String url = AppController.END_POINT + "/update_user.php";
            JsonReader jsonReader = new JsonReader(url);

            // prepare parameters
            List<NameValuePair> parameters = new ArrayList<>(5);
            parameters.add(new BasicNameValuePair("username", username));
            parameters.add(new BasicNameValuePair("name", name));
            parameters.add(new BasicNameValuePair("password", password));
            parameters.add(new BasicNameValuePair("address", address));
            parameters.add(new BasicNameValuePair("org_type", orgType));

            // execute request
            response = jsonReader.sendGetRequest(parameters);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // ensure response is not null
            if (response == null) {
                showMsg(R.string.connection_error_try_again);

                return;
            }

            if (response.equals(Constants.JSON_MSG_SUCCESS)) {
                // --updated successfully--
                // update user in runtime
                User activeUser = AppController.getInstance(activity).getActiveUser();
                activeUser.setName(name);
                activeUser.setPassword(password);
                activeUser.setAddress(address);
                activeUser.setOrgType(orgType);

                // update saved user in DB
                UserDAO userDAO = new UserDAO(activity);
                userDAO.open();
                userDAO.update(activeUser);
                userDAO.close();

                // show success msg
                showMsg(R.string.profile_updated_successfully);

                // finish activity
                finish();
            } else {
                showMsg(R.string.connection_error_try_again);
            }
        }

        private void showProgress(boolean show) {
            if (show)
                progressDialog.show();
            else
                progressDialog.hide();
        }

        private void showMsg(int msgRes) {
            Toast.makeText(activity, msgRes, Toast.LENGTH_LONG).show();

            // hide progress
            showProgress(false);
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
