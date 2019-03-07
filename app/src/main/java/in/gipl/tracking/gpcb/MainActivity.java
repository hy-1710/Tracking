package in.gipl.tracking.gpcb;

import android.content.DialogInterface;
import android.content.Intent;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.novoda.sexp.finder.ElementFinder;

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
import java.util.concurrent.Callable;


import in.gipl.tracking.gpcb.asynctask.FirstLocationSyncTask;
import in.gipl.tracking.gpcb.asynctask.OkHttpAsyncTask;
import in.gipl.tracking.gpcb.database.DataBaseHelper;
import in.gipl.tracking.gpcb.database.IntervalData;
import in.gipl.tracking.gpcb.database.LocationData;
import in.gipl.tracking.gpcb.database.SyncPostData;
import in.gipl.tracking.gpcb.databinding.ActivityMainBinding;
import in.gipl.tracking.gpcb.helper.LocationActivityHelper;
import in.gipl.tracking.gpcb.helper.LocationJobHelper;
import in.gipl.tracking.gpcb.prefs.AppPrefs;
import in.gipl.tracking.gpcb.webservice.ApiClient;
import in.gipl.tracking.gpcb.webservice.ApiInterface;
import in.gipl.tracking.gpcb.worker.LocationJob;
import in.gipl.tracking.gpcb.worker.SyncJob;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;

