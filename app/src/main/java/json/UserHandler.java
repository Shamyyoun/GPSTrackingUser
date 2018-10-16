package json;

import org.json.JSONException;
import org.json.JSONObject;

import datamodels.User;

public class UserHandler {
    private String response;

    public UserHandler(String response) {
        this.response = response;
    }

    public User handle() {
        try {
            JSONObject jsonObject = new JSONObject(response);
            User user = handleUser(jsonObject);
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private User handleUser(JSONObject jsonObject) {
        User user;
        try {
            String username = jsonObject.getString("username");
            String password = jsonObject.getString("password");
            String email = jsonObject.getString("email");
            String name = jsonObject.getString("name");
            int type = jsonObject.getInt("type");
            String address = jsonObject.getString("address");
            String orgType = jsonObject.getString("org_type");
            int trackingInterval = jsonObject.getInt("tracking_interval");

            user = new User(username, password, email, name, type, address, orgType, trackingInterval);
        } catch (JSONException e) {
            user = null;
            e.printStackTrace();
        }

        return user;
    }
}
