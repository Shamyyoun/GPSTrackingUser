package database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseSQLiteHelper extends SQLiteOpenHelper {
    private Context context;

    // database info
    private static final String DATABASE_NAME = "gps_tracking_users.db";
    private static final int DATABASE_VERSION = 3;

    // table users
    public static final String TABLE_USERS = "users";
    public static final String USERS_USERNAME = "username";
    public static final String USERS_PASSWORD = "password";
    public static final String USERS_EMAIL = "email";
    public static final String USERS_NAME = "name";
    public static final String USERS_TYPE = "type";
    public static final String USERS_ADDRESS = "address";
    public static final String USERS_ORG_TYPE = "org_type";
    public static final String USERS_TRACKING_INTERVAL = "tracking_interval";

    // table vehicles
    public static final String TABLE_VEHICLES = "vehicles";
    public static final String VEHICLES_ID = "_id";
    public static final String VEHICLES_PASSWORD = "_password";
    public static final String VEHICLES_NAME = "_name";
    public static final String VEHICLES_PURPOSE = "purpose";
    public static final String VEHICLES_LICENCE_NUMBER = "licence_number";
    public static final String VEHICLES_NUMBER = "_number";
    public static final String VEHICLES_COLOR = "color";
    public static final String VEHICLES_YEAR = "year";
    public static final String VEHICLES_MODEL = "model";
    public static final String VEHICLES_BRAND = "brand";
    public static final String VEHICLES_LAT = "lat";
    public static final String VEHICLES_LNG = "lng";
    public static final String VEHICLES_TRIP_STATUS = "trip_status";

    // tables creation
    private static final String USERS_CREATE = "CREATE TABLE " + TABLE_USERS
            + "("
            + USERS_USERNAME + " TEXT PRIMARY KEY, "
            + USERS_PASSWORD + " TEXT NOT NULL, "
            + USERS_EMAIL + " TEXT NOT NULL, "
            + USERS_NAME + " TEXT NOT NULL, "
            + USERS_TYPE + " INTEGER NOT NULL, "
            + USERS_ADDRESS + " TEXT, "
            + USERS_ORG_TYPE + " TEXT, "
            + USERS_TRACKING_INTERVAL + " INTEGER NOT NULL"
            + ");";

    private static final String VEHICLES_CREATE = "CREATE TABLE " + TABLE_VEHICLES
            + "("
            + VEHICLES_ID + " TEXT PRIMARY KEY, "
            + VEHICLES_PASSWORD + " TEXT NOT NULL, "
            + VEHICLES_NAME + " TEXT NOT NULL, "
            + VEHICLES_PURPOSE + " TEXT, "
            + VEHICLES_LICENCE_NUMBER + " INTEGER, "
            + VEHICLES_NUMBER + " INTEGER, "
            + VEHICLES_COLOR + " TEXT, "
            + VEHICLES_YEAR + " INTEGER, "
            + VEHICLES_MODEL + " TEXT, "
            + VEHICLES_BRAND + " TEXT, "
            + VEHICLES_LAT + " REAL, "
            + VEHICLES_LNG + " REAL, "
            + VEHICLES_TRIP_STATUS + " INTEGER"
            + ");";

    public DatabaseSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        // create tables
        database.execSQL(USERS_CREATE);
        database.execSQL(VEHICLES_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VEHICLES);
        onCreate(db);
    }

}