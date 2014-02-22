package tw.singtracy;

import android.app.Application;
import android.util.Log;

import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.Kii.Site;

public class SingTracy extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // initialize the Kii SDK!
        Kii.initialize("56636da7", "33c5ef875c7a0ba99960253192a807ca", Site.JP);
        Log.v("TEST", "Kii init");
    }
}