package com.blueberryjoy.history;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

public class App extends Application {
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
    }

    @Override
    protected void attachBaseContext(@NonNull Context base) {
        super.attachBaseContext(base);
    }

    public static Context getAppContext() {
        return appContext;
    }
}
