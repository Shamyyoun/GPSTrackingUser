package json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import datamodels.Vehicle;

public class VehiclesHandler {
    private String response;

    public VehiclesHandler(String response) {
        this.response = response;
    }

    public List<Vehicle> handle() {
        try {
            JSONArray jsonArray = new JSONArray(response);
            List<Vehicle> vehicles = new ArrayList<Vehicle>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Vehicle vehicle = handleVehicle(jsonObject);

                vehicles.add(vehicle);
            }
            return vehicles;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private Vehicle handleVehicle(JSONObject jsonObject) {
        Vehicle vehicle;
        try {
            String id = jsonObject.getString("id");
            String password = jsonObject.getString("password");
            String name = jsonObject.getString("name");
            String purpose = jsonObject.getString("purpose");
            int licenceNumber = jsonObject.getInt("licence_number");
            int number = jsonObject.getInt("number");
            String color = jsonObject.getString("color");
            String model = jsonObject.getString("model");
            int year = jsonObject.getInt("year");
            String brand = jsonObject.getString("brand");
            double latitude = jsonObject.getDouble("lat");
            double longitude = jsonObject.getDouble("lng");
            int tripStatus = jsonObject.getInt("trip_status");

            vehicle = new Vehicle(id, password, name, purpose, licenceNumber, number, color, model, year, brand);
            vehicle.setLatitude(latitude);
            vehicle.setLongitude(longitude);
            vehicle.setTripStatus(tripStatus);

        } catch (JSONException e) {
            vehicle = null;
            e.printStackTrace();
        }

        return vehicle;
    }
}
