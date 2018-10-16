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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adapters.SpinnerAdapter;
import datamodels.Constants;
import json.JsonReader;
import utils.InternetUtil;
import utils.ViewUtil;


public class SignupActivity extends ActionBarActivity {
    private EditText textName;
    private Spinner spinnerType;
    private EditText textUsername;
    private EditText textEmail;
    private EditText textPassword;
    private EditText textRePassword;
    private View layoutOrgData;
    private EditText textAddress;
    private EditText textOrgType;
    private Button buttonSignup;

    private List<AsyncTask> tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initComponents();
    }

    /**
     * method, used to initialize components
     */
    private void initComponents() {
        textName = (EditText) findViewById(R.id.text_name);
        spinnerType = (Spinner) findViewById(R.id.spinner_type);
        textUsername = (EditText) findViewById(R.id.text_username);
        textEmail = (EditText) findViewById(R.id.text_email);
        textPassword = (EditText) findViewById(R.id.text_password);
        textRePassword = (EditText) findViewById(R.id.text_rePassword);
        layoutOrgData = findViewById(R.id.layout_orgData);
        textAddress = (EditText) findViewById(R.id.text_address);
        textOrgType = (EditText) findViewById(R.id.text_orgType);
        buttonSignup = (Button) findViewById(R.id.button_signup);
        tasks = new ArrayList<AsyncTask>();

        // customize fonts
        Typeface typeface = Typeface.createFromAsset(getAssets(), "roboto_l.ttf");
        textName.setTypeface(typeface);
        textUsername.setTypeface(typeface);
        textEmail.setTypeface(typeface);
        textPassword.setTypeface(typeface);
        textRePassword.setTypeface(typeface);
        textAddress.setTypeface(typeface);
        textOrgType.setTypeface(typeface);
        buttonSignup.setTypeface(typeface);

        // customize hints
        String color = String.format("#%06X", 0xFFFFFF & getResources().getColor(R.color.dark_white));
        textName.setHint(Html.fromHtml("<font color='" + color + "'>" + getString(R.string.your_name) + "</font>"));
        textUsername.setHint(Html.fromHtml("<font color='" + color + "'>" + getString(R.string.username) + "</font>"));
        textEmail.setHint(Html.fromHtml("<font color='" + color + "'>" + getString(R.string.email_address) + "</font>"));
        textPassword.setHint(Html.fromHtml("<font color='" + color + "'>" + getString(R.string.password) + "</font>"));
        textRePassword.setHint(Html.fromHtml("<font color='" + color + "'>" + getString(R.string.re_password) + "</font>"));
        textAddress.setHint(Html.fromHtml("<font color='" + color + "'>" + getString(R.string.address) + "</font>"));
        textOrgType.setHint(Html.fromHtml("<font color='" + color + "'>" + getString(R.string.org_type) + "</font>"));

        // customize spinner
        String[] types = getResources().getStringArray(R.array.acc_type);
        SpinnerAdapter adapter = new SpinnerAdapter(this, R.layout.spinner_type_item, types);
        spinnerType.setAdapter(adapter);

        // hide keyboard for first activity appear
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // add listeners
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // show or hide orgData layout
                ViewUtil.showView(layoutOrgData, position == 2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SignupTask().execute();
            }
        });
    }

    /**
     * sub class, used to send sign up request
     */
    private class SignupTask extends AsyncTask<Void, Void, Void> {
        private SignupActivity activity;
        private String name;
        private int accountType;
        private String username;
        private String email;
        private String password;
        private String rePassword;
        private String address;
        private String orgType;

        private ProgressDialog progressDialog;
        private String response;

        private SignupTask() {
            activity = SignupActivity.this;

            name = textName.getText().toString();
            accountType = spinnerType.getSelectedItemPosition();
            username = textUsername.getText().toString();
            email = textEmail.getText().toString();
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
            username = username.trim();
            email = email.trim();
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

            if (accountType == 0) {
                showError(R.string.select_your_account_type);

                cancel(true);
                return;
            }

            if (email.isEmpty()) {
                textEmail.setText("");
                textEmail.setError(getString(R.string.email_cant_be_empty));

                cancel(true);
                return;
            }

            // ensure that email is valid
            String pattern = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(email);
            if (!m.find()) {
                textEmail.setError(getString(R.string.email_is_not_valid));

                cancel(true);
                return;
            }

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
            String url = AppController.END_POINT + "/signup.php";
            JsonReader jsonReader = new JsonReader(url);

            // prepare parameters
            List<NameValuePair> parameters = new ArrayList<>(7);
            parameters.add(new BasicNameValuePair("name", name));
            parameters.add(new BasicNameValuePair("type", "" + accountType));
            parameters.add(new BasicNameValuePair("username", username));
            parameters.add(new BasicNameValuePair("email", email));
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
                showError(R.string.connection_error_try_again);

                return;
            }

            if (response.equals(Constants.JSON_MSG_EXISTS)) {
                // username exists
                showError(R.string.username_exists);

                return;
            }

            if (response.equals(Constants.JSON_MSG_SUCCESS)) {
                // signed up successfully >> finish to goto login activity
                setResult(RESULT_OK);
                finish();

                return;
            } else {
                showError(R.string.connection_error_try_again);

                return;
            }
        }

        private void showProgress(boolean show) {
            if (show)
                progressDialog.show();
            else
                progressDialog.hide();
        }

        private void showError(int errorMsgRes) {
            Toast.makeText(activity, errorMsgRes, Toast.LENGTH_LONG).show();

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