public class        MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static AppPrefs appPrefs;
    private static ElementFinder<String> elementFinder;
    int SyncInterval = 30;
    int LocationInterval = 15;
    String x = "30", y = "15";
    Animation animFadeIn;
    boolean check = true;
    private ActivityMainBinding binding;
    private LocationActivityHelper locationActivityHelper;
    private long mLastClickTime = 0;
    Utils utils;
    DataBaseHelper db ;
    Location loc ;
    boolean isFirstTimeInstall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(MainActivity.this, R.layout.activity_main);
        appPrefs = new AppPrefs(MainActivity.this);
        utils = new Utils(TAG, MainActivity.this);
        Log.e(TAG, "onCreate: called--" );
        getSyncInterval(check = false);
        db = new DataBaseHelper(MainActivity.this);

        if(appPrefs.getIMEINO()!= null)
        {
            isFirstTimeInstall = false;
        }else
        {
            Log.e(TAG, "onCreate: APP IS INSTALL FIRST TIME_____>>>" );
            isFirstTimeInstall = true;
        }


    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //buton click for configuration

        // getUniqueIMEIId(MainActivity.this);

        getSyncInterval(check = false);

        if (appPrefs.getIMEINO() != null) {
            binding.tvIMEINO.setText(appPrefs.getIMEINO());
        }




        binding.txtSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animFadeIn = AnimationUtils.loadAnimation(MainActivity.this,
                        R.anim.fade_in);
                try {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 3000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                binding.txtSubmit.startAnimation(animFadeIn);
                //pass flag to check if button click then show snackbar else not
                getSyncInterval(check=true);


            }
        });

        binding.txtSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animFadeIn = AnimationUtils.loadAnimation(MainActivity.this,
                        R.anim.fade_in);
                try {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 3000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                binding.txtSend.startAnimation(animFadeIn);
                try {
                    sendTrackLog();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });


        // Get Location
        locationActivityHelper = new LocationActivityHelper(this, new LocationActivityHelper.OnLocationUpdatesListener() {
            @Override
            public void onLocationUpdate(Location location) {
                if (location != null) {
                    Timber.i("location is %s", location);
                    loc = location;
                    Log.e(TAG, "onLocationUpdate: LOCAtion : "+loc );
                    binding.tvLocation.setText(location.getLatitude() + "  | " + location.getLongitude());
                    //binding.tvIMEINO.setText(appPrefs.getIMEINO()!= null ? appPrefs.getIMEINO() : "Could not find IMEI Number." );
                    binding.tvIMEINO.setText(appPrefs.getIMEINO());
                    if(isFirstTimeInstall)
                    {
                        SyncFirstTIme();
                    }

                }
            }

            @Override
            public void onLocationError(Exception e) {
                e.printStackTrace();
                Utils.showMessage(MainActivity.this, e.getMessage());
            }
        }, appPrefs, binding);

        locationActivityHelper.create();







        // Start Firebase Job

        //  scheduleLocationJob();
        //    scheduleSyncJob();


    }

    private void SyncFirstTIme()
    {
        Log.e(TAG, "onPostCreate:  BEFORE IF" );
        if(isFirstTimeInstall)
        {

            if(loc!= null)
            {

                if(utils.isDeviceOnline())
                {
                    Log.e(TAG, "onPostCreate: Calling  FirstLocationSyncTask---" );
                    new FirstLocationSyncTask(db, binding, MainActivity.this, loc).execute();

                }else {
                    Log.e(TAG, "onPostCreate: DEVICE OFFLINE" );


                    utils.showMessage(MainActivity.this, "Oops! ", "No Internet connection.Please Turn On Internet Connection for Device Location Configuration", "Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }, "Turn On", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                            startActivity(intent);
                        }
                    });
                }


            }
        }
    }

    @Override
    protected void onPause() {
        // Remove location updates to save battery.
        locationActivityHelper.pause();
        super.onPause();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        locationActivityHelper.resume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationActivityHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        locationActivityHelper.onActivityResult(requestCode, resultCode, data);
    }




    private  void sendTrackLog() throws Exception {




        long maxId = db.getMaxNotesCount();
        long maxSyncId = db.getMaxSyncCount();


        List<LocationData> locationDataList = new ArrayList<>();
        if (maxId > 0L) {
            //in db there are records
            if (maxSyncId == 0L) {
                //no records has been sent
                Timber.e("call: appPrefs.getLocationId() = 0 and Inside If");

                //so add all data into list from local db
                locationDataList.addAll(db.getAllLocation());

                // If you just write "else if (maxSyncId > 0L)" it will always true
                // so, bottom's "else if (maxId == maxSyncId)" will never occur
            } else if (maxSyncId > 0L && maxSyncId < maxId) {
                // means some records have been sent to server
                // few records has been sent
                locationDataList.addAll(db.getFromAllLocation(maxSyncId));
            } else if (maxId == maxSyncId) {
                //all records sync
                Timber.e("call: NO RECORDS INSIDE DB for sync:");
            }
        }



        sendLog(locationDataList, db);



    }

    /**
     * when user forcefully click on sendTracklog button then send tracklog server side
     * @param locationDataList
     * @param db
     * @throws Exception
     */

    private void sendLog(final List<LocationData> locationDataList, final DataBaseHelper db) throws Exception {


        JSONArray jsonArray = new JSONArray();

        for (LocationData data : locationDataList) {

            JSONObject Tracklog = new JSONObject();
            Tracklog.put("IMEINo", data.getUserId());
            Tracklog.put("Latitude", data.getLatitude());
            Tracklog.put("Longitude", data.getLongitude());
            Tracklog.put("CreatedOn", data.getTimestamp());

            jsonArray.put(Tracklog);

        }

        String jsonArrayString = jsonArray.toString();
        Log.e(TAG, "sendLog: jsonSring -----" + jsonArrayString);



        if(utils.isDeviceOnline()) {

            new OkHttpAsyncTask(locationDataList,db, binding).execute();
        }else
        {
            Log.e(TAG, "sendLog: DEVICE OFFLINE" );
            utils.showMessage(MainActivity.this, "Oops! ", "No Internet connection.Please check Your Internet Connection", "Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }, "Turn On", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                    startActivity(intent);
                }
            });
        }






     }

    private void scheduleLocationJob() {
        //creating new firebase job dispatcher
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));

        // Passing Job Parameter
        // Bundle extras = new Bundle();
        // extras.putString("aString", "aString");
        // extras.putInt("aInt", 1);
        // extras.putDouble("aDouble", 2.0);

        Job locationJob = dispatcher.newJobBuilder()
                //persist the task across boots
                .setLifetime(Lifetime.FOREVER)

                //the JobService that will be called
                .setService(LocationJob.class)
                //.setExtras(extras)

                //uniquely identifies the job
                .setTag(LocationJob.TAG)

                //don't overwrite an existing job with the same tag
                .setReplaceCurrent(true)

                // We are mentioning that the job is periodic.
                .setRecurring(true)

                // Run between 10 - 12 minutes from now.
                  .setTrigger(Trigger.executionWindow(LocationInterval, LocationInterval + 2))
                //.setTrigger(Trigger.executionWindow(5 * 60, 7 * 60))
                //.setTrigger(Trigger.executionWindow(30, 60))

                // retry with exponential backoff
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)

                .build();

        dispatcher.mustSchedule(locationJob);
    }

    private void scheduleSyncJob() {
        //creating new firebase job dispatcher
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));

        // Passing Job Parameter
        // Bundle extras = new Bundle();
        // extras.putString("aString", "aString");
        // extras.putInt("aInt", 1);
        // extras.putDouble("aDouble", 2.0);

        Job syncJob = dispatcher.newJobBuilder()
                //persist the task across boots
                .setLifetime(Lifetime.FOREVER)

                //the JobService that will be called
                .setService(SyncJob.class)
                //.setExtras(extras)

                //uniquely identifies the job
                .setTag(SyncJob.TAG)

                //don't overwrite an existing job with the same tag
                .setReplaceCurrent(true)

                // We are mentioning that the job is periodic.
                .setRecurring(true)


                // Run between 10 - 12 minutes from now.
                 .setTrigger(Trigger.executionWindow(SyncInterval, SyncInterval + 2))
               // .setTrigger(Trigger.executionWindow(7 * 60, 9 * 60))
                //.setTrigger(Trigger.executionWindow(30, 60))

                // retry with exponential backoff
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)

                // conditions
                .setConstraints(
                        // only run on an network
                        Constraint.ON_ANY_NETWORK
                )

                .build();

        dispatcher.mustSchedule(syncJob);
    }

    private void getSyncInterval(final boolean check) {

        if (utils.isDeviceOnline()) {

            Log.e(TAG, "getSyncInterval: DEVICE is ONLINE  ______" );


            ApiInterface apiService =
                    ApiClient.getClient().create(ApiInterface.class);

            retrofit2.Call<List<IntervalData>> call = apiService.getTopRatedMovies();

            call.enqueue(new retrofit2.Callback<List<IntervalData>>() {
                @Override
                public void onResponse(retrofit2.Call<List<IntervalData>> call, retrofit2.Response<List<IntervalData>> response) {
                    //  Log.e(TAG, "onResponse: ", );

                    if (response.isSuccessful()) {

                        Log.e(TAG, "onResponse: checking-----" + response.body());
                        //IntervalData data = response.body();
                        List<IntervalData> intervalData = response.body();
                        Log.e(TAG, "onResponse: intervalData------" + intervalData);
                        IntervalData data = intervalData.get(0);
                        Log.e(TAG, "onResponse: dats is--------" + data);
                        SyncInterval = data.getSyncInterval();
                        LocationInterval = data.getLocationInterval();
                        x = String.valueOf(SyncInterval);
                        y = String.valueOf(LocationInterval);

                        if (binding != null) {
                            binding.tvSyncInterval.setText(x);
                            binding.tvLocationInterval.setText(y);

                        }
                        scheduleLocationJob();
                        scheduleSyncJob();

                        if (check) {
                            Snackbar snackbar = Snackbar
                                    .make(binding.coordinatorLayout, "Configuration Set Successfully", Snackbar.LENGTH_LONG)
                                    .setAction("Okay", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                        }
                                    });


                            snackbar.show();
                            snackbar.setActionTextColor(Color.YELLOW);



                        } else {

                        }



                    } else {
                        Log.e(TAG, "onResponse: Failures----" + response.errorBody());
                    }


                }

                @Override
                public void onFailure(retrofit2.Call<List<IntervalData>> call, Throwable t) {
                    t.printStackTrace();
                    Log.e(TAG, "onFailure: " + t.getMessage());
                }
            });
        }else
        {
            Log.e(TAG, "getSyncInterval: DEVICE OFFLINE" );

            utils.showMessage(MainActivity.this, "Oops! ", "No Internet connection.Please check Your Internet Connection", "Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }, "Turn On", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                    startActivity(intent);
                }
            });

        }




    }

    public ActivityMainBinding getBinding() {
        return binding;
    }

    private void getSyncIntervalusingSOAP() {

        Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Timber.d("call: ");
                // Perform database operation here
                // this is background thread itself
                // don't start new Thread or AsyncTask here

                // Don't call jobFinished() inside call() method
                // if you got nothing to do then return from method
                // jobFinished() will be call from onNext(), onComplete() or onSuccess() from main thread

                // Don't write try {} catch {} inside call() except for stream operations.
                // If something goes wrong, onError() will be called.
                // your app won't crash, Don't worry
                // See the method signature "public List<LocationData> call() throws Exception {}".


//--------------------------------
           /*     // Request
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                OkHttpClient client = new OkHttpClient.Builder().addInterceptor(logging).build();

                MediaType mediaType = MediaType.parse("text/xml");
                // RequestBody requestBody = RequestBody.create(mediaType, "<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><SyncTracklog xmlns=\"http://tempuri.org/\"><Tracklog>" + json + "</Tracklog></SyncTracklog></soap:Body></soap:Envelope>");
                RequestBody requestBody = RequestBody.create(mediaType, "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n  <soap:Body>\r\n    <GetTrackingConfig xmlns=\"http://tempuri.org/\" />\r\n  </soap:Body>\r\n</soap:Envelope>");
                Request request = new Request.Builder()
                        .url("http://dsl.gipl.net/gsplvtsmobile.asmx")
                        .post(requestBody)
                        .addHeader("Content-Type", "text/xml")
                        .addHeader("SOAPAction", "http://tempuri.org/GetTrackingConfig")
                        .build();

                Response response = client.newCall(request).execute();
                if (response != null) {
                    ResponseBody responseBody = response.body();
                    if (response.isSuccessful()) {

                        if (responseBody != null) {
                            String responseMessage = responseBody.string();// <?xml version="1.0" encoding="utf-8"?><soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">  <soap:Body>  <GetTrackingConfigResponse xmlns="http://tempuri.org/"> <GetTrackingConfigResult>[{"LocationInterval":40,"SyncInterval":180}]</GetTrackingConfigResult></GetTrackingConfigResponse></soap:Body></soap:Envelope>


                           *//* binding.tlSyncInterval.getEditText().setText("");
                            binding.tlTimeInterval.getEditText().setText("");*//*
                            //Log.e(TAG, "call: "+responseMessage );

                            String s = responseMessage; //[{"LocationInterval":40,"SyncInterval":180}]



                           *//* ElementFinderFactory factory = SimpleEasyXmlParser.getElementFinderFactory();
                            elementFinder = factory.getStringFinder();
                            Streamer<String> streamer = new SimpleStreamer(elementFinder);
                            String favouriteColour = SimpleEasyXmlParser.parse(s, streamer);*//*

                            String[] split = s.split("\"LocationInterval\":");
                            String locationIntervalString = split[1];

                            String[] s1 = locationIntervalString.split(",");

                            y = s1[0];
                            //here i multiply value 60 so in job service i wont multiply into 60
                            LocationInterval = Integer.parseInt(y) * 60;// 40
                            String realInterval1 = s1[1]; // "SyncInterval":180}]</GetTrackingConfigResult></GetTrackingConfigResponse></soap:Body></soap:Envelope>


                            String[] stt = realInterval1.split("\"SyncInterval\":");

                            String inter = stt[1];//180}]</GetTrackingConfigResult></GetTrackingConfigResponse></soap:Body></soap:Envelope>


                            String[] strr = inter.split("]</GetTrackingConfigResult");
                            String[] str = realInterval1.split("</GetTrackingConfigResult");
                            String check = strr[0];
                            int stringLenth = check.length();
                            x = check.substring(0, stringLenth - 1);

                            //here i multiply value 60 so in job service i wont multiply into 60
                            SyncInterval = Integer.parseInt(x) * 60;


                            Log.e(TAG, "call: LocationInterval-----" + LocationInterval +
                                    "& SyncInterval------ " + SyncInterval);

                            Timber.e("sendPost: Response success message: %s %s %s", response.code(), response.message(), responseMessage);
                        } else {
                            Timber.e("sendPost: Response success message: %s %s", response.code(), response.message());
                        }
                    } else {
                        if (responseBody != null) {
                            String responseMessage = responseBody.string();
                            Timber.e("sendPost: Response error message: %s %s %s", response.code(), response.message(), responseMessage);
                        } else {
                            Timber.e("sendPost: Response error message: %s %s", response.code(), response.message());
                        }
                    }
                }*/
                //---------------------------

                ApiInterface apiService =
                        ApiClient.getClient().create(ApiInterface.class);

                retrofit2.Call<List<IntervalData>> call = apiService.getTopRatedMovies();
                call.enqueue(new retrofit2.Callback<List<IntervalData>>() {
                    @Override
                    public void onResponse(retrofit2.Call<List<IntervalData>> call, retrofit2.Response<List<IntervalData>> response) {
                        //  Log.e(TAG, "onResponse: ", );

                        if (response.isSuccessful()) {

                            Log.e(TAG, "onResponse: checking-----" + response.body());
                            //IntervalData data = response.body();
                            List<IntervalData> intervalData = response.body();
                            Log.e(TAG, "onResponse: intervalData------" + intervalData);
                            IntervalData data = intervalData.get(0);
                            Log.e(TAG, "onResponse: dats is--------" + data);
                            SyncInterval = data.getSyncInterval();
                            LocationInterval = data.getLocationInterval();
                            x = String.valueOf(SyncInterval);
                            y = String.valueOf(LocationInterval);

                        } else {
                            Log.e(TAG, "onResponse: Failures----" + response.errorBody());
                        }


                    }

                    @Override
                    public void onFailure(retrofit2.Call<List<IntervalData>> call, Throwable t) {
                        t.printStackTrace();
                        Log.e(TAG, "onFailure: " + t.getMessage());
                    }
                });


                return "";
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Timber.d("onSubscribe: ");
            }

            @Override
            public void onSuccess(String s) {
                Timber.d("onSuccess: ");
                //  jobFinished(job, true);

                if (binding != null) {
                    binding.tvSyncInterval.setText(x);
                    binding.tvLocationInterval.setText(y);

                } else {

                }
                scheduleLocationJob();
                scheduleSyncJob();

            }


            @Override
            public void onError(Throwable e) {
                Timber.d("onError: ");
                e.printStackTrace();
                //            jobFinished(job, true);
                scheduleLocationJob();
                scheduleSyncJob();

            }
        });

    }



    private void getLocation() {
        new LocationJobHelper(getApplicationContext(), new LocationJobHelper.OnLocationUpdatesListener() {
            @Override
            public void onLocationUpdate(Location location) {
                Timber.d("onLocationUpdate(): location: %s", location);
                databaseOperation( location);
            }

            @Override
            public void onLocationError(Exception e) {
                Timber.d("onLocationError(): %s", e.getMessage());
                e.printStackTrace();

            }
        });
    }

    private void databaseOperation( final Location location) {
        Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Timber.d("call: ");
                // Perform database operation here
                // this is background thread itself
                // don't start new Thread or AsyncTask here

                DataBaseHelper db = new DataBaseHelper(MainActivity.this);
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String table = LocationData.TABLE_NAME;
                Timber.e("call: Lat: %s, Long: : %s, TABLE: %s", latitude, longitude, table);

                long id = db.insertNote(latitude, longitude);
                Timber.e("call: ID: %s", id);
                Log.e(TAG, "call: TOTAL NO OF RECORDS :" + db.getAllLocation());

                return true;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {
                Timber.d("onSubscribe: ");
            }

            @Override
            public void onSuccess(Boolean aBoolean) {
                Timber.d("onSuccess: ");

            }

            @Override
            public void onError(Throwable e) {
                Timber.d("onError: ");
                e.printStackTrace();

            }
        });
    }





}





