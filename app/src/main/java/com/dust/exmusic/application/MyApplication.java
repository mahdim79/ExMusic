package com.dust.exmusic.application;

import android.app.Application;
import android.graphics.Typeface;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        setUpTypeFace();
        initializeRealm();
        super.onCreate();
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
