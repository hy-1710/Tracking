package in.gipl.tracking.gpcb.database;

import android.support.annotation.NonNull;

public class LocationData {

    public static final String TABLE_NAME = "TrackLog";

    public static final String TRACK_LOG_ID = "TrackLogId";
    public static final String IMEI_NO = "IMEINo";
    public static final String LATITUDE = "Latitude";
    public static final String LONGITUDE = "Longitude";
    public static final String CREATED_ON = "CreatedOn";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + TRACK_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + IMEI_NO + " TEXT,"
                    + LATITUDE + " REAL,"
                    + LONGITUDE + " REAL,"
                    + CREATED_ON + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + ")";

    private long locationId;
    private long userId;
    private double latitude;
    private double longitude;
    private String timestamp;

    public LocationData() {
        this.locationId = 0L;
        this.userId = 0L;
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.timestamp = "";
    }

    public LocationData(long locationId, long userId, double latitude, double longitude, @NonNull String timestamp) {
        this.locationId = locationId;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public long getLocationId() {
        return locationId;
    }

    public void setLocationId(long locationId) {
        this.locationId = locationId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTimestamp() {
        if (timestamp == null) {
            timestamp = "";
        }
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "LocationData{" +
                "locationId=" + locationId +
                ", IMEINo=" + userId +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