/*public class OkHttpHandler extends AsyncTask<Void, Void, String> {

    OkHttpClient client ;
    List<LocationData> locationData ;
    DataBaseHelper  dataBaseHelper;
    String jsonArrayString;
    String resp;
    Response response;

    public OkHttpHandler(List<LocationData> locationData, DataBaseHelper dataBaseHelper)
    {
        this.locationData = locationData;
        this.dataBaseHelper = dataBaseHelper;
        client = new OkHttpClient();
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



            }catch (JSONException e) {
                e.printStackTrace();
            }



            Log.e(TAG, "sendPost: JSON -----" + jsonArrayString);

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
                    *//*  resp = response.body().string();*//*
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


        return null;
    }




    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        Log.e(TAG, "onPostExecute: "+s );
        if(s!= null)
        {
            if(s.equals("OK"))
            {


                Timber.e("sendPost: Response success message: %s %s %s", response.code(), response.message(), response.body());
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

            }else if(s.equals("Not Found"))
            {
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
        }else
        {
            Snackbar snackbar = Snackbar
                    .make(binding.coordinatorLayout, "No TrackLog Found!", Snackbar.LENGTH_LONG)
                    .setAction("Okay", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                   *//* Snackbar snackbar1 = Snackbar.make(binding.coordinatorLayout, "Message is restored!", Snackbar.LENGTH_SHORT);
                                    snackbar1.show();*//*
                        }
                    });

            snackbar.show();
            snackbar.setActionTextColor(Color.YELLOW);
        }



    }


}*/
