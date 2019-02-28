package in.gipl.tracking.gpcb.asynctask;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.novoda.sexp.Streamer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import in.gipl.tracking.gpcb.MainActivity;
import in.gipl.tracking.gpcb.database.DataBaseHelper;
import in.gipl.tracking.gpcb.database.LocationData;
import in.gipl.tracking.gpcb.databinding.ActivityMainBinding;
import in.gipl.tracking.gpcb.helper.LocationJobHelper;
import in.gipl.tracking.gpcb.worker.LocationJob;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;

public class FirstLocationSyncTask  extends AsyncTask<Void, Void, String> {

    OkHttpClient client;
    List<LocationData> locationData;
    DataBaseHelper dataBaseHelper;
    String jsonArrayString;
    String resp;
    Response response;
    MainActivity activity;
    DataBaseHelper db;
    String s;
    Location location;



    ActivityMainBinding binding;
    public static final String TAG = FirstLocationSyncTask.class.getSimpleName();

    public FirstLocationSyncTask( DataBaseHelper dataBaseHelper, ActivityMainBinding binding, MainActivity activity, Location location) {
       locationData = new ArrayList<>();
        this.dataBaseHelper = dataBaseHelper;
        this.activity = activity;
        client = new OkHttpClient();
        this.binding = binding;
        this.location = location;
       db = new DataBaseHelper(activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... voids) {


        if(location!= null)
        {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            String table = LocationData.TABLE_NAME;
            Timber.e("call: Lat: %s, Long: : %s, TABLE: %s", latitude, longitude, table);

            long id = db.insertNote(latitude, longitude);
            Timber.e("call: ID: %s", id);
            Log.e(TAG, "call: TOTAL NO OF RECORDS :" + db.getAllLocation());

            s = Long.toString(id);
            String str = SyncData();

            if(str!= null) {

            }
        }

        return  null;


    }


    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        Log.e(TAG, "onPostExecute: "+s );


    }

    private String SyncData()
    {
        if(db.getAllLocation().size() > 0)
        {
            locationData = db.getAllLocation();

            try {
                JSONArray jsonArray = new JSONArray();

                for (LocationData data : locationData) {

                    JSONObject Tracklog = new JSONObject();
                    Tracklog.put("IMEINo", data.getUserId());
                    Tracklog.put("Latitude", data.getLatitude());
                    Tracklog.put("Longitude", data.getLongitude());
                    Tracklog.put("CreatedOn", data.getTimestamp());
                    jsonArray.put(Tracklog);
                }

                jsonArrayString = jsonArray.toString();



            }catch (JSONException e) {
                e.printStackTrace();
            }

            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType,
                    jsonArrayString);
            Request request = new Request.Builder()
                    .url("http://demo.gipl.in/GPCBMobile.svc/SyncTracklog")
                    .post(body)
                    .addHeader("Content-Type", "text/plain")

                    .addHeader("Cache-Control", "no-cache")
                    .addHeader("Postman-Token", "11986b1f-5fcb-49d4-a41e-19e67d936c3c")

                    .build();

            try {
                response = client.newCall(request).execute();
                if (response != null) {

                    Log.e(TAG, "doInBackground: BODY----"+response.body().string());
                    /*  resp = response.body().string();*/
                    ResponseBody responseBody = response.body();

                    if (response.isSuccessful()) {

                        Log.e(TAG, "doInBackground: response is Succesfull---" );
                        LocationData locationDataWithMaxId = getLocationDataWithMaxId(locationData);
                        dataBaseHelper.insertSyncData(
                                locationDataWithMaxId.getLocationId(),
                                locationDataWithMaxId.getUserId(),
                                locationData.size(),
                                getCurrentTimeUsingDate()
                        );
                        if (responseBody != null) {
                            Log.e(TAG, "doInBackground: "+response.message() );


                            if(response.message().equals("OK"))
                            {

                                // String s= ""+response.body().string();
                                return response.message();
                            }



                        } else {

                            return response.message();

                        }
                    } else {

                        if (responseBody != null) {
                            return response.message();


                        } else {
                            return response.message();
                        }
                    }


                }else

                {
                    Log.e(TAG, "doInBackground: ---"+response.body().string() );
                }



            } catch (IOException e) {
                e.printStackTrace();
            }


        }else
        {

        }



        return  null;
    }

    private LocationData getLocationDataWithMaxId(final List<LocationData> locationDataList) {
        LocationData locationDataMaxId = new LocationData();
        for (LocationData locationData : locationDataList) {
            if (locationData.getLocationId() > locationDataMaxId.getLocationId()) {
                locationDataMaxId = locationData;
            }
        }
        return locationDataMaxId;
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



}