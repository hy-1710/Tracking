package in.gipl.tracking.gpcb.asynctask;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
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
import okhttp3.Call;
import okhttp3.Callback;
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
    static String jsonData;



    ActivityMainBinding binding;
    public static final String TAG = FirstLocationSyncTask.class.getSimpleName();

    public FirstLocationSyncTask(DataBaseHelper dataBaseHelper, ActivityMainBinding binding, MainActivity activity, Location location) {
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


        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            String table = LocationData.TABLE_NAME;
            Timber.e("call: Lat: %s, Long: : %s, TABLE: %s", latitude, longitude, table);

            long id = db.insertNote(latitude, longitude);
            Timber.e("call: ID: %s", id);


            s = Long.toString(id);
            SyncData(); // get data from DB


        }

        return null;


    }

    private void SyncData()
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
                Log.e(TAG, "sendPost: JSON -----" + jsonArrayString);
                sendData(jsonArrayString);









            }catch (JSONException e) {
                e.printStackTrace();
            }




        }else
        {
            Log.e(TAG, "SyncData: NO TRACKLOG " );

        }




    }


    private void ExcecuteMessage()
    {
        Log.e(TAG, "ExcecuteMessage: "+jsonData );
        if (jsonData != null) {
            Log.e(TAG, "ExcecuteMessage: jsonDATA is not null" );



            if (jsonData.contains("True")) {
                Log.e(TAG, "onPostExecute:s.contains(\"True\")  Called ");
                Snackbar snackbar = Snackbar
                        .make(binding.coordinatorLayout, "Send TrackLog Successfully", Snackbar.LENGTH_LONG)
                        .setAction("Okay", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                   /* Snackbar snackbar1 = Snackbar.make(binding.coordinatorLayout, "Message is restored!", Snackbar.LENGTH_SHORT);
                                    snackbar1.show();*/
                            }
                        });

                snackbar.show();
                snackbar.setActionTextColor(Color.YELLOW);
            } else if (jsonData.contains("Device Not Configure")) {
                Snackbar snackbar = Snackbar
                        .make(binding.coordinatorLayout, "Sorry! Your Device is not Configured", Snackbar.LENGTH_LONG)
                        .setAction("Okay", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                   /* Snackbar snackbar1 = Snackbar.make(binding.coordinatorLayout, "Message is restored!", Snackbar.LENGTH_SHORT);
                                    snackbar1.show();*/
                            }
                        });

                snackbar.show();
                snackbar.setActionTextColor(Color.YELLOW);
            } else if (jsonData.contains("False")) {
                Snackbar snackbar = Snackbar
                        .make(binding.coordinatorLayout, "Something went wrong!", Snackbar.LENGTH_LONG)
                        .setAction("Okay", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        });

                snackbar.show();
                snackbar.setActionTextColor(Color.YELLOW);
            } else if (jsonData.contains("No Tracklog Found")) {
                Log.e(TAG, "onPostExecute: else if (s.contains(\"No Tracklog Found\") Called ");
                Snackbar snackbar = Snackbar
                        .make(binding.coordinatorLayout, "No TrackLog Found!", Snackbar.LENGTH_LONG)
                        .setAction("Okay", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        });

                snackbar.show();
                snackbar.setActionTextColor(Color.YELLOW);
            } else {
                Snackbar snackbar = Snackbar
                        .make(binding.coordinatorLayout, "Something went wrong!", Snackbar.LENGTH_LONG)
                        .setAction("Okay", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        });

                snackbar.show();
                snackbar.setActionTextColor(Color.YELLOW);
            }
        }
    }
    private void sendData(String Message)
    {
        Log.e(TAG, "sendData  Message -->: "+Message );

        if(Message!= null)
        {
            Log.e(TAG, "sendData: IF MESSGE NOT NUll" );
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType,
                    jsonArrayString);
            Request request = new Request.Builder()
                    .url("http://demo.gipl.in/GPCBMobile.svc/SyncTracklog")
                    // .url("http://gpcblta.gipl.in/GPCBMobile.svc/SyncTracklog")
                    .post(body)
                    .addHeader("Content-Type", "text/plain")

                    .addHeader("Cache-Control", "no-cache")
                    .addHeader("Postman-Token", "11986b1f-5fcb-49d4-a41e-19e67d936c3c")

                    .build();

            Call cL = client.newCall(request);
            cL.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "onFailure: " );
                    e.printStackTrace();
                    jsonData= e.getMessage().toString();
                    Log.e(TAG, "onFailure: "+jsonData );


                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        jsonData = response.body().string();
                        Log.e(TAG, "onResponse response: jsonData " + jsonData);

                        if (response != null) {
                            Log.e(TAG, "onResponse: is not null" );

                            Log.e(TAG, "onResponse: response.isSuccessful()---> "+response.isSuccessful() );



                            if (response.isSuccessful()) {

                                Log.e(TAG, "doInBackground: response is Succesfull---");
                                LocationData locationDataWithMaxId = getLocationDataWithMaxId(locationData);
                                dataBaseHelper.insertSyncData(
                                        locationDataWithMaxId.getLocationId(),
                                        locationDataWithMaxId.getUserId(),
                                        locationData.size(),
                                        getCurrentTimeUsingDate()
                                );
                                setMessage(jsonData);



                            }else
                            {
                                Log.e(TAG, "onResponse not success: "+ response.message());
                                jsonData = response.message();
                                setMessage(jsonData);

                            }


                        }else
                        {
                            Log.e(TAG, "onResponse: is null" );
                        }

                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            });



        }else
        {

            Log.e(TAG, "sendData: IF MESSGAE IS NULL" );
            setMessage("No Tracklog Found");
        }



    }

    private String setMessage(String message)
    {
        Log.e(TAG, "setMessage: "+message );
        if(message!= null)
        {
            return message;
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.e(TAG, "onPostExecute: Called--->" );

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ExcecuteMessage();
                //Do something after 100ms
            }
        }, 500);

      //  new Thread(new ExecuteMessageTask() ).start();
       // ExcecuteMessage();



