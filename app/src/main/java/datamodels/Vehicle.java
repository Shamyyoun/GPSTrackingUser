package datamodels;

import java.io.Serializable;

/**
 * Created by Shamyyoun on 4/27/2015.
 */
public class Vehicle implements Serializable{
    private String id;
    private String password;
    private String name;
    private String purpose;
    private int licenceNumber;
    private int number;
    private String color;
    private String model;
    private int year;
    private String brand;
    private double latitude;
    private double longitude;
    private int tripStatus;

    public Vehicle(String id) {
        this.id = id;
    }

    public Vehicle(String id, String password, String name, String purpose, int licenceNumber,
                   int number, String color, String model, int year, String brand) {
        this.id = id;
        this.password = password;
        this.name = name;
        this.purpose = purpose;
        this.licenceNumber = licenceNumber;
        this.number = number;
        this.color = color;
        this.model = model;
        this.year = year;
        this.brand = brand;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public int getLicenceNumber() {
        return licenceNumber;
    }

    public void setLicenceNumber(int licenceNumber) {
        this.licenceNumber = licenceNumber;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLocation(double[] location) {
        latitude = location[0];
        longitude = location[1];
    }

    public double[] getLocation() {
        return new double[]{latitude, longitude};
    }

    public int getTripStatus() {
        return tripStatus;
    }

    public void setTripStatus(int tripStatus) {
        this.tripStatus = tripStatus;
    }

    @Override
    public String toString() {
        return name;
    }
}
