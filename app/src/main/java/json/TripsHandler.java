package json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import datamodels.Trip;

public class TripsHandler {
    private String response;

    public TripsHandler(String response) {
        this.response = response;
    }

    public List<Trip> handle() {
        try {
            JSONArray jsonArray = new JSONArray(response);
            List<Trip> trips = new ArrayList<Trip>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Trip trip = handleTrip(jsonObject);

                trips.add(trip);
            }
            return trips;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private Trip handleTrip(JSONObject jsonObject) {
        Trip trip;
        try {
            int id = jsonObject.getInt("id");
            double startLat = jsonObject.getDouble("start_lat");
            double startLng = jsonObject.getDouble("start_lng");
            String startLocationTitle = jsonObject.getString("start_location_title");
            String startTime = jsonObject.getString("start_time");
            double endLat = jsonObject.getDouble("end_lat");
            double endLng = jsonObject.getDouble("end_lng");
            String endLocationTitle = jsonObject.getString("end_location_title");
            String endTime = jsonObject.getString("end_time");
            boolean ended = jsonObject.getBoolean("ended");

            trip = new Trip(id, startLat, startLng, startLocationTitle, startTime, endLat, endLng, endLocationTitle, endTime, ended);

        } catch (JSONException e) {
            trip = null;
            e.printStackTrace();
        }

        return trip;
    }
}
