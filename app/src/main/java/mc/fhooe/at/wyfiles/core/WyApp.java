package mc.fhooe.at.wyfiles.core;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

import mc.fhooe.at.wyfiles.dagger.AppComponent;
import mc.fhooe.at.wyfiles.dagger.AppModule;
import mc.fhooe.at.wyfiles.dagger.DaggerAppComponent;

/**
 * @author Martin Macheiner
 *         Date: 18.12.2016.
 */

public class WyApp extends Application {

    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        JodaTimeAndroid.init(this);

        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

}
