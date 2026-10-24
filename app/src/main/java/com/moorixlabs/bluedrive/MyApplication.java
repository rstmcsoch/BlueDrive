package com.moorixlabs.bluedrive;
import android.app.Application;
import android.content.Context;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
public class MyApplication extends Application {
 public static void applyTheme(Context c) { int value=c.getSharedPreferences("appearance",0).getInt("theme",0); AppCompatDelegate.setDefaultNightMode(value==1?AppCompatDelegate.MODE_NIGHT_NO:value==2?AppCompatDelegate.MODE_NIGHT_YES:AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM); }
 @Override public void onCreate(){super.onCreate();applyTheme(this);String locale=getSharedPreferences("appearance",0).getString("locale","");if(!locale.isEmpty())AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(locale));SettingsUtils.migrate(this);}
}