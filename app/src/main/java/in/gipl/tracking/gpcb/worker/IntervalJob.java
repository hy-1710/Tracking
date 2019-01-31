package in.gipl.locationjob.worker;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.util.concurrent.Callable;

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

public class IntervalJob extends JobService {

    public static final String TAG = "worker.IntervalJob";


    String locationInterval = "";
    String TimeInterval = "";

    @Override
    public boolean onStartJob(final JobParameters job) {
        Timber.d("onStartJob: ");
        databaseOperation(job);
        return true;
    }

    @Override
    public boolean onStopJob(final JobParameters job) {
        Timber.d("onStopJob: ");
        return true;
    }

    private void databaseOperation(final JobParameters job) {
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


                sendPost(job);
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

    private void sendPost(final JobParameters job) throws Exception {
        // Likewise call() throws Exception
        // sendPost() throws Exception
        // Still onError() will call If anything goes wrong.
        // Your app won't crash, Don't worry

        Timber.e("sendPost: CALLED");


        String jsonArrayString = "'";

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


