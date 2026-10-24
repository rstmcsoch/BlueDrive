package com.moorixlabs.bluedrive;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // English is the only supported application language.
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"));
    }
}
