package in.gipl.tracking.gpcb.database;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

public class IntervalData {

    public static final String TAG = IntervalData.class.getSimpleName();
  /*  public static final String TABLE_NAME = "TrackLog";

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
                    + ")";*/

    @SerializedName("LocationInterval")
    private int locationInterval;
    @SerializedName("SyncInterval")
    private int syncInterval;

    public IntervalData(int locationInterval, int syncInterval) {
        this.locationInterval = locationInterval;
        this.syncInterval = syncInterval;
    }


    public int getLocationInterval() {
        return locationInterval;
    }

    public void setLocationInterval(int locationInterval) {
        this.locationInterval = locationInterval;
    }

    public int getSyncInterval() {
        return syncInterval;
    }

    public void setSyncInterval(int syncInterval) {
        Log.e(TAG, "setSyncInterval: " + syncInterval);
        this.syncInterval = syncInterval;
    }

    @Override
    public String toString() {
        return "LocationData{" +
                "locationInterval=" + locationInterval +
                ", syncInterval=" + syncInterval +

                '}';
    }
}
