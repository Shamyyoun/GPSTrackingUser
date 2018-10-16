package services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import com.mahmoudelshamy.gpstracking.userapp.AppController;
import com.mahmoudelshamy.gpstracking.userapp.R;

import database.VehicleDAO;
import datamodels.Constants;
import datamodels.Trip;
import receivers.GcmBroadcastReceiver;

public class GcmMessageHandler extends IntentService {
    private Handler handler;

    public GcmMessageHandler() {
        super("GcmMessageHandler");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Bundle extras = intent.getExtras();

        // get values
        final String key = extras.getString("key");
        final String message = extras.getString("message");

        handler.post(new Runnable() {
                         @Override
                         public void run() {
                             // check if there is an active user
                             if (AppController.getInstance(getApplicationContext()).getActiveUser() != null) {
                                 // check key
                                 if (Constants.PUSHN_HELP.equals(key)) {
                                     // convert vehicle's id
                                     int vehicleId = 0;
                                     try {
                                         vehicleId = Integer.parseInt(message);
                                     } catch (Exception e) {
                                     }

                                     // get vehicle's name
                                     String vehicleName = extras.getString("vehicle_name");

                                     // show notification
                                     String content = vehicleName + " " + getString(R.string.is_requesting_help);
                                     Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.help);
                                     showNotification(vehicleId, content, soundUri);
                                 } else if (Constants.PUSHN_TRIP.equals(key)) {
                                     // get extra values
                                     String mode = extras.getString("mode");
                                     String vehicleId = extras.getString("vehicle_id");

                                     VehicleDAO vehicleDAO = new VehicleDAO(getApplicationContext());
                                     vehicleDAO.open();

                                     // check message
                                     int intentMessage;
                                     if (message.equals("success")) {
                                         // check mode
                                         if ("start".equals(mode)) {
                                             // --trip started successfully--
                                             intentMessage = Constants.MESSAGE_TRIP_STARTED;
                                             vehicleDAO.updateTripStatus(Trip.STATUS_START, vehicleId);
                                         } else {
                                             // --trip ended successfully--
                                             intentMessage = Constants.MESSAGE_TRIP_ENDED;
                                             vehicleDAO.updateTripStatus(Trip.STATUS_END, vehicleId);
                                         }
                                     } else {
                                         // --error--
                                         intentMessage = Constants.MESSAGE_TRIP_ERROR;
                                     }

                                     vehicleDAO.close();

                                     // send broadcast message to main activity
                                     Intent intent = new Intent(getPackageName() + ".TRIP_RESPONDED");
                                     intent.putExtra(Constants.KEY_MESSAGE, intentMessage);
                                     intent.putExtra(Constants.KEY_VEHICLE_ID, vehicleId);
                                     sendBroadcast(intent);
                                 }
                             }
                         }
                     }

        );

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    /**
     * method, used to show notification with passed parameters
     */
    private void showNotification(int id, String content, Uri soundUri) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int iconResId = R.drawable.ic_launcher;
        String title = getString(R.string.app_title);
        long when = System.currentTimeMillis();

        Intent notificationIntent = new Intent();
        PendingIntent contentIntent = PendingIntent.getActivity(this, id, notificationIntent, 0);
        Notification notification = new Notification(iconResId, title, when);
        notification.sound = soundUri;
        notification.setLatestEventInfo(this, title, content, contentIntent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(id, notification);
    }
}
