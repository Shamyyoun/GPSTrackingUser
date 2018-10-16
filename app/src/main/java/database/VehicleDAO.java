package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import datamodels.Vehicle;

public class VehicleDAO {

    private SQLiteDatabase database;
    private DatabaseSQLiteHelper dbHelper;

    public VehicleDAO(Context context) {
        dbHelper = new DatabaseSQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * method, used to add vehicle to database
     */
    public void add(Vehicle vehicle) {
        ContentValues values = new ContentValues();
        values.put(DatabaseSQLiteHelper.VEHICLES_ID, vehicle.getId());
        values.put(DatabaseSQLiteHelper.VEHICLES_PASSWORD, vehicle.getPassword());
        values.put(DatabaseSQLiteHelper.VEHICLES_NAME, vehicle.getName());
        values.put(DatabaseSQLiteHelper.VEHICLES_PURPOSE, vehicle.getPurpose());
        values.put(DatabaseSQLiteHelper.VEHICLES_LICENCE_NUMBER, vehicle.getLicenceNumber());
        values.put(DatabaseSQLiteHelper.VEHICLES_NUMBER, vehicle.getNumber());
        values.put(DatabaseSQLiteHelper.VEHICLES_COLOR, vehicle.getColor());
        values.put(DatabaseSQLiteHelper.VEHICLES_YEAR, vehicle.getYear());
        values.put(DatabaseSQLiteHelper.VEHICLES_MODEL, vehicle.getModel());
        values.put(DatabaseSQLiteHelper.VEHICLES_BRAND, vehicle.getBrand());
        values.put(DatabaseSQLiteHelper.VEHICLES_LAT, vehicle.getLatitude());
        values.put(DatabaseSQLiteHelper.VEHICLES_LNG, vehicle.getLongitude());
        values.put(DatabaseSQLiteHelper.VEHICLES_TRIP_STATUS, vehicle.getTripStatus());

        database.insert(DatabaseSQLiteHelper.TABLE_VEHICLES, null, values);
    }

    /**
     * method, used to add list of vehicles to database
     */
    public void add(List<Vehicle> vehicles) {
        for (Vehicle vehicle : vehicles) {
            add(vehicle);
        }
    }

    /**
     * method, used to update vehicle in database
     */
    public void update(Vehicle vehicle) {
        ContentValues values = new ContentValues();
        values.put(DatabaseSQLiteHelper.VEHICLES_PASSWORD, vehicle.getPassword());
        values.put(DatabaseSQLiteHelper.VEHICLES_NAME, vehicle.getName());
        values.put(DatabaseSQLiteHelper.VEHICLES_PURPOSE, vehicle.getPurpose());
        values.put(DatabaseSQLiteHelper.VEHICLES_LICENCE_NUMBER, vehicle.getLicenceNumber());
        values.put(DatabaseSQLiteHelper.VEHICLES_NUMBER, vehicle.getNumber());
        values.put(DatabaseSQLiteHelper.VEHICLES_COLOR, vehicle.getColor());
        values.put(DatabaseSQLiteHelper.VEHICLES_YEAR, vehicle.getYear());
        values.put(DatabaseSQLiteHelper.VEHICLES_MODEL, vehicle.getModel());
        values.put(DatabaseSQLiteHelper.VEHICLES_BRAND, vehicle.getBrand());
        values.put(DatabaseSQLiteHelper.VEHICLES_LAT, vehicle.getLatitude());
        values.put(DatabaseSQLiteHelper.VEHICLES_LNG, vehicle.getLongitude());
        values.put(DatabaseSQLiteHelper.VEHICLES_LNG, vehicle.getLongitude());

        database.update(DatabaseSQLiteHelper.TABLE_VEHICLES, values, DatabaseSQLiteHelper.VEHICLES_ID + " = '" + vehicle.getId() + "'", null);
    }

    /**
     * method, used to update vehicle's trip status in database
     */
    public void updateTripStatus(int status, String vehicleId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseSQLiteHelper.VEHICLES_TRIP_STATUS, status);

        int r = database.update(DatabaseSQLiteHelper.TABLE_VEHICLES, values, DatabaseSQLiteHelper.VEHICLES_ID + " = '" + vehicleId + "'", null);
    }

    /**
     * method, used to delete vehicle from database filtered by vehicle id
     */
    public void delete(String vehicleId) {
        database.delete(DatabaseSQLiteHelper.TABLE_VEHICLES, DatabaseSQLiteHelper.VEHICLES_ID + " = '" + vehicleId + "'", null);
    }

    /**
     * method, used to delete all vehicles from database
     */
    public void deleteAll() {
        database.delete(DatabaseSQLiteHelper.TABLE_VEHICLES, null, null);
    }

    /**
     * method, used to get all vehicles from db
     */
    public List<Vehicle> getAll() {
        List<Vehicle> vehicles = new ArrayList<Vehicle>();

        Cursor cursor = database.query(DatabaseSQLiteHelper.TABLE_VEHICLES, null, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Vehicle vehicle = cursorToVehicle(cursor);
            vehicles.add(vehicle);

            cursor.moveToNext();
        }
        cursor.close();
        return vehicles;
    }

    /**
     * method, used to get item values from cursor row
     */
    private Vehicle cursorToVehicle(Cursor cursor) {
        String id = cursor.getString(cursor.getColumnIndex(DatabaseSQLiteHelper.VEHICLES_ID));
        String password = cursor.getString(cursor.getColumnIndex(DatabaseSQLiteHelper.VEHICLES_PASSWORD));
        String name = cursor.getString(cursor.getColumnIndex(DatabaseSQLiteHelper.VEHICLES_NAME));
        String purpose = cursor.getString(cursor.getColumnIndex(DatabaseSQLiteHelper.VEHICLES_PURPOSE));
        int licenceNumber = cursor.getInt(cursor.getColumnIndex(DatabaseSQLiteHelper.VEHICLES_LICENCE_NUMBER));
        int number = cursor.getInt(cursor.getColumnIndex(DatabaseSQLiteHelper.VEHICLES_NUMBER));
        String color = cursor.getString(cursor.getColumnIndex(DatabaseSQLiteHelper.VEHICLES_COLOR));
        int year = cursor.getInt(cursor.getColumnIndex(DatabaseSQLiteHelper.VEHICLES_YEAR));
        String model = cursor.getString(cursor.getColumnIndex(DatabaseSQLiteHelper.VEHICLES_MODEL));
        String brand = cursor.getString(cursor.getColumnIndex(DatabaseSQLiteHelper.VEHICLES_BRAND));
        double latitude = cursor.getDouble(cursor.getColumnIndex(DatabaseSQLiteHelper.VEHICLES_LAT));
        double longitude = cursor.getDouble(cursor.getColumnIndex(DatabaseSQLiteHelper.VEHICLES_LNG));
        int tripStatus = cursor.getInt(cursor.getColumnIndex(DatabaseSQLiteHelper.VEHICLES_TRIP_STATUS));

        Vehicle vehicle = new Vehicle(id, password, name, purpose, licenceNumber, number, color, model, year, brand);
        vehicle.setLatitude(latitude);
        vehicle.setLongitude(longitude);
        vehicle.setTripStatus(tripStatus);

        return vehicle;
    }

    /**
     * method, used to check if database vehicles or not based on count
     */
    public boolean hasVehicles() {
        Cursor mCount = database.rawQuery("SELECT COUNT(" + DatabaseSQLiteHelper.VEHICLES_ID +
                ") FROM " + DatabaseSQLiteHelper.TABLE_VEHICLES, null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();

        if (count == 0)
            return false;
        else
            return true;
    }
}