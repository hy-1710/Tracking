package in.gipl.tracking.gpcb.asynctask;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import in.gipl.tracking.gpcb.database.DataBaseHelper;
import in.gipl.tracking.gpcb.database.LocationData;
import in.gipl.tracking.gpcb.databinding.ActivityMainBinding;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;

public class OkHttpAsyncTask extends AsyncTask<Void, Void, String> {

    OkHttpClient client ;
    List<LocationData> locationData ;
    DataBaseHelper dataBaseHelper;
    String jsonArrayString;
    String resp;
    Response response;
    static String jsonData;

    ActivityMainBinding binding;
    public static final String TAG = OkHttpAsyncTask.class.getSimpleName();

    public OkHttpAsyncTask(List<LocationData> locationData, DataBaseHelper dataBaseHelper, ActivityMainBinding binding)
    {
        this.locationData = locationData;
        this.dataBaseHelper = dataBaseHelper;
        client = new OkHttpClient();
        this.binding = binding;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected String doInBackground(Void... voids) {

        Log.e(TAG, "doInBackground: Called--" );

        if(locationData.size() > 0)
        {

            Log.i(TAG, "doInBackground: list size" + locationData.size());



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
                sendData(jsonArrayString);



            }catch (JSONException e) {
                e.printStackTrace();
            }


        }else
        {

        }


        return null;


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
                        .make(binding.coordinatorLayout, "Sorry! Your Device is not Configure.", Snackbar.LENGTH_LONG)
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

    private String sendData(String Message)
    {
        Log.e(TAG, "sendData: " );

        if(Message!= null)
        {
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
                        Log.e(TAG, "onResponse: " + jsonData);

                        if (response != null) {



                            if (response.isSuccessful()) {

                                Log.e(TAG, "doInBackground: response is Succesfull---");
                                LocationData locationDataWithMaxId = getLocationDataWithMaxId(locationData);
                                dataBaseHelper.insertSyncData(
                                        locationDataWithMaxId.getLocationId(),
                                        locationDataWithMaxId.getUserId(),
                                        locationData.size(),
                                        getCurrentTimeUsingDate()
                                );



                            }else
                            {
                                Log.e(TAG, "onResponse: "+ response.message());
                                jsonData = response.message();

                            }


                        }

                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            });


            return setMessage(jsonData);


        }else
        {

        }
        Log.e(TAG, "sendData: JSONDATA--->"+jsonData );
        return  setMessage(jsonData);
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

       /* Log.e(TAG, "onPostExecute: "+s );
        if(s!= null) {


            if (s.contains("True")) {
                Log.e(TAG, "onPostExecute:s.contains(\"True\")  Called " );
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
                Log.e(TAG, "onPostExecute: else if (s.contains(\"No Tracklog Found\") Called " );
                Snackbar snackbar = Snackbar
                        .make(binding.coordinatorLayout, "No TrackLog Found!", Snackbar.LENGTH_LONG)
                        .setAction("Okay", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        });

                snackbar.show();
                snackbar.setActionTextColor(Color.YELLOW);
            }

            else {
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


        }*/

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ExcecuteMessage();
                //Do something after 100ms
            }
        }, 500);



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
