package in.gipl.tracking.gpcb.database;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.net.NetworkInterface;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import in.gipl.tracking.gpcb.App;
import in.gipl.tracking.gpcb.prefs.AppPrefs;
import timber.log.Timber;

public class DataBaseHelper extends SQLiteOpenHelper {

    public static final String TAG = DataBaseHelper.class.getSimpleName();
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "location_db";
    // initialised from App::onCreate()
    public static App app;
    public static AppPrefs prefs;
    Context context;


    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        prefs = new AppPrefs(context);
        this.context = context;


    }

    public static String getUniqueIMEIId(Context context) {
        Log.e(TAG, "getUniqueIMEIId: Called----");
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            /*if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return "";
            }*/
            @SuppressLint("MissingPermission") String imei = telephonyManager.getDeviceId();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                @SuppressLint("MissingPermission")
                String str = telephonyManager.getImei();
                Log.e(TAG, "getUniqueIMEIId: IMEI ID------------ " + str);
            }

          /*  Log.e(TAG, "getUniqueIMEIId: value in sharepref----"+appPrefs.getIMEINO() );

            Log.e("imei  IMEI ID________", "=" + imei);*/

            Timber.i("IMEI NO  is %s", imei);
            if (imei != null && !imei.isEmpty()) {
                //get successfull macAdd

                //865182031956444
                //000000000000000

                prefs.setIMEINO(imei);

                return imei;
            } else {

                String serialNo = android.os.Build.SERIAL;
                prefs.setIMEINO(serialNo);

                return serialNo;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "not_found";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LocationData.CREATE_TABLE);
        db.execSQL(SyncData.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + LocationData.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SyncData.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    private String getCurrentTimeUsingDate() {
        Date date = new Date();
        String strDateFormat = "yyyy-MM-dd HH:mm:ss";
        DateFormat dateFormat = new SimpleDateFormat(strDateFormat, Locale.US);
        Timber.e("getCurrentTimeUsingDate: dateFormat %s", dateFormat);
        String formattedDate = dateFormat.format(date);
        Timber.e("Current time of the day using Date - 12 hour format: %s", formattedDate);
        return formattedDate;
    }

    public long insertNote(double latitude, double longitude) {
        long id = 0;

        String ImeiNo = prefs.getIMEINO();
        if (ImeiNo != null && !ImeiNo.isEmpty()) {


        } else {
            ImeiNo = getUniqueIMEIId(context);
        }
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            ContentValues values = new ContentValues();
            String mLastUpdateTime = getCurrentTimeUsingDate();
            Timber.e("insertNote: %s", mLastUpdateTime);

            // `id` and `timestamp` will be inserted automatically.
            // no need to add them
            // values.put(LocationData.USER_ID, 1);
            values.put(SyncData.IMEI_NO, ImeiNo);
            values.put(LocationData.LATITUDE, latitude);
            values.put(LocationData.LONGITUDE, longitude);
            values.put(LocationData.CREATED_ON, mLastUpdateTime);

            Timber.e("insertNote: TABLE NAME: %s", LocationData.TABLE_NAME);

            // insert row
            id = db.insert(LocationData.TABLE_NAME, null, values);
            if (id > 0) {
                Timber.e("insertNote: record inserted successfully: %s", id);
            } else {
                Timber.e("insertNote: record inserted failed.");
            }

            // close db connection
            db.close();
        }

        // return newly inserted row id
        return id;
    }

    public long insertSyncData(long lastLocationId, long userId, int totalSyncRecordSize, String createdOn) {
        long id = 0;

        String ImeiNo = prefs.getIMEINO();
        if (ImeiNo != null && !ImeiNo.isEmpty()) {


        } else {
            ImeiNo = getUniqueIMEIId(context);
        }

        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            ContentValues values = new ContentValues();

            // `id` and `timestamp` will be inserted automatically.
            // no need to add them
            values.put(SyncData.LAST_TRACK_LOG_ID, lastLocationId);

            values.put(SyncData.IMEI_NO, ImeiNo);
            values.put(SyncData.TOTAL_SYNC_RECORD, totalSyncRecordSize);
            values.put(LocationData.CREATED_ON, createdOn);

            Timber.e("insertSyncData: TABLE NAME: %s", SyncData.TABLE_NAME);

            // insert row
            id = db.insert(SyncData.TABLE_NAME, null, values);
            if (id > 0) {
                Timber.e("insertSyncData: record inserted successfully: %s", id);
            } else {
                Timber.e("insertSyncData: record inserted failed.");
            }

            // close db connection
            db.close();
        }
        // return newly inserted row id
        return id;
    }

    public LocationData getLocationData(long id) {
        LocationData locationData = new LocationData();
        SQLiteDatabase db = getReadableDatabase();
        if (db != null) {
            Cursor cursor = db.query(LocationData.TABLE_NAME,
                    new String[]{LocationData.TRACK_LOG_ID, LocationData.IMEI_NO,
                            LocationData.LATITUDE, LocationData.LONGITUDE, LocationData.CREATED_ON},
                    LocationData.TRACK_LOG_ID + "=?",
                    new String[]{String.valueOf(id)}, null, null, null, null);

            if (cursor != null) {
                if (cursor.moveToNext()) {
                    // prepare note object
                    locationData.setLocationId(cursor.getLong(cursor.getColumnIndex(LocationData.TRACK_LOG_ID)));
                    locationData.setUserId(cursor.getLong(cursor.getColumnIndex(LocationData.IMEI_NO)));
                    locationData.setLatitude(cursor.getDouble(cursor.getColumnIndex(LocationData.LATITUDE)));
                    locationData.setLongitude(cursor.getDouble(cursor.getColumnIndex(LocationData.LONGITUDE)));
                    locationData.setTimestamp(cursor.getString(cursor.getColumnIndex(LocationData.CREATED_ON)));
                }
                // close the db connection
                cursor.close();
            }
            db.close();
        }
        return locationData;
    }

    public SyncData getSyncData(long syncId) {
        SyncData syncData = new SyncData();
        SQLiteDatabase db = getReadableDatabase();
        if (db != null) {
            Cursor cursor = db.query(SyncData.TABLE_NAME,
                    new String[]{SyncData.LAST_TRACK_LOG_ID, SyncData.IMEI_NO, SyncData.TOTAL_SYNC_RECORD, SyncData.CREATED_ON},
                    SyncData.SYNC_ID + "=?",
                    new String[]{String.valueOf(syncId)}, null, null, null, null);

            if (cursor != null) {
                if (cursor.moveToNext()) {

                    // prepare note object
                    syncData.setSyncID(cursor.getLong(cursor.getColumnIndex(SyncData.SYNC_ID)));
                    syncData.setLastTrackLogId(cursor.getLong(cursor.getColumnIndex(SyncData.LAST_TRACK_LOG_ID)));
                    syncData.setUserId(cursor.getLong(cursor.getColumnIndex(SyncData.IMEI_NO)));
                    syncData.setTotalSync(cursor.getInt(cursor.getColumnIndex(SyncData.TOTAL_SYNC_RECORD)));
                    syncData.setTimestamp(cursor.getString(cursor.getColumnIndex(SyncData.CREATED_ON)));
                }
                // close the cursor connection
                cursor.close();
            }
            db.close();
        }
        return syncData;
    }

    public List<LocationData> getAllLocation() {
        List<LocationData> notes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        if (db != null) {
            // Select All Query
            String selectQuery = "SELECT * FROM " + LocationData.TABLE_NAME + " ORDER BY " +
                    LocationData.CREATED_ON + " DESC";

            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    do {
                        LocationData locationData = new LocationData();
                        locationData.setLocationId(cursor.getLong(cursor.getColumnIndex(LocationData.TRACK_LOG_ID)));
                        locationData.setUserId(cursor.getLong(cursor.getColumnIndex(LocationData.IMEI_NO)));
                        locationData.setLatitude(cursor.getDouble(cursor.getColumnIndex(LocationData.LATITUDE)));
                        locationData.setLongitude(cursor.getDouble(cursor.getColumnIndex(LocationData.LONGITUDE)));
                        locationData.setTimestamp(cursor.getString(cursor.getColumnIndex(LocationData.CREATED_ON)));

                        notes.add(locationData);
                    } while (cursor.moveToNext());
                }

                // close cursor connection
                cursor.close();
            }
            db.close();
        }
        // return notes list
        return notes;
    }

    public long getMaxNotesCount() {
        long count = 0L;
        SQLiteDatabase db = getReadableDatabase();
        if (db != null) {
            String countQuery = "SELECT MAX(" + LocationData.TRACK_LOG_ID + ") FROM " + LocationData.TABLE_NAME;
            Cursor cursor = db.rawQuery(countQuery, null);
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    count = cursor.getLong(0);
                }
                cursor.close();
            }
            db.close();
        }
        // return count
        return count;
    }

    public List<LocationData> getFromAllLocation(long fromId) {
        List<LocationData> notes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        if (db != null) {
            // Select All Query
            String selectQuery = "SELECT * FROM " + LocationData.TABLE_NAME + " WHERE " + LocationData.TRACK_LOG_ID + " > " + fromId;
            //+" ORDER BY " +   LocationData.COLUMN_TIMESTAMP + " DESC";

            Timber.e("getFromAllLocation: check query -----> %s", selectQuery);
            // select * from tblLocation  where locationId  between 33 and 87 order by locationId desc

            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor != null) {
                // looping through all rows and adding to list
                if (cursor.moveToNext()) {
                    do {
                        LocationData locationData = new LocationData();
                        locationData.setLocationId(cursor.getLong(cursor.getColumnIndex(LocationData.TRACK_LOG_ID)));
                        locationData.setUserId(cursor.getLong(cursor.getColumnIndex(LocationData.IMEI_NO)));
                        locationData.setLatitude(cursor.getDouble(cursor.getColumnIndex(LocationData.LATITUDE)));
                        locationData.setLongitude(cursor.getDouble(cursor.getColumnIndex(LocationData.LONGITUDE)));
                        locationData.setTimestamp(cursor.getString(cursor.getColumnIndex(LocationData.CREATED_ON)));

                        notes.add(locationData);
                    } while (cursor.moveToNext());
                }

                // close cursor connection
                cursor.close();
            }
            db.close();
        }
        // return notes list
        Timber.e("getFromAllLocation: returns records Size :%s", notes.size());
        return notes;
    }

    public long getMaxSyncCount() {
        long count = 0L;
        SQLiteDatabase db = getReadableDatabase();
        if (db != null) {
            String countQuery = "SELECT MAX(" + SyncData.LAST_TRACK_LOG_ID + ") FROM " + SyncData.TABLE_NAME;
            Cursor cursor = db.rawQuery(countQuery, null);
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    count = cursor.getLong(0);
                }
                cursor.close();
            }
            db.close();
        }
        // return count
        return count;
    }

    public String getMacAddress() {
        /*WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        String macAddress = wInfo.getMacAddress();
        Log.e(TAG, "getMacAddress: MAC ADDRESS_____>"+macAddress );
        return macAddress;*/


        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    //res1.append(Integer.toHexString(b & 0xFF) + ":");
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";


    }


}
