package in.gipl.tracking.gpcb;

import android.app.Application;


import in.gipl.tracking.gpcb.database.DataBaseHelper;
import timber.log.Timber;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DataBaseHelper.app = this;

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
