package datamodels;

/**
 * Created by Shamyyoun on 2/8/2015.
 */
public class Constants {
    // json response constants
    public static final String JSON_MSG_ERROR = "error";
    public static final String JSON_MSG_SUCCESS = "success";
    public static final String JSON_MSG_NOT_VERIFIED = "not_verified";
    public static final String JSON_MSG_EXISTS = "exists";

    // sp constants
    public static final String SP_RESPONSES = "responses";
    public static final String SP_KEY_ACTIVE_USER_RESPONSE = "active_user_response";
    public static final String SP_CONFIG = "config";
    public static final String SP_KEY_TRACKING_INTERVAL = "tracking_interval";
    public static final String SP_KEY_REG_ID = "reg_id";

    // request codes
    public static final int REQ_CODE_SIGNUP = 1;
    public static final int REQ_CODE_VEHICLES = 2;

    // keys
    public static final String KEY_CAME_FROM_MAIN = "came_from_main";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_VEHICLE_ID = "vehicle_id";
    public static final String KEY_TRIP = "trip";
    public static final String KEY_VEHICLE = "vehicle";

    // push notifications keys
    public static final String PUSHN_HELP = "help";
    public static final String PUSHN_TRIP = "trip";

    // main activity messages
    public static final int MESSAGE_TRIP_STARTED = 1;
    public static final int MESSAGE_TRIP_ENDED = 2;
    public static final int MESSAGE_TRIP_ERROR = 3;
}