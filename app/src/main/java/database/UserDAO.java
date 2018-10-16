package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import datamodels.User;

public class UserDAO {

    private SQLiteDatabase database;
    private DatabaseSQLiteHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = new DatabaseSQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * method, used to add users to database
     */
    public void add(User user) {
        ContentValues values = new ContentValues();
        values.put(DatabaseSQLiteHelper.USERS_USERNAME, user.getUsername());
        values.put(DatabaseSQLiteHelper.USERS_PASSWORD, user.getPassword());
        values.put(DatabaseSQLiteHelper.USERS_EMAIL, user.getEmail());
        values.put(DatabaseSQLiteHelper.USERS_NAME, user.getName());
        values.put(DatabaseSQLiteHelper.USERS_TYPE, user.getType());
        values.put(DatabaseSQLiteHelper.USERS_ADDRESS, user.getAddress());
        values.put(DatabaseSQLiteHelper.USERS_ORG_TYPE, user.getOrgType());
        values.put(DatabaseSQLiteHelper.USERS_TRACKING_INTERVAL, user.getTrackingInterval());

        database.insert(DatabaseSQLiteHelper.TABLE_USERS, null, values);
    }

    /**
     * method, used to update users in database
     */
    public void update(User user) {
        ContentValues values = new ContentValues();
        values.put(DatabaseSQLiteHelper.USERS_PASSWORD, user.getPassword());
        values.put(DatabaseSQLiteHelper.USERS_EMAIL, user.getEmail());
        values.put(DatabaseSQLiteHelper.USERS_NAME, user.getName());
        values.put(DatabaseSQLiteHelper.USERS_TYPE, user.getType());
        values.put(DatabaseSQLiteHelper.USERS_ADDRESS, user.getAddress());
        values.put(DatabaseSQLiteHelper.USERS_ORG_TYPE, user.getOrgType());
        values.put(DatabaseSQLiteHelper.USERS_TRACKING_INTERVAL, user.getTrackingInterval());

        database.update(DatabaseSQLiteHelper.TABLE_USERS, values, DatabaseSQLiteHelper.USERS_USERNAME + " = '" + user.getUsername() + "'", null);
    }

    /**
     * method, used to delete all users from database
     */
    public void deleteAll() {
        database.delete(DatabaseSQLiteHelper.TABLE_USERS, null, null);
    }

    /**
     * method, used to get user from db
     */
    public User get() {
        User user = null;
        Cursor cursor = database.query(DatabaseSQLiteHelper.TABLE_USERS, null, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            user = cursorToUser(cursor);
            cursor.moveToNext();
        }
        cursor.close();
        return user;
    }

    /**
     * method, used to get item values from cursor row
     */
    private User cursorToUser(Cursor cursor) {
        String username = cursor.getString(cursor.getColumnIndex(DatabaseSQLiteHelper.USERS_USERNAME));
        String password = cursor.getString(cursor.getColumnIndex(DatabaseSQLiteHelper.USERS_PASSWORD));
        String email = cursor.getString(cursor.getColumnIndex(DatabaseSQLiteHelper.USERS_EMAIL));
        String name = cursor.getString(cursor.getColumnIndex(DatabaseSQLiteHelper.USERS_NAME));
        int type = cursor.getInt(cursor.getColumnIndex(DatabaseSQLiteHelper.USERS_TYPE));
        String address = cursor.getString(cursor.getColumnIndex(DatabaseSQLiteHelper.USERS_ADDRESS));
        String orgType = cursor.getString(cursor.getColumnIndex(DatabaseSQLiteHelper.USERS_ORG_TYPE));
        int trackingInterval = cursor.getInt(cursor.getColumnIndex(DatabaseSQLiteHelper.USERS_TRACKING_INTERVAL));

        User user = new User(username, password, email, name, type, address, orgType, trackingInterval);

        return user;
    }
}