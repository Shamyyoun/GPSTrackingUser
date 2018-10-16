package json;

import org.json.JSONException;
import org.json.JSONObject;

public class LocationHandler {
    private String response;

    public LocationHandler(String response) {
        this.response = response;
    }

    public double[] handle() {
        try {
            JSONObject jsonObject = new JSONObject(response);
            double[] location = handleLocation(jsonObject);
            return location;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private double[] handleLocation(JSONObject jsonObject) {
        double[] location;
        try {
            double latitude = jsonObject.getDouble("lat");
            double longitude = jsonObject.getDouble("lng");

            location = new double[]{latitude, longitude};
        } catch (JSONException e) {
            location = null;
            e.printStackTrace();
        }

        return location;
    }
}
