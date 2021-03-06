package com.dust.exmusic.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.ViewPager;

import com.dust.exmusic.R;
import com.dust.exmusic.adapters.viewpagers.PlayerViewPagerAdapter;
import com.dust.exmusic.customviews.CTextView;
import com.dust.exmusic.dataclasses.MainDataClass;
import com.dust.exmusic.dataproviders.MetaDataLoader;
import com.dust.exmusic.fragments.others.MusicDetailsBottomDialog;
import com.dust.exmusic.interfaces.OnLoadPicture;
import com.dust.exmusic.realm.RealmHandler;
import com.dust.exmusic.services.PlayerService;
import com.dust.exmusic.sharedpreferences.SharedPreferencesCenter;
import com.gauravk.audiovisualizer.visualizer.CircleLineVisualizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerActivity extends AppCompatActivity {

    private ImageView playPauseButton;
    private ImageView fastRewindButton;
    private ImageView fastForwardButton;
    private ImageView RepeatButton;
    private ImageView transparentImage;
    private SeekBar musicSeekbar;
    private CTextView totalTime;
    private CTextView elapsedTime;
    private CTextView elapsed2;
    private CardView cardView1;
    private ViewPager playerViewPager;
    private ImageView othersImage;
    private ImageView addToFavImage;
    private ImageView shuffleButton;
    private CircleLineVisualizer circleLineVisualizer;

    private boolean firstEntry = true;

    private Handler handler;
    private Runnable runnable;

    private String currentPath;

    private OnMusicPlayerStateChanged onMusicPlayerStateChanged;
    private OnDataSynced onDataSynced;

    private Timer timer;
    private Timer timer1;

    private MediaPlayer mediaPlayer;

    private RealmHandler realmHandler;

    List<MainDataClass> list = new ArrayList<>();

    private final int ShuffleModeOn = 1;
    private final int ShuffleModeOff = 0;

    private final int REPEAT_OFF = 0;
    private final int REPEAT_ON = 1;
    private final int REPEAT_ONE = 2;

    private SharedPreferencesCenter sharedPreferencesCenter;

    private int RepeatMode = 0;
    private AlphaAnimation alphaAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        seExTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        setUpDataBase();
        setUpSharedPreferences();
        setUpAlphaAnimation();
        setUpViews();
        setUpList();
        setUpViewPager(list);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setUpService(getIntent().getExtras().getString("PATH"));
        setUpAddToFavoriteImage(getIntent().getExtras().getString("PATH"));
        setUpShuffleButton();
    }

    private void seExTheme() {
        if (new SharedPreferencesCenter(this).getDarkTheme())
            setTheme(R.style.Theme_DarkExMusic);
    }

    private void setUpShuffleButton() {
        setShuffleButtonInterface();
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!sharedPreferencesCenter.getShuffleMode().equals(""))
                    sharedPreferencesCenter.setShuffleMode("");
                else
                    sharedPreferencesCenter.setShuffleMode(sharedPreferencesCenter.getPlayPair());
                setShuffleButtonInterface();
            }
        });
    }

    private void setShuffleButtonInterface() {
        if (!sharedPreferencesCenter.getShuffleMode().equals(""))
            shuffleButton.setImageResource(R.drawable.gradient_shuffle_on);
        else
            shuffleButton.setImageResource(R.drawable.gradient_shuffle_off);
    }

    private Pair<String, String> getSeparatedShuffleMode() {
        String data = sharedPreferencesCenter.getShuffleMode();
        return new Pair<>(data.substring(0, data.indexOf('|')), data.substring(data.indexOf('|') + 1));
    }

    private Pair<String, String> getSeparatedListMode(String data) {
        return new Pair<>(data.substring(0, data.indexOf('|')), data.substring(data.indexOf('|') + 1));
    }

    private void setUpListByShuffle() {
        Pair<String, String> pair = getSeparatedShuffleMode();

        switch (pair.first) {
            case "PlayList":
                list.clear();
                list.addAll(realmHandler.getPlayListData(pair.second));
                break;
            case "Artists":
                list.clear();
                list.addAll(realmHandler.getArtistSongs(pair.second));
                break;
            case "Albums":
                list.clear();
                list.addAll(realmHandler.getAlbumSongs(pair.second));
                break;
            case "Folders":
                list.clear();
                list.addAll(realmHandler.getFoldersSong(pair.second));
                break;
            case "ALL":
                list.clear();
                list.addAll(realmHandler.getAllSortedMainData(sharedPreferencesCenter.getSortType()));
                break;
            case "FavoriteList":
                list.clear();
                String[] names = sharedPreferencesCenter.getFavoriteListPaths();
                for (int i = 0; i < names.length; i++)
                    list.add(realmHandler.getMusicDataByPath(names[i]));
                break;
        }
    }

    private void setUpListByType(String playList) {
        Pair<String, String> pair = getSeparatedListMode(playList);

        switch (pair.first) {
            case "ALL":
                sharedPreferencesCenter.setPlaylistActive("");
                list.addAll(realmHandler.getAllSortedMainData(sharedPreferencesCenter.getSortType()));
                break;
            case "PlayList":
                sharedPreferencesCenter.setPlaylistActive(pair.second);
                list.addAll(realmHandler.getPlayListData(pair.second));
                break;
            case "FavoriteList":
                sharedPreferencesCenter.setPlaylistActive("");
                String[] favPaths = sharedPreferencesCenter.getFavoriteListPaths();
                for (int i = 0; i < favPaths.length; i++)
                    list.add(realmHandler.getMusicDataByPath(favPaths[i]));
                break;
            case "Artists":
                sharedPreferencesCenter.setPlaylistActive("");
                list.addAll(realmHandler.getArtistSongs(pair.second));
                break;
            case "Albums":
                sharedPreferencesCenter.setPlaylistActive("");
                list.addAll(realmHandler.getAlbumSongs(pair.second));
                break;
            case "Folders":
                sharedPreferencesCenter.setPlaylistActive("");
                list.addAll(realmHandler.getFoldersSong(pair.second));
                break;
        }
    }

    private void setUpList() {

        if (getIntent().getExtras().containsKey("SERVICE_EXTRA")) {

            if (!sharedPreferencesCenter.getShuffleMode().equals("")) {
                setUpListByShuffle();
            } else if (!sharedPreferencesCenter.getPlayPair().equals("")) {
                setUpListByType(sharedPreferencesCenter.getPlayPair());
            } else {
                setUpListByType("ALL|ALL");
            }

        } else if (getIntent().getExtras().containsKey("SHUFFLE_MODE")) {
            sharedPreferencesCenter.setPlayPair(getIntent().getExtras().getString("PLAY_LIST"));
            setUpListByShuffle();

        } else {

            if (getIntent().getExtras().containsKey("PLAY_LIST")) {
                sharedPreferencesCenter.setPlayPair(getIntent().getExtras().getString("PLAY_LIST"));
                setUpListByType(getIntent().getExtras().getString("PLAY_LIST"));
            }

        }
        if (!sharedPreferencesCenter.getShuffleMode().equals(""))
            sharedPreferencesCenter.setShuffleMode(sharedPreferencesCenter.getPlayPair());
    }

    private void setUpAlphaAnimation() {
        alphaAnimation = new AlphaAnimation(0.2f, 1f);
        alphaAnimation.setDuration(300);
        alphaAnimation.setFillAfter(true);
    }

    private void setUpAddToFavoriteImage(String path) {
        boolean favAvailability = sharedPreferencesCenter.checkFavoriteListPathAvailability(path);
        if (favAvailability) {
            addToFavImage.setImageResource(R.drawable.gradient_added_favlist);
        } else {
            addToFavImage.setImageResource(R.drawable.gradient_add_to_favlist);
        }
        addToFavImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sharedPreferencesCenter.checkFavoriteListPathAvailability(path)) {
                    sharedPreferencesCenter.removeFavoriteList(path);
                    addToFavImage.setImageResource(R.drawable.gradient_add_to_favlist);
                } else {
                    sharedPreferencesCenter.addToFavoriteListPaths(path);
                    addToFavImage.setImageResource(R.drawable.gradient_added_favlist);
                }
            }
        });
    }

    private void setUpDataBase() {
        realmHandler = new RealmHandler();
    }

    private void setUpViewPager(List<MainDataClass> dataClasses) {

        playerViewPager.setAdapter(new PlayerViewPagerAdapter(getSupportFragmentManager(), dataClasses));
        for (int i = 0; i < dataClasses.size(); i++) {
            if (dataClasses.get(i).getPath().equals(getIntent().getExtras().getString("PATH")))
                playerViewPager.setCurrentItem(i);
        }
        playerViewPager.setOffscreenPageLimit(0);
        currentPath = getIntent().getExtras().getString("PATH");
        playerViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (handler == null)
                    handler = new Handler();
                else
                    handler.removeCallbacks(runnable);

                runnable = new Runnable() {
                    @Override
                    public void run() {
                        setUpService(dataClasses.get(position).getPath());
                        currentPath = dataClasses.get(position).getPath();
                        setUpAddToFavoriteImage(currentPath);
                        new MetaDataLoader(PlayerActivity.this).getPicture(currentPath, new OnLoadPicture() {
                            @Override
                            public void onGetPicture(Bitmap bitmap) {
                                if (bitmap != null)
                                    transparentImage.setImageBitmap(bitmap);
                                else
                                    transparentImage.setImageResource(R.drawable.empty_music_pic);
                                transparentImage.startAnimation(alphaAnimation);

                            }
                        });
                        handler = null;
                    }
                };

                handler.postDelayed(runnable, 400);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void setUpSharedPreferences() {
        sharedPreferencesCenter = new SharedPreferencesCenter(this);
    }

    private void setUpService(String path) {

        try {
            Intent intent = new Intent(this, PlayerService.class);
            intent.setAction("com.dust.exmusic.ACTION_RESET");
            intent.putExtra("PATH", path);
            startService(intent);
        } catch (Exception e) {
        }

        bindMediaPlayerService(path);
    }

    private void bindMediaPlayerService(String path) {
        Intent intent = new Intent(this, PlayerService.class);
        intent.putExtra("PATH", path);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                PlayerService.MyBinder binder = (PlayerService.MyBinder) iBinder;
                PlayerService playerService = binder.getService();
                PlayerActivity.this.mediaPlayer = playerService.mediaPlayer;
                setUpVisualizer(mediaPlayer.getAudioSessionId());
                setUpMediaController();
                if (mediaPlayer.getCurrentPosition() > 0) {
                    totalTime.setText(calculateTimer(mediaPlayer.getDuration()));
                    if (mediaPlayer.isPlaying()) {
                        playPauseButton.setImageResource(R.drawable.gradient_pause);
                    } else {
                        playPauseButton.setImageResource(R.drawable.gradient_play);
                    }
                } else {
                    playPauseButton.setImageResource(R.drawable.gradient_pause);
                    totalTime.setText(calculateTimer(mediaPlayer.getDuration()));
                }
                enableMediaController();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        }, BIND_AUTO_CREATE);
    }

    private void setUpViews() {
        circleLineVisualizer = (CircleLineVisualizer) findViewById(R.id.visualizer);
        playPauseButton = (ImageView) findViewById(R.id.playPauseButton);
        playerViewPager = (ViewPager) findViewById(R.id.playerViewPager);
        fastRewindButton = (ImageView) findViewById(R.id.fastRewindButton);
        fastForwardButton = (ImageView) findViewById(R.id.fastForwardButton);
        RepeatButton = (ImageView) findViewById(R.id.RepeatButton);
        transparentImage = (ImageView) findViewById(R.id.transparentImage);
        elapsedTime = (CTextView) findViewById(R.id.elapsedTime);
        elapsed2 = (CTextView) findViewById(R.id.elapsed2);
        totalTime = (CTextView) findViewById(R.id.totalTime);
        musicSeekbar = (SeekBar) findViewById(R.id.musicSeekbar);
        cardView1 = (CardView) findViewById(R.id.cardView1);
        othersImage = (ImageView) findViewById(R.id.othersImage);
        addToFavImage = (ImageView) findViewById(R.id.addToFavImage);
        shuffleButton = (ImageView) findViewById(R.id.shuffleButton);

        musicSeekbar.setEnabled(false);

        new MetaDataLoader(PlayerActivity.this).getPicture(getIntent().getExtras().getString("PATH"), new OnLoadPicture() {
            @Override
            public void onGetPicture(Bitmap bitmap) {
                if (bitmap != null) {
                    transparentImage.setImageBitmap(bitmap);

                }
            }
        });

        ImageView backImg = (ImageView) findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        othersImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicDetailsBottomDialog dialog = new MusicDetailsBottomDialog(currentPath);
                dialog.show(getSupportFragmentManager(), "null");
            }
        });

    }

    private void setUpMediaController() {
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PlayerActivity.this, PlayerService.class);
                intent.setAction("com.dust.exmusic.ACTION_PLAY");
                startService(intent);
                if (mediaPlayer.isPlaying()) {
                    playPauseButton.setImageResource(R.drawable.gradient_play);
                } else {
                    playPauseButton.setImageResource(R.drawable.gradient_pause);
                }
            }
        });

        fastForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PlayerActivity.this, PlayerService.class);
                intent.setAction("com.dust.exmusic.ACTION_FORWARD");
                startService(intent);
            }
        });

        fastRewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PlayerActivity.this, PlayerService.class);
                intent.setAction("com.dust.exmusic.ACTION_REWIND");
                startService(intent);
            }
        });

        RepeatMode = sharedPreferencesCenter.getRepeatMode();

        switch (RepeatMode) {
            case REPEAT_OFF:
                RepeatButton.setImageResource(R.drawable.gradient_repeat_off);
                break;
            case REPEAT_ON:
                RepeatButton.setImageResource(R.drawable.gradient_repeat_on);
                break;
            case REPEAT_ONE:
                RepeatButton.setImageResource(R.drawable.gradient_repeat_one);
                break;

        }

        RepeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (RepeatMode == REPEAT_OFF) {
                    RepeatMode = REPEAT_ON;
                    sharedPreferencesCenter.setRepeatMode(RepeatMode);
                    RepeatButton.setImageResource(R.drawable.gradient_repeat_on);
                } else if (RepeatMode == REPEAT_ON) {
                    RepeatButton.setImageResource(R.drawable.gradient_repeat_one);
                    RepeatMode = REPEAT_ONE;
                    sharedPreferencesCenter.setRepeatMode(RepeatMode);
                } else {
                    RepeatButton.setImageResource(R.drawable.gradient_repeat_off);
                    RepeatMode = REPEAT_OFF;
                    sharedPreferencesCenter.setRepeatMode(RepeatMode);
                }
            }
        });

        setUpSeekBar();

    }


    private void setUpSeekBar() {

        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setDuration(500);
        alphaAnimation.setFillAfter(true);

        AlphaAnimation alphaAnimation2 = new AlphaAnimation(1f, 0f);
        alphaAnimation2.setDuration(500);
        alphaAnimation2.setFillAfter(true);

        musicSeekbar.setMax(mediaPlayer.getDuration());
        musicSeekbar.setEnabled(true);
        musicSeekbar.setProgress(0);
        if (timer != null) {
            timer.purge();
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new MainTimerTask(), 0, 500);

        musicSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                cardView1.setVisibility(View.VISIBLE);
                cardView1.startAnimation(alphaAnimation);
                if (timer != null) {
                    timer.purge();
                    timer.cancel();
                }
                elapsedTime.setText(calculateTimer(seekBar.getProgress()));
                timer1 = new Timer();
                timer1.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                elapsed2.setText(calculateTimer(seekBar.getProgress()));
                                elapsedTime.setText(calculateTimer(seekBar.getProgress()));
                            }
                        });
                    }
                }, 0, 20);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                cardView1.startAnimation(alphaAnimation2);

                try {
                    mediaPlayer.seekTo(seekBar.getProgress());
                    timer = new Timer();
                    timer.schedule(new MainTimerTask(), 0, 500);
                } catch (Exception e) {
                    if (timer != null) {
                        timer.purge();
                        timer.cancel();
                    }
                }

                if (timer1 != null) {
                    timer1.purge();
                    timer1.cancel();
                }

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        onMusicPlayerStateChanged = new OnMusicPlayerStateChanged();
        onDataSynced = new OnDataSynced();
        registerReceiver(onMusicPlayerStateChanged, new IntentFilter("com.dust.exmusic.OnMusicPlayerStateChanged"));
        registerReceiver(onDataSynced, new IntentFilter("com.dust.exmusic.OnDataSynced"));
        if (timer != null) {
            timer = new Timer();
            timer.schedule(new MainTimerTask(), 0, 500);
        }

    }

    @Override
    protected void onStop() {
        unregisterReceiver(onMusicPlayerStateChanged);
        unregisterReceiver(onDataSynced);
        if (timer != null) {
            timer.purge();
            timer.cancel();
        }

        if (timer1 != null) {
            timer1.purge();
            timer1.cancel();
        }
        super.onStop();
    }

    private class MainTimerTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        musicSeekbar.setProgress(mediaPlayer.getCurrentPosition());
                        elapsedTime.setText(calculateTimer(mediaPlayer.getCurrentPosition()));
                    } catch (Exception e) {
                    }
                }
            });
        }
    }

    private String calculateTimer(long millis) {
        int second = (int) (millis / 1000);
        int minute = second / 60;
        second %= 60;
        return String.format(Locale.ENGLISH, "%02d", minute) + ":" + String.format(Locale.ENGLISH, "%02d", second);
    }

    public class OnDataSynced extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null && intent.getExtras().containsKey("COMPLETION")) {
                disableMediaController();
                if (RepeatMode == REPEAT_OFF) {
                    musicSeekbar.setProgress(0);
                    elapsedTime.setText(calculateTimer(0));
                    playPauseButton.setImageResource(R.drawable.gradient_play);
                }
            } else {
                currentPath = intent.getExtras().getString("PATH");
                bindMediaPlayerService(currentPath);
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getPath().equals(currentPath))
                        playerViewPager.setCurrentItem(i);
                }
            }
        }
    }

    private void disableMediaController() {

        fastRewindButton.setEnabled(false);
        fastForwardButton.setEnabled(false);
        playPauseButton.setEnabled(false);
        musicSeekbar.setEnabled(false);
    }

    private void enableMediaController() {
        fastRewindButton.setEnabled(true);
        fastForwardButton.setEnabled(true);
        playPauseButton.setEnabled(true);
        musicSeekbar.setEnabled(true);
    }

    public class OnMusicPlayerStateChanged extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (mediaPlayer.isPlaying()) {
                    playPauseButton.setImageResource(R.drawable.gradient_pause);
                } else {
                    playPauseButton.setImageResource(R.drawable.gradient_play);
                }
            } catch (Exception e) {

            }
        }
    }

    @Override
    protected void onDestroy() {
        releaseVisualizer();
        super.onDestroy();
    }

    private void releaseVisualizer() {
        if (circleLineVisualizer != null)
            circleLineVisualizer.release();
    }

    private void setUpVisualizer(int sessionId) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (sessionId != -1)
                    circleLineVisualizer.setAudioSessionId(sessionId);
            }
        }, 200);
    }
}