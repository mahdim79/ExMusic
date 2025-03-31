package com.dust.exmusic.activities;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.dust.exmusic.BuildConfig;
import com.dust.exmusic.R;
import com.dust.exmusic.adapters.viewpagers.MainViewPagerAdapter;
import com.dust.exmusic.customviews.CTextView;
import com.dust.exmusic.dataproviders.MetaDataLoader;
import com.dust.exmusic.fragments.navigationviewfragments.main.SettingsFragment;
import com.dust.exmusic.fragments.navigationviewfragments.main.WholeAlbumsFragment;
import com.dust.exmusic.fragments.navigationviewfragments.main.WholeArtistsFragment;
import com.dust.exmusic.fragments.navigationviewfragments.main.WholeFavoriteListFragment;
import com.dust.exmusic.fragments.navigationviewfragments.main.WholeFolderFragment;
import com.dust.exmusic.fragments.navigationviewfragments.main.WholePlayListFragment;
import com.dust.exmusic.fragments.searchfragments.MainSearchFragment;
import com.dust.exmusic.interfaces.OnLoadPicture;
import com.dust.exmusic.realm.RealmHandler;
import com.dust.exmusic.services.PlayerService;
import com.dust.exmusic.sharedpreferences.SharedPreferencesCenter;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private DrawerLayout drawerLayout;
    private LinearLayout drawer;
    private Toolbar toolbar;
    private CoordinatorLayout coordinator;
    private ViewPager viewPager;
    private TabLayout tableLayout;
    private AppBarLayout appbarLayout;
    private ImageView imgSearch;
    private ImageView circle_center;
    private CardView cardView1;
    private CTextView version;
    private FloatingActionButton floatingButton;
    private OnFavoriteListChanged onFavoriteListChanged;

    private LinearLayout settings_Linear;
    private LinearLayout folders_Linear;
    private LinearLayout albums_Linear;
    private LinearLayout artists_Linear;
    private LinearLayout favoriteList_Linear;
    private LinearLayout playList_Linear;
    private OnUnlockDrawer onUnlockDrawer;
    private OnReceivePath onReceivePath;

    private final int ALL_SHUFFLE_TYPE = 1;
    private final int FAV_SHUFFLE_TYPE = 1;

    private RealmHandler handler;

    private int ShuffleFloatingType = ALL_SHUFFLE_TYPE;

    private SharedPreferencesCenter sharedPreferencesCenter;

    private int allDataCount = 0;
    private int favDataCount = 0;

    private ActivityResultLauncher<String[]> externalStorageLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @Override
        public void onActivityResult(Map<String, Boolean> result) {
            checkPermissions();
        }
    });

    private ActivityResultLauncher<String> externalRecordAudio = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {}
    });

    private ActivityResultLauncher<Intent> externalStorageLauncherS = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            checkPermissions();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        seExTheme();
        setUpEnglishLanguage();
        super.onCreate(savedInstanceState);
        adjustFontScale();
        setContentView(R.layout.activity_main);
        checkPermissions();
        setUpSharedPreferences();
        setUpRealmHandler();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setUpViews();
        setUpSearchImg();
        setPrimaryImportantData();
        setUpFloatingActionButton();
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {

            }
        },2000);
    }

    private void adjustFontScale() {
        Configuration configuration = getResources().getConfiguration();
        if (configuration.fontScale != 1.0f) {
            configuration.fontScale = 1.0f;
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(metrics);
            metrics.scaledDensity = configuration.fontScale * metrics.density;
            getBaseContext().getResources().updateConfiguration(configuration, metrics);
        }
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

    private void setUpRealmHandler() {
        handler = new RealmHandler();
    }

    private void setPrimaryImportantData() {
        allDataCount = handler.getAllDataCount();
        String[] favData = sharedPreferencesCenter.getFavoriteListPaths();
        if (!favData[0].equals(""))
            favDataCount = favData.length;
    }

    private void optimizeFloatingActionButton(int position) {
        switch (position) {
            case 0:
                ShuffleFloatingType = ALL_SHUFFLE_TYPE;
                if (allDataCount == 0)
                    floatingButton.setVisibility(View.GONE);
                else
                    floatingButton.setVisibility(View.VISIBLE);
                break;
            case 2:
                ShuffleFloatingType = FAV_SHUFFLE_TYPE;
                if (favDataCount == 0)
                    floatingButton.setVisibility(View.GONE);
                else
                    floatingButton.setVisibility(View.VISIBLE);
                break;
            default:
                floatingButton.setVisibility(View.GONE);
                break;
        }
    }

    private String getJoinedShuffleMode(String Type, String name) {
        return Type + "|" + name;
    }

    private void setUpFloatingActionButton() {
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferencesCenter.setPlaylistActive("");
                Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                if (viewPager.getCurrentItem() == 0) {
                    sharedPreferencesCenter.setShuffleMode(getJoinedShuffleMode("ALL", "ALL"));
                    intent.putExtra("PATH", handler.getMainData(1, sharedPreferencesCenter.getSortType()).get(0).getPath());
                    intent.putExtra("SHUFFLE_MODE", "ALL");
                    intent.putExtra("PLAY_LIST", "ALL|ALL");
                } else {
                    sharedPreferencesCenter.setShuffleMode(getJoinedShuffleMode("FavoriteList", "FavoriteList"));
                    intent.putExtra("PATH", handler.getMusicDataByPath(sharedPreferencesCenter.getFavoriteListPaths()[0]).getPath());
                    intent.putExtra("PLAY_LIST", "FavoriteList|FavoriteList");
                }
                startActivity(intent);
            }
        });
    }

    private void setUpSearchImg() {
        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.drawerLayout, new MainSearchFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack("MainSearchFragment")
                        .commit();
            }
        });
    }

    private void setUpSharedPreferences() {
        sharedPreferencesCenter = new SharedPreferencesCenter(this);
    }

    private boolean checkServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (runningServiceInfo.service.getClassName().equals(PlayerService.class.getName()))
                return true;
        }
        return false;
    }

    private void setUpSmallCircle() {
        if (checkServiceRunning()) {
            Intent intent = new Intent(this, PlayerService.class);
            intent.setAction("com.dust.exmusic.ACTION_SEND_PATH");
            startService(intent);
        } else {
            cardView1.setVisibility(View.GONE);
        }
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            if (!Environment.isExternalStorageManager()){
                Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                permissionIntent.setData(Uri.fromParts("package", getPackageName(), null));
                externalStorageLauncherS.launch(permissionIntent);
                return;
            }
        }else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                externalStorageLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE});
                return;
            }
        }

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            externalRecordAudio.launch(Manifest.permission.RECORD_AUDIO);

    }

    private void setUpViews() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        version = (CTextView) findViewById(R.id.version);
        floatingButton = (FloatingActionButton) findViewById(R.id.floatingButton);
        appbarLayout = (AppBarLayout) findViewById(R.id.appbarLayout);
        imgSearch = (ImageView) findViewById(R.id.imgSearch);
        circle_center = (ImageView) findViewById(R.id.circle_center);
        drawer = (LinearLayout) findViewById(R.id.drawer);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        coordinator = (CoordinatorLayout) findViewById(R.id.coordinator);
        cardView1 = (CardView) findViewById(R.id.cardView1);

        settings_Linear = (LinearLayout) findViewById(R.id.settings_Linear);
        folders_Linear = (LinearLayout) findViewById(R.id.folders_Linear);
        albums_Linear = (LinearLayout) findViewById(R.id.albums_Linear);
        artists_Linear = (LinearLayout) findViewById(R.id.artists_Linear);
        favoriteList_Linear = (LinearLayout) findViewById(R.id.favoriteList_Linear);
        playList_Linear = (LinearLayout) findViewById(R.id.playList_Linear);

        settings_Linear.setOnClickListener(this);
        folders_Linear.setOnClickListener(this);
        albums_Linear.setOnClickListener(this);
        artists_Linear.setOnClickListener(this);
        favoriteList_Linear.setOnClickListener(this);
        playList_Linear.setOnClickListener(this);

        if (sharedPreferencesCenter.getEnglishLanguage())
            version.setTypeface(null);
        version.setText(getResources().getString(R.string.version, BuildConfig.VERSION_NAME));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, 0, 0);
        toggle.syncState();
        drawer.bringToFront();

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blue));

        toolbar.setNavigationIcon(R.drawable.gradient_humberger);

        drawerLayout.addDrawerListener(toggle);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawerLayout.isDrawerOpen(drawer))
                    drawerLayout.closeDrawers();
                else
                    drawerLayout.openDrawer(drawer);

            }
        });
        setTitle("");

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tableLayout = (TabLayout) findViewById(R.id.round_tabs);
        viewPager.setAdapter(new MainViewPagerAdapter(getSupportFragmentManager(), this));
        viewPager.setOffscreenPageLimit(3);
        tableLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                optimizeFloatingActionButton(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {

            if (grantResults[0] != -1 && grantResults[1] != -1) {
                recreate();
            } else {
                Toast.makeText(this, getResources().getString(R.string.permissionDenied), Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public void onClick(View view) {
        drawerLayout.closeDrawer(drawer, false);
        switch (view.getId()) {
            case R.id.settings_Linear:
                startFragment(new SettingsFragment(), "SettingsFragment");
                break;
            case R.id.folders_Linear:
                startFragment(new WholeFolderFragment(), "WholeFolderFragment");
                break;
            case R.id.albums_Linear:
                startFragment(new WholeAlbumsFragment(), "WholeAlbumsFragment");
                break;
            case R.id.artists_Linear:
                startFragment(new WholeArtistsFragment(), "WholeArtistsFragment");
                break;
            case R.id.favoriteList_Linear:
                startFragment(new WholeFavoriteListFragment(), "WholeFavoriteListFragment");
                break;
            case R.id.playList_Linear:
                startFragment(new WholePlayListFragment(), "WholePlayListFragment");
                break;
        }
    }

    public void startFragment(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction().replace(R.id.drawerLayout, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(tag)
                .commit();
    }

    public class OnUnlockDrawer extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // setting a delay in order to prevent transition destroy
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (intent.getExtras() != null && intent.getExtras().containsKey("LOCK"))
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    else
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                }
            }, 200);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        onUnlockDrawer = new OnUnlockDrawer();
        onReceivePath = new OnReceivePath();
        onFavoriteListChanged = new OnFavoriteListChanged();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            registerReceiver(onFavoriteListChanged, new IntentFilter("com.dust.exmusic.OnFavoriteListChanged"),RECEIVER_EXPORTED);
            registerReceiver(onUnlockDrawer, new IntentFilter("com.dust.exmusic.UNLOCK_MAIN_DRAWER"),RECEIVER_EXPORTED);
            registerReceiver(onReceivePath, new IntentFilter("com.dust.exmusic.OnReceivePath"),RECEIVER_EXPORTED);
        }else {
            registerReceiver(onFavoriteListChanged, new IntentFilter("com.dust.exmusic.OnFavoriteListChanged"));
            registerReceiver(onUnlockDrawer, new IntentFilter("com.dust.exmusic.UNLOCK_MAIN_DRAWER"));
            registerReceiver(onReceivePath, new IntentFilter("com.dust.exmusic.OnReceivePath"));
        }

        setUpSmallCircle();
        sendBroadcast(new Intent("com.dust.exmusic.OnFavoriteListChanged"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(onUnlockDrawer);
        unregisterReceiver(onReceivePath);
        unregisterReceiver(onFavoriteListChanged);
    }

    public class OnReceivePath extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras().getString("PATH") == null)
                cardView1.setVisibility(View.GONE);
            else
                cardView1.setVisibility(View.VISIBLE);

            if (!intent.getExtras().getBoolean("IS_PLAYING")) {
                AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0.5f);
                alphaAnimation.setDuration(500);
                alphaAnimation.setRepeatCount(Animation.INFINITE);
                alphaAnimation.setRepeatMode(Animation.REVERSE);
                cardView1.startAnimation(alphaAnimation);
            } else {
                cardView1.clearAnimation();
            }
            new MetaDataLoader(MainActivity.this).getPicture(intent.getExtras().getString("PATH"), new OnLoadPicture() {
                @Override
                public void onGetPicture(Bitmap bitmap) {
                    if (bitmap != null) {
                        circle_center.setImageBitmap(bitmap);
                    } else {
                        circle_center.setImageResource(R.drawable.empty_music_pic);
                    }
                }
            });

            cardView1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent1 = new Intent(MainActivity.this, PlayerActivity.class);
                    intent1.putExtra("PLAY_LIST", sharedPreferencesCenter.getPlayPair());
                    intent1.putExtra("PATH", intent.getExtras().getString("PATH"));
                    startActivity(intent1);
                }
            });

        }
    }

    private class OnFavoriteListChanged extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String[] favData = sharedPreferencesCenter.getFavoriteListPaths();
                if (!favData[0].equals(""))
                    favDataCount = favData.length;
                else
                    favDataCount = 0;
                optimizeFloatingActionButton(viewPager.getCurrentItem());
            } catch (Exception e) {
            }
        }
    }

}