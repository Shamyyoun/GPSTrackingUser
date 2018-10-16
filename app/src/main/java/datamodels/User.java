package datamodels;

/**
 * Created by Shamyyoun on 4/30/2015.
 */
public class User {
    public static int TYPE_PERSONAL = 1;
    public static int TYPE_ORGANIZATION = 2;

    private String username;
    private String password;
    private String email;
    private String name;
    private int type;
    private String address;
    private String orgType;
    private int trackingInterval;

    public User(String username) {
        this.username = username;
    }

    public User(String username, String password, String email, String name, int type, String address, String orgType, int trackingInterval) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.type = type;
        this.address = address;
        this.orgType = orgType;
        this.trackingInterval = trackingInterval;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOrgType() {
        return orgType;
    }

    public void setOrgType(String orgType) {
        this.orgType = orgType;
    }

    public int getTrackingInterval() {
        return trackingInterval;
    }

    public void setTrackingInterval(int trackingInterval) {
        this.trackingInterval = trackingInterval;
    }
}