/*

        if (s.contains("True")) {
            Log.e(TAG, "onPostExecute:s.contains(\"True\")  Called ");
            Snackbar snackbar = Snackbar
                    .make(binding.coordinatorLayout, "Send TrackLog Successfully", Snackbar.LENGTH_LONG)
                    .setAction("Okay", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                   *//* Snackbar snackbar1 = Snackbar.make(binding.coordinatorLayout, "Message is restored!", Snackbar.LENGTH_SHORT);
                                    snackbar1.show();*//*
                        }
                    });

            snackbar.show();
            snackbar.setActionTextColor(Color.YELLOW);
        } else if (s.contains("Device Not Configure")) {
            Snackbar snackbar = Snackbar
                    .make(binding.coordinatorLayout, "Sorry! Device is not Configure. Contact your administrator", Snackbar.LENGTH_LONG)
                    .setAction("Okay", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                   *//* Snackbar snackbar1 = Snackbar.make(binding.coordinatorLayout, "Message is restored!", Snackbar.LENGTH_SHORT);
                                    snackbar1.show();*//*
                        }
                    });

            snackbar.show();
            snackbar.setActionTextColor(Color.YELLOW);
        } else if (s.contains("False")) {
            Snackbar snackbar = Snackbar
                    .make(binding.coordinatorLayout, "Something went wrong!", Snackbar.LENGTH_LONG)
                    .setAction("Okay", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });

            snackbar.show();
            snackbar.setActionTextColor(Color.YELLOW);
        } else if (s.contains("No Tracklog Found")) {
            Log.e(TAG, "onPostExecute: else if (s.contains(\"No Tracklog Found\") Called ");
            Snackbar snackbar = Snackbar
                    .make(binding.coordinatorLayout, "No TrackLog Found!", Snackbar.LENGTH_LONG)
                    .setAction("Okay", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });

            snackbar.show();
            snackbar.setActionTextColor(Color.YELLOW);
        } else {
            Snackbar snackbar = Snackbar
                    .make(binding.coordinatorLayout, "Something went wrong!", Snackbar.LENGTH_LONG)
                    .setAction("Okay", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });

            snackbar.show();
            snackbar.setActionTextColor(Color.YELLOW);
        }*/
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