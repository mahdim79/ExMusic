package com.dust.exmusic.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.dust.exmusic.R;
import com.dust.exmusic.sharedpreferences.SharedPreferencesCenter;

import java.util.Locale;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        seExTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setUpEnglishLanguage();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        TextView textMain = (TextView) findViewById(R.id.textMain);
        TextView textRights = (TextView) findViewById(R.id.textRights);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setDuration(500);
        alphaAnimation.setFillAfter(true);
        textMain.startAnimation(alphaAnimation);

        alphaAnimation.setStartOffset(900);
        textRights.startAnimation(alphaAnimation);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 1500);

    }

    private void setUpEnglishLanguage() {
        String localeStr;
        if (new SharedPreferencesCenter(this).getEnglishLanguage())
            localeStr = "en";
        else
            localeStr = "fa";
        Locale locale = new Locale(localeStr);
        Locale.setDefault(locale);
        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void seExTheme() {
        if (new SharedPreferencesCenter(this).getDarkTheme())
            setTheme(R.style.Theme_DarkExMusic);
    }
}