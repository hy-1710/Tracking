package in.gipl.tracking.gpcb.prefs;

import android.content.Context;
import android.support.annotation.NonNull;

public class AppPrefs extends BasePrefs {

    /**
     * SharedPreference key for storing username & selfHostedURL
     */
    public static final String EmployeeId = "EmployeeId";
    public static final String UserId = "UserId";
    public static final String Right_None = "Right_None";
    public static final String Scroll_X = "Scroll_X";
    public static final String Scroll_Y = "Scroll_y";
    public static final String locationId;
    public static final String latitude;
    public static final String longitude;
    public static final String timeStamp;
    private static final String TAG;
    private static final String UserName;
    private static final String Password;
    private static final String Databse;
    private static final String SyncInterval;
    private static final String LocationInterval;
    private static final String IMEINO;

    static {
        TAG = AppPrefs.class.getSimpleName();
        UserName = "UserName";
        Password = "Password";
        Databse = "Databse";
        locationId = "locationId";
        latitude = "latitude";
        longitude = "longitude";
        timeStamp = "timeStamp";
        SyncInterval = "SyncInterval";
        LocationInterval = "LocationInterval";
        IMEINO = "imeiNo";


    }

    /**
     * @param context context
     */
    public AppPrefs(@NonNull Context context) {
        super(TAG, context);
    }

    public String getUserName() {
        return getString(UserName);
    }

    public void setUserName(String userName) {
        putString(UserName, userName);
    }

    public String getPassword() {
        return getString(Password);
    }

    public void setPassword(String password) {
        putString(Password, password);

    }

    public String getDatabse() {
        return getString(Databse);
    }

    public void setDatabse(String databse) {
        putString(Databse, databse);
    }

    public String getTimeStamp() {
        return getString(timeStamp);
    }

    public void setTimeStamp(String TimeStamp) {
        putString(timeStamp, TimeStamp);
    }

    public int getEmployeeId() {
        return getInt(EmployeeId);
    }

    public void setEmployeeId(int employeeId) {
        putInt(EmployeeId, employeeId);

    }

    public int getLocationId() {
        return getInt(locationId);
    }


    public void setLocationId(int LocationId) {
        putInt(locationId, LocationId);

    }

    public int getUserId() {
        return getInt(UserId);
    }

    public void setUserId(int UserId) {
        putInt(locationId, UserId);

    }


    public String getLatitude() {
        // double d = Double.parseDouble(latitude);
        return getString(latitude);
    }

    public void setLatitude(double Latitude) {
        putString(latitude, String.valueOf(Latitude));
    }

    public String getLongitude() {

        //double d = Double.parseDouble(latitude);
        return getString(longitude);
    }

    public void setLongitude(double Longitude) {
        putString(longitude, String.valueOf(Longitude));
    }


    public int getSyncInterval() {
        return getInt(SyncInterval);
    }

    public void setSyncInterval(int syncInterval) {
        putInt(SyncInterval, syncInterval);

    }

    public int getLocationInterval() {
        return getInt(LocationInterval);
    }

    public void setLocationInterval(int locationInterval) {
        putInt(LocationInterval, locationInterval);

    }

    public String getIMEINO() {
        return getString(IMEINO);
    }


    public void setIMEINO(String ImeiNo) {
        putString(IMEINO, String.valueOf(ImeiNo));
    }
}
