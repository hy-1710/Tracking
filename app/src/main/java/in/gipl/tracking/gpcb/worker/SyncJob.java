package in.gipl.tracking.gpcb.worker;

import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;


import in.gipl.tracking.gpcb.database.DataBaseHelper;
import in.gipl.tracking.gpcb.database.LocationData;
import in.gipl.tracking.gpcb.database.SyncPostData;
import in.gipl.tracking.gpcb.webservice.ApiClient;
import in.gipl.tracking.gpcb.webservice.ApiInterface;
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
import okhttp3.logging.HttpLoggingInterceptor;
import timber.log.Timber;

public class SyncJob extends JobService {

    public static final String TAG = "worker.SyncJob";

    @Override
    public boolean onStartJob(final JobParameters job) {
        Timber.d("onStartJob: ");
        databaseOperation(job);
        return true;
    }

    @Override
    public boolean onStopJob(final JobParameters job) {
        Timber.d("onStopJob: ");
        return true; //Answers the question: "Should this job not be retried?"
        //return false; //Answers the question: "Should this job be retried?"
    }

    private void databaseOperation(final JobParameters job) {
        Single.fromCallable(new Callable<List<LocationData>>() {
            @Override
            public List<LocationData> call() throws Exception {
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

                DataBaseHelper db = new DataBaseHelper(SyncJob.this);

                // case 1: last data size 0 and db has data then select * from table
                // case 2: last data size > 0 and db have more then data then send the records
                // case 3: last data size = db.data

                // when there is no data in db then stop the service
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
                // sendPost(locationDataList, job, db);
                sendLog(locationDataList, job, db);
                return locationDataList;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<List<LocationData>>() {
            @Override
            public void onSubscribe(Disposable d) {
                Timber.d("onSubscribe: ");
            }

            @Override
            public void onSuccess(List<LocationData> locationData) {
                Timber.d("onSuccess: ");
                jobFinished(job, true);
            }

            @Override
            public void onError(Throwable e) {
                Timber.d("onError: ");
                e.printStackTrace();
                jobFinished(job, true);
            }
        });
    }

    private void sendLog(final List<LocationData> locationDataList, final JobParameters job, DataBaseHelper db) throws Exception {


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
        Log.e(TAG, "sendPost: jsonSring -----" + jsonArrayString);


        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType,
                jsonArrayString);
        Request request = new Request.Builder()
                .url("http://demo.gipl.in/GPCBMobile.svc/SyncTracklog")
               // .url("http://localhost:1065/GPCBMobile.svc/SyncTrackLog")
                .post(body)
                .addHeader("Content-Type", "text/plain")
              /*  .addHeader("Cache-Control", "no-cache")
                .addHeader("Postman-Token", "11986b1f-5fcb-49d4-a41e-19e67d936c3c")*/
                .build();

        Response response = client.newCall(request).execute();

        if (response != null) {
            ResponseBody responseBody = response.body();
            if (response.isSuccessful()) {
                LocationData locationDataWithMaxId = getLocationDataWithMaxId(locationDataList);
                db.insertSyncData(
                        locationDataWithMaxId.getLocationId(),
                        locationDataWithMaxId.getUserId(),
                        locationDataList.size(),
                        getCurrentTimeUsingDate()
                );
                if (responseBody != null) {
                    String responseMessage = responseBody.string();
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
        }





  /*      ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        retrofit2.Call<List<SyncPostData>> call = apiService.SyncTrackLog(jsonArrayString);






        call.enqueue(new retrofit2.Callback<List<SyncPostData>>() {
            @Override
            public void onResponse(retrofit2.Call<List<SyncPostData>> call, retrofit2.Response<List<SyncPostData>> response) {

                String responseBody = response.message().toString();

                Log.e(TAG, "onResponse: resposeBody  :---" + responseBody );
                if (response.isSuccessful()) {

                    Log.e(TAG, "onResponse: " + response.message().toString());

                } else {
                    Log.e(TAG, "onResponse: Error : " + response.message());
                }


            }

            @Override
            public void onFailure(retrofit2.Call<List<SyncPostData>> call, Throwable t) {

            }
        });*/

      /*  apiService.SyncTrackLog(jsonArrayString) .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver() {

                    @Override
                    public void onSuccess(Object o) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        // Network error
                    }
                });*/
    }


    private void sendPost(final List<LocationData> locationDataList, final JobParameters job, DataBaseHelper db) throws Exception {
        // Likewise call() throws Exception
        // sendPost() throws Exception
        // Still onError() will call If anything goes wrong.
        // Your app won't crash, Don't worry

        Timber.e("sendPost: CALLED");
        if (locationDataList.size() > 0) {
            Timber.e("sendPost: CHECK Size of Data list for service call: %s", locationDataList.size());
            Timber.e("sendPost: CHECK DATA: %s", locationDataList);

            //with the help of StringBuilder
            StringBuilder builder = new StringBuilder();
            builder.append('[');
            for (int i = 0; i < locationDataList.size(); i++) {
                LocationData locationData = locationDataList.get(i);
                if (i > 0) {
                    builder.append(',');
                }
                builder.append('{');

                // UserId
                builder.append('"');
                builder.append(LocationData.IMEI_NO);
                builder.append('"');
                builder.append(':');
                builder.append(locationData.getUserId());
                builder.append(',');

                // Latitude
                builder.append('"');
                builder.append(LocationData.LATITUDE);
                builder.append('"');
                builder.append(':');
                builder.append(locationData.getLatitude());
                builder.append(',');

                // Longitude
                builder.append('"');
                builder.append(LocationData.LONGITUDE);
                builder.append('"');
                builder.append(':');
                builder.append(locationData.getLongitude());
                builder.append(',');

                // Longitude
                builder.append('"');
                builder.append(LocationData.CREATED_ON);
                builder.append('"');
                builder.append(':');
                builder.append('"');
                builder.append(locationData.getTimestamp());
                builder.append('"');
                // builder.append(',');

                builder.append('}');
            }
            builder.append(']');
            String json = builder.toString();

            /////-----End of StringBuilder


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
            Log.e(TAG, "sendPost: jsonSring -----" + jsonArrayString);

            // Request
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(logging).build();

            MediaType mediaType = MediaType.parse("text/xml");
            // RequestBody requestBody = RequestBody.create(mediaType, "<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><SyncTracklog xmlns=\"http://tempuri.org/\"><Tracklog>" + json + "</Tracklog></SyncTracklog></soap:Body></soap:Envelope>");
            RequestBody requestBody = RequestBody.create(mediaType, "<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><SyncTracklog xmlns=\"http://tempuri.org/\"><Tracklog>" + jsonArrayString + "</Tracklog></SyncTracklog></soap:Body></soap:Envelope>");
            Request request = new Request.Builder()
                    .url("http://dsl.gipl.net/gsplvtsmobile.asmx")
                    .post(requestBody)
                    .addHeader("Content-Type", "text/xml")
                    .addHeader("SOAPAction", "http://tempuri.org/SyncTracklog")
                    .build();

            Response response = client.newCall(request).execute();
            if (response != null) {
                ResponseBody responseBody = response.body();
                if (response.isSuccessful()) {
                    LocationData locationDataWithMaxId = getLocationDataWithMaxId(locationDataList);
                    db.insertSyncData(
                            locationDataWithMaxId.getLocationId(),
                            locationDataWithMaxId.getUserId(),
                            locationDataList.size(),
                            getCurrentTimeUsingDate()
                    );
                    if (responseBody != null) {
                        String responseMessage = responseBody.string();
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
            }
        }
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
