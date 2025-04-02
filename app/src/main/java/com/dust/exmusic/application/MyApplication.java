package com.dust.exmusic.application;

import android.app.Application;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        initAppCenter();
        setUpTypeFace();
        initializeRealm();
        super.onCreate();
    }

    private void initAppCenter() {
        AppCenter.start(this, "cc4124d0-163a-411c-a559-fe3bb022e504",
                Analytics.class, Crashes.class);
    }

    private void initializeRealm() {
        Realm.init(this);
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name("MainDB")
                .schemaVersion(1)
                .allowWritesOnUiThread(true)
                .build();
        Realm.setDefaultConfiguration(configuration);
    }

    public Typeface setUpTypeFace() {
        return Typeface.createFromAsset(this.getAssets(), "fonts/far_mitra.ttf");
    }

}
