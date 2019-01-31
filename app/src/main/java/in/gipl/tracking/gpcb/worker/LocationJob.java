package in.gipl.tracking.gpcb.worker;

import android.location.Location;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.util.concurrent.Callable;


import in.gipl.tracking.gpcb.database.DataBaseHelper;
import in.gipl.tracking.gpcb.database.LocationData;
import in.gipl.tracking.gpcb.helper.LocationJobHelper;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class LocationJob extends JobService {

    public static final String TAG = "worker.LocationJob";

    @Override
    public boolean onStartJob(final JobParameters job) {
        Timber.d("onStartJob: ");
        getLocation(job);
        return true;
    }

    @Override
    public boolean onStopJob(final JobParameters job) {
        Timber.d("onStopJob: ");
        return true;
    }

    private void getLocation(final JobParameters job) {
        new LocationJobHelper(getApplicationContext(), new LocationJobHelper.OnLocationUpdatesListener() {
            @Override
            public void onLocationUpdate(Location location) {
                Timber.d("onLocationUpdate(): location: %s", location);
                databaseOperation(job, location);
            }

            @Override
            public void onLocationError(Exception e) {
                Timber.d("onLocationError(): %s", e.getMessage());
                e.printStackTrace();
                jobFinished(job, true);
            }
        });
    }

    private void databaseOperation(final JobParameters job, final Location location) {
        Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Timber.d("call: ");
                // Perform database operation here
                // this is background thread itself
                // don't start new Thread or AsyncTask here

                DataBaseHelper db = new DataBaseHelper(LocationJob.this);
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
}
