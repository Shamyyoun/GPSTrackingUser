package com.mahmoudelshamy.gpstracking.userapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;

import database.VehicleDAO;
import datamodels.Cache;
import datamodels.User;


public class SplashActivity extends ActionBarActivity {
    private static final int SPLASH_TIME = 1 * 1000;

    private VehicleDAO vehicleDAO;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        vehicleDAO = new VehicleDAO(this);

        // init splash handler and runnable
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                Class activityClass;

                // check cached user
                User activeUser = AppController.getInstance(getApplicationContext()).getActiveUser();
                if (activeUser == null) {
                    // no cached user >> goto login activity
                    activityClass = LoginActivity.class;
                } else {
                    // there is a cached user
                    // check his reg_id
                    String regId = Cache.getRegId(getApplicationContext());
                    if (regId == null) {
                        // register user and send his reg_id to server
                        AppController.registerToGCM(getApplicationContext());
                    }

                    // check if has vehicles in database or not
                    vehicleDAO.open();
                    if (vehicleDAO.hasVehicles()) {
                        // goto main activity
                        activityClass = MainActivity.class;
                    } else {
                        // goto add vehicle activity
                        activityClass = VehiclesActivity.class;
                    }
                    vehicleDAO.close();
                }

                // goto suitable activity
                Intent intent = new Intent(SplashActivity.this, activityClass);
                startActivity(intent);
                SplashActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        };

        handler.postDelayed(runnable, SPLASH_TIME);
    }
}
