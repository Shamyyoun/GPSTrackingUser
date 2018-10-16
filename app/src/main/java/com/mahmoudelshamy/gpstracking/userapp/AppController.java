package com.mahmoudelshamy.gpstracking.userapp;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import database.UserDAO;
import datamodels.Cache;
import datamodels.User;
import json.JsonReader;

/**
 * Created by Shamyyoun on 3/15/2015.
 */
public class AppController extends Application {
    public static final String END_POINT = "http://gpstracking.mahmoudelshamy.com";
    public static final String PROJECT_NUMBER = "547910235601";

    private User activeUser;

    public AppController() {
        super();
    }

    /**
     * overriden method
     */
    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * method, used to get active user from runtime or from SP
     */
    public User getActiveUser() {
        if (activeUser == null) {
            // get saved user if exists
            UserDAO userDAO = new UserDAO(getApplicationContext());
            userDAO.open();
            activeUser = userDAO.get();
            userDAO.close();
        }

        return activeUser;
    }

    /**
     * method, used to set active user
     */
    public void setActiveUser(User user) {
        this.activeUser = user;
    }

    /**
     * method used to return current application instance
     */
    public static AppController getInstance(Context context) {
        return (AppController) context.getApplicationContext();
    }

    /**
     * method, used to register user to GCM and send his reg_id to server
     */
    public static void registerToGCM(final Context context) {
        // execute operation in async task
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    // get reg_id
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
                    String regId = gcm.register(PROJECT_NUMBER);

                    // check reg_id
                    if (regId != null) {
                        // send reg_id to server
                        JsonReader jsonReader = new JsonReader(AppController.END_POINT + "/update_user_regid.php");
                        List<NameValuePair> parameters = new ArrayList<>(2);
                        parameters.add(new BasicNameValuePair("username", getInstance(context).getActiveUser().getUsername()));
                        parameters.add(new BasicNameValuePair("reg_id", regId));
                        jsonReader.sendAsyncPostRequest(parameters);

                        // cache it
                        Cache.updateRegId(context, regId);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.execute();
    }
}
