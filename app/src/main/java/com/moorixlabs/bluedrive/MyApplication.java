package com.moorixlabs.bluedrive;

import android.app.Application;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        LocaleHelper.applyPersistedLocale(this);
    }
}