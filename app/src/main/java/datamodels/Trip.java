package datamodels;

import java.io.Serializable;
import java.util.Date;

import utils.DateUtil;
import utils.LocationUtil;

/**
 * Created by Shamyyoun on 5/27/2015.
 */
public class Trip implements Serializable {
    public static final int STATUS_START = 1;
    public static final int STATUS_END = 2;
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private int id;
    private double startLat;
    private double startLng;
    private String startLocationTitle;
    private Date startDate;
    private double endLat;
    private double endLng;
    private String endLocationTitle;
    private Date endDate;
    private boolean ended;
    private long[] duration; // represented as {seconds, minutes, hours, days}
    private float distance; // represented as meters
    private float speed; // represented as km/h

    public Trip(int id, double startLat, double startLng, String startLocationTitle, String startDate, double endLat, double endLng, String endLocationTitle, String endDate, boolean ended) {
        this.id = id;
        this.startLat = startLat;
        this.startLng = startLng;
        this.startLocationTitle = startLocationTitle;
        this.startDate = DateUtil.convertToDate(startDate, DATE_FORMAT);
        this.endLat = endLat;
        this.endLng = endLng;
        this.endLocationTitle = endLocationTitle;
        this.endDate = DateUtil.convertToDate(endDate, DATE_FORMAT);
        this.ended = ended;

        calcDDS();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getStartLat() {
        return startLat;
    }

    public void setStartLat(double startLat) {
        this.startLat = startLat;
    }

    public double getStartLng() {
        return startLng;
    }

    public void setStartLng(double startLng) {
        this.startLng = startLng;
    }

    public String getStartLocationTitle() {
        return startLocationTitle;
    }

    public void setStartLocationTitle(String startLocationTitle) {
        this.startLocationTitle = startLocationTitle;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
        calcDDS();
    }

    public void setStartDate(String startDate) {
        this.startDate = DateUtil.convertToDate(startDate, DATE_FORMAT);
        calcDDS();
    }

    public double getEndLat() {
        return endLat;
    }

    public void setEndLat(double endLat) {
        this.endLat = endLat;
    }

    public double getEndLng() {
        return endLng;
    }

    public void setEndLng(double endLng) {
        this.endLng = endLng;
    }

    public String getEndLocationTitle() {
        return endLocationTitle;
    }

    public void setEndLocationTitle(String endLocationTitle) {
        this.endLocationTitle = endLocationTitle;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
        calcDDS();
    }

    public void setEndDate(String endDate) {
        this.endDate = DateUtil.convertToDate(endDate, DATE_FORMAT);
        calcDDS();
    }

    public void setEnded(boolean ended) {
        this.ended = ended;
    }

    public boolean isEnded() {
        return ended;
    }

    public long[] getDuration() {
        return duration;
    }

    public float getDistance() {
        return distance;
    }

    public float getSpeed() {
        return speed;
    }

    /**
     * method, used to calc duration , distance and speed
     */
    private void calcDDS() {
        // calc duration
        duration = DateUtil.getDuration(this.startDate, this.endDate);

        // calc distance
        distance = LocationUtil.getDistance(startLat, startLng, endLat, endLng);

        // calc speed
        float durationInHours = ((duration[0] / 60f) / 60f) + (duration[1] / 60f) + (duration[2]) + (duration[3] * 24);
        float distanceInKM = distance / 1000;
        if (durationInHours > 0) {
            speed = distanceInKM / durationInHours;
        } else {
            speed = 0;
        }
    }
}
