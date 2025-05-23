package com.dust.exmusic.services;

import static android.app.Notification.PRIORITY_LOW;
import static android.app.Notification.PRIORITY_MIN;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.util.Pair;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.dust.exmusic.R;
import com.dust.exmusic.activities.MainActivity;
import com.dust.exmusic.dataclasses.MainDataClass;
import com.dust.exmusic.dataproviders.MetaDataLoader;
import com.dust.exmusic.interfaces.OnLoadPicture;
import com.dust.exmusic.realm.ExternalRealmHandler;
import com.dust.exmusic.realm.MainObject;
import com.dust.exmusic.sharedpreferences.SharedPreferencesCenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmResults;

public class PlayerService extends Service {

    private final int REPEAT_OFF = 0;
    private final int REPEAT_ON = 1;
    private final int REPEAT_ONE = 2;

    private final int TYPE_PLAY = 3;

    private Timer notificationUpdaterTimer;

    private final int TYPE_PAUSE = 4;

    private ExternalRealmHandler externalRealmHandler = new ExternalRealmHandler(Realm.getDefaultInstance());

    private String path;

    private int RepeatMode = 0;

    private int lastNotificationType = TYPE_PAUSE;

    private PowerManager.WakeLock cpuWakeLock;

    private SharedPreferencesCenter sharedPreferencesCenter;

    private MediaSessionCompat mediaSessionCompat;

    public MediaPlayer mediaPlayer;

    public PlayerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        acquireCpuWakeLock();
        notificationUpdaterTimer = new Timer("notificationtimer");
        notificationUpdaterTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (lastNotificationType != TYPE_PAUSE)
                    showNotification(lastNotificationType);
            }
        }, 0, 1000);
        mediaSessionCompat = new MediaSessionCompat(this, "media_session_main");
        mediaSessionCompat.setActive(true);
        mediaSessionCompat.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onSeekTo(long pos) {
                mediaPlayer.seekTo((int) pos);
                super.onSeekTo(pos);
            }


            @Override
            public void onPlay() {
                super.onPlay();
                try {
                    Intent intentPlay = new Intent(PlayerService.this, PlayerService.class);
                    intentPlay.setAction("com.dust.exmusic.ACTION_PLAY");
                    PendingIntent.getService(PlayerService.this, 103, intentPlay, PendingIntent.FLAG_IMMUTABLE).send();
                } catch (Exception e) {
                }
            }

            @Override
            public void onPause() {
                super.onPause();
                try {
                    Intent intentPlay = new Intent(PlayerService.this, PlayerService.class);
                    intentPlay.setAction("com.dust.exmusic.ACTION_PLAY");
                    PendingIntent.getService(PlayerService.this, 103, intentPlay, PendingIntent.FLAG_IMMUTABLE).send();
                } catch (Exception e) {
                }
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                goToNextMusic();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                goToPrevMusic();
            }
        });
    }

    private void acquireCpuWakeLock() {
        cpuWakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,PlayerService.class.getSimpleName());
        cpuWakeLock.acquire();
    }

    private void goToNextMusic() {
        try{
            String nextPath = getPreviousAndNextPath(path).second;
            if (nextPath != null) {
                initMediaPlayer(nextPath, false);
            }
        }catch (Exception e){}
    }

    private void goToPrevMusic() {
        try{
            String previousPath = getPreviousAndNextPath(path).first;
            if (previousPath != null) {
                initMediaPlayer(previousPath, false);
            }
        }catch (Exception e){}
    }

    private void initMediaPlayer(String path, boolean offMode) {
        this.path = path;
        Log.i("initMediaPlayerFun", "init");
        try {
            try {
                mediaPlayer.setOnCompletionListener(null);
                mediaPlayer.stop();
                mediaPlayer.reset();
            } catch (Exception e) {
            }
            if (mediaPlayer == null)
                mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(path);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                    Log.i("initMediaPlayer", "start()");
                    if (offMode) {
                        mediaPlayer.pause();
                        showNotification(TYPE_PAUSE);
                    } else {
                        showNotification(TYPE_PLAY);
                    }
                    Intent intent = new Intent("com.dust.exmusic.OnDataSynced");
                    intent.putExtra("PATH", path);
                    sendBroadcast(intent);
                    sendBroadcast(new Intent("com.dust.exmusic.OnMusicPlayerStateChanged"));
                    sendPath();
                }
            });

            Log.i("initMediaPlayerFun", "setOnCompletionListener init");
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {

                    if (mediaPlayer.getDuration() > 0 && ((mediaPlayer.getCurrentPosition() / 1000) >= (mediaPlayer.getDuration() / 1000) || ((mediaPlayer.getCurrentPosition() + 1000) / 1000) == (mediaPlayer.getDuration() / 1000))) {
                        Log.i("initMediaPlayerFun", "on completion call current = " + mediaPlayer.getCurrentPosition() + " | " + mediaPlayer.getDuration());

                        Intent intent = new Intent("com.dust.exmusic.OnDataSynced");
                        intent.putExtra("COMPLETION", 1);
                        sendBroadcast(intent);

                        if (!sharedPreferencesCenter.getPlaylistActive().equals("") && sharedPreferencesCenter.getShuffleMode().equals("")) {
                            List<MainDataClass> datas = getPlayListData(sharedPreferencesCenter.getPlaylistActive());
                            for (int i = 0; i < datas.size(); i++) {
                                if (datas.get(i).getPath().equals(path)) {

                                    if (i == datas.size() - 1) {
                                        switch (sharedPreferencesCenter.getRepeatMode()) {
                                            case REPEAT_OFF:
                                                initMediaPlayer(datas.get(0).getPath(), true);
                                                PlayerService.this.path = datas.get(0).getPath();
                                                break;
                                            case REPEAT_ON:
                                                initMediaPlayer(datas.get(0).getPath(), false);
                                                PlayerService.this.path = datas.get(0).getPath();
                                                break;
                                            case REPEAT_ONE:
                                                initMediaPlayer(datas.get(i).getPath(), false);
                                                PlayerService.this.path = datas.get(i).getPath();
                                                break;
                                        }
                                    } else {
                                        initMediaPlayer(datas.get(i + 1).getPath(), false);
                                        PlayerService.this.path = datas.get(i + 1).getPath();
                                    }
                                    stopForeground(true);
                                }
                            }
                        } else {
                            if (!sharedPreferencesCenter.getShuffleMode().equals("")) {
                                List<MainDataClass> list = new ArrayList<>();
                                Pair<String, String> pair = getSeparatedShuffleMode();
                                switch (pair.first) {
                                    case "Artists":
                                        list.addAll(externalRealmHandler.getArtistSongs(pair.second));
                                        break;
                                    case "Albums":
                                        list.addAll(externalRealmHandler.getAlbumSongs(pair.second));
                                        break;
                                    case "Folders":
                                        list.addAll(externalRealmHandler.getFoldersSong(pair.second));
                                        break;
                                    case "ALL":
                                        list.addAll(externalRealmHandler.getAllSortedMainData(sharedPreferencesCenter.getSortType()));
                                        break;
                                    case "PlayList":
                                        list.addAll(externalRealmHandler.getPlayListData(pair.second));
                                        break;
                                    case "FavoriteList":
                                        String[] names = sharedPreferencesCenter.getFavoriteListPaths();
                                        if (!names[0].equals(""))
                                            for (int i = 0; i < names.length; i++)
                                                list.add(externalRealmHandler.getMusicDataByPath(names[i]));

                                        break;
                                }

                                int index = 0;

                                switch (list.size()) {
                                    case 1:
                                        index = 0;
                                        break;
                                    case 2:
                                        for (int i = 0; i < list.size(); i++) {
                                            if (!list.get(i).getPath().equals(path))
                                                index = i;
                                        }
                                        break;
                                    default:
                                        if (!list.isEmpty()) {
                                            do {
                                                index = getRandomNumber(0, list.size());
                                            } while (list.get(index).getPath().equals(path));
                                        }
                                        break;
                                }

                                if (list.isEmpty()) {
                                    sharedPreferencesCenter.setShuffleMode("");
                                    initMediaPlayer(path, true);
                                } else {
                                    initMediaPlayer(list.get(index).getPath(), false);
                                    PlayerService.this.path = list.get(index).getPath();
                                }
                                stopForeground(true);
                            } else {
                                RepeatMode = sharedPreferencesCenter.getRepeatMode();
                                if (RepeatMode == REPEAT_ONE) {
                                    Log.i("initMediaPlayerFun", "on completion call REPEAT_ONE");
                                    initMediaPlayer(path, false);
                                } else if (RepeatMode == REPEAT_OFF) {
                                    initMediaPlayer(path, true);
                                    showNotification(TYPE_PAUSE);
                                } else {
                                    Log.i("initMediaPlayerFun", "on completion call ELSE");
                                    setRepeat();
                                }
                            }
                            sendBroadcast(new Intent("com.dust.exmusic.OnMusicPlayerStateChanged"));
                        }
                    }
                }
            });

            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            Log.i("mediaplayerInit",e.getMessage());
        }
    }

    private void setRepeat() {
        List<MainDataClass> list = new ArrayList<>();
        Pair<String, String> pair = getSeparatedListMode(sharedPreferencesCenter.getPlayPair());
        switch (pair.first) {
            case "ALL":
                sharedPreferencesCenter.setPlaylistActive("");
                list.addAll(externalRealmHandler.getAllSortedMainData(sharedPreferencesCenter.getSortType()));
                break;
            case "PlayList":
                sharedPreferencesCenter.setPlaylistActive(pair.second);
                list.addAll(externalRealmHandler.getPlayListData(pair.second));
                break;
            case "FavoriteList":
                sharedPreferencesCenter.setPlaylistActive("");
                String[] favPaths = sharedPreferencesCenter.getFavoriteListPaths();
                for (int i = 0; i < favPaths.length; i++)
                    list.add(externalRealmHandler.getMusicDataByPath(favPaths[i]));
                break;
            case "Artists":
                sharedPreferencesCenter.setPlaylistActive("");
                list.addAll(externalRealmHandler.getArtistSongs(pair.second));
                break;
            case "Albums":
                sharedPreferencesCenter.setPlaylistActive("");
                list.addAll(externalRealmHandler.getAlbumSongs(pair.second));
                break;
            case "Folders":
                sharedPreferencesCenter.setPlaylistActive("");
                list.addAll(externalRealmHandler.getFoldersSong(pair.second));
                break;
        }
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getPath().equals(path))
                index = i + 1;
        }
        if (index == list.size())
            index = 0;
        initMediaPlayer(list.get(index).getPath(), false);
        PlayerService.this.path = list.get(index).getPath();

        stopForeground(true);
    }

    private Pair<String, String> getSeparatedListMode(String data) {
        return new Pair<>(data.substring(0, data.indexOf('|')), data.substring(data.indexOf('|') + 1));
    }

    private Pair<String, String> getSeparatedShuffleMode() {
        String data = sharedPreferencesCenter.getLastPlayMode();
        return new Pair<>(data.substring(0, data.indexOf('|')), data.substring(data.indexOf('|') + 1));
    }

    private int getRandomNumber(int start, int stop) {
        return (int) Math.floor(start + (Math.random() * (stop - start)));
    }

    private List<MainDataClass> getPlayListData(String playlistActive) {
        List<MainDataClass> dataClasses = new ArrayList<>();
        Realm realm = Realm.getDefaultInstance();
        RealmResults<MainObject> results = realm.where(MainObject.class).findAll();
        for (int i = 0; i < results.size(); i++) {
            String[] list = results.get(i).playLists.split(",");
            for (int j = 0; j < list.length; j++) {
                if (list[j].equals(playlistActive))
                    dataClasses.add(convertObjectToDataClass(results.get(i)));
            }
        }
        return dataClasses;
    }

    public MainDataClass convertObjectToDataClass(MainObject object) {
        MainDataClass dataClass = new MainDataClass();
        dataClass.setMusicName(object.name);
        dataClass.setArtistName(object.artistName);
        dataClass.setPath(object.path);
        dataClass.setYear(object.year);
        dataClass.setAlbum(object.album);
        dataClass.setPlaying(false);
        return dataClass;
    }

    @Override
    public IBinder onBind(Intent intent) {
        initMediaPlayer(intent.getExtras().getString("PATH"), false);
        initSharedPreferences();
        return new MyBinder();
    }

    private void initSharedPreferences() {
        sharedPreferencesCenter = new SharedPreferencesCenter(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            intent.getAction();
        } catch (Exception e) {
            return START_STICKY;
        }

        switch (intent.getAction()) {
            case "com.dust.exmusic.ACTION_PLAY":
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    showNotification(TYPE_PAUSE);
                } else {
                    mediaPlayer.start();
                    showNotification(TYPE_PLAY);
                }
                sendPath();
                sendBroadcast(new Intent("com.dust.exmusic.OnMusicPlayerStateChanged"));
                break;
            case "com.dust.exmusic.ACTION_FORWARD":
                goToNextMusic();
                break;
            case "com.dust.exmusic.ACTION_REWIND":
                goToPrevMusic();
                break;
            case "com.dust.exmusic.ACTION_NEW_MUSIC":
                String nextPath = intent.getStringExtra("EXTRA_MUSIC_PATH");
                Log.i("ACTION_NEW_MUSIC","call :" + nextPath);
                if (nextPath != null && !path.equals(nextPath)) {
                    initMediaPlayer(nextPath, false);
                }
                break;
            case "com.dust.exmusic.ACTION_SEND_PATH":
                try {
                    sendPath();
                } catch (Exception e) {

                }
                break;
            case "com.dust.exmusic.ACTION_RESET":
                try {
                    if (!path.equals(intent.getExtras().getString("PATH"))) {
                        initMediaPlayer(intent.getExtras().getString("PATH"), false);
                    }
                } catch (Exception e) {
                }
                break;
            case "com.dust.exmusic.ACTION_DELETE_NOTIFICATION":
                mediaPlayer.pause();
                sendBroadcast(new Intent("com.dust.exmusic.OnMusicPlayerStateChanged"));
                stopForeground(true);
                sharedPreferencesCenter.setPlaylistActive("");
                sendPath();
                break;
        }

        return START_STICKY;
    }

    private List<MainDataClass> getListByType() {
        if (!sharedPreferencesCenter.getPlaylistActive().isEmpty()) {
            return getPlayListData(sharedPreferencesCenter.getPlaylistActive());
        } else {
            List<MainDataClass> list = new ArrayList<>();
            Pair<String, String> pair = getSeparatedShuffleMode();
            switch (pair.first) {
                case "Artists":
                    list.addAll(externalRealmHandler.getArtistSongs(pair.second));
                    break;
                case "Albums":
                    list.addAll(externalRealmHandler.getAlbumSongs(pair.second));
                    break;
                case "Folders":
                    list.addAll(externalRealmHandler.getFoldersSong(pair.second));
                    break;
                case "ALL":
                    list.addAll(externalRealmHandler.getAllSortedMainData(sharedPreferencesCenter.getSortType()));
                    break;
                case "PlayList":
                    list.addAll(externalRealmHandler.getPlayListData(pair.second));
                    break;
                case "FavoriteList":
                    String[] names = sharedPreferencesCenter.getFavoriteListPaths();
                    if (!names[0].equals(""))
                        for (int i = 0; i < names.length; i++)
                            list.add(externalRealmHandler.getMusicDataByPath(names[i]));
                    break;
                default:
                    return externalRealmHandler.getAllSortedMainData(sharedPreferencesCenter.getSortType());
            }
            return list;
        }
    }

    private Pair<String, String> getPreviousAndNextPath(String currentPath) {
        List<MainDataClass> allMusics = getListByType();
        int currentIndex = -1;
        for (int i = 0; i < allMusics.size(); i++) {
            if (allMusics.get(i).getPath().equals(currentPath))
                currentIndex = i;
        }

        String previousPath = null;
        String nextPath = null;

        int pIndex = currentIndex - 1;
        int nIndex = currentIndex + 1;

        if (pIndex >= 0 && pIndex < allMusics.size())
            previousPath = allMusics.get(pIndex).getPath();

        if (nIndex < allMusics.size())
            nextPath = allMusics.get(nIndex).getPath();

        return new Pair<String, String>(previousPath, nextPath);
    }

    private void sendPath() {
        Intent intent1 = new Intent("com.dust.exmusic.OnReceivePath");
        intent1.putExtra("PATH", path);
        intent1.putExtra("IS_PLAYING", mediaPlayer.isPlaying());
        sendBroadcast(intent1);
    }

    private String createNotificationChannel() {
        String channelId = getPackageName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "music_channel", NotificationManager.IMPORTANCE_LOW);
            channel.setLightColor(Color.BLUE);
            channel.setVibrationPattern(new long[0]);
            channel.enableVibration(false);
            channel.setSound(null, null);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        }
        return channelId;
    }

    private void showNotification(int type) {
        lastNotificationType = type;
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent intentPlay = new Intent(this, PlayerService.class);
        intentPlay.setAction("com.dust.exmusic.ACTION_PLAY");

        Intent intentRewind = new Intent(this, PlayerService.class);
        intentRewind.setAction("com.dust.exmusic.ACTION_REWIND");

        Intent intentForward = new Intent(this, PlayerService.class);
        intentForward.setAction("com.dust.exmusic.ACTION_FORWARD");

        Intent intent1 = new Intent(this, PlayerService.class);
        intent1.setAction("com.dust.exmusic.ACTION_DELETE_NOTIFICATION");

        new MetaDataLoader(this).getPicture(path, new OnLoadPicture() {
            @Override
            public void onGetPicture(Bitmap bitmap) {
                if (bitmap == null)
                    bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.empty_music_pic);

                int icon = 0;

                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(PlayerService.this, createNotificationChannel());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){

                    if (type == TYPE_PLAY) {
                        icon = R.drawable.ic_baseline_pause_white;
                    } else {
                        icon = R.drawable.ic_baseline_play_arrow_white;
                    }

                    try {
                        MediaMetadataCompat.Builder mediaMetadataCompat = new MediaMetadataCompat.Builder();
                        mediaMetadataCompat.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer.getDuration());
                        mediaSessionCompat.setMetadata(mediaMetadataCompat.build());

                        int state = PlaybackStateCompat.STATE_PLAYING;
                        if (type == TYPE_PAUSE)
                            state = PlaybackStateCompat.STATE_PAUSED;

                        PlaybackStateCompat.Builder pbs = new PlaybackStateCompat.Builder().setState(state, mediaPlayer.getCurrentPosition(), 1.0f)
                                .setActions(PlaybackStateCompat.ACTION_SEEK_TO | PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);

                        mediaSessionCompat.setPlaybackState(pbs.build());
                    } catch (Exception e) {}

                    notificationBuilder
                            .setSmallIcon(icon)
                            .setLargeIcon(bitmap)
                            .setContentTitle(getMusicDataByPath(path).getMusicName())
                            .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSessionCompat.getSessionToken()))
                            .setPriority(PRIORITY_LOW)
                            .setContentText(getMusicDataByPath(path).getArtistName())
                            .setContentIntent(PendingIntent.getActivity(PlayerService.this, 102, mainIntent, PendingIntent.FLAG_IMMUTABLE));

                    startForeground(101, notificationBuilder.build());
                } else {

                    if (type == TYPE_PLAY) {
                        icon = R.drawable.gradient_pause;
                    } else {
                        icon = R.drawable.gradient_play;
                    }

                    Notification notification = notificationBuilder
                            .setSmallIcon(R.drawable.playlist)
                            .setLargeIcon(bitmap)
                            .setContentTitle(getMusicDataByPath(path).getMusicName())
                            .setContentText(getMusicDataByPath(path).getArtistName())
                            .setCustomContentView(new RemoteViews(getPackageName(), R.layout.notification_normal))
                            .setCustomBigContentView(new RemoteViews(getPackageName(), R.layout.notification_layout))
                            .setContentIntent(PendingIntent.getActivity(PlayerService.this, 102, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                            .build();

                    notification.contentView.setImageViewBitmap(R.id.mainImage, bitmap);
                    notification.contentView.setTextViewText(R.id.musicName, getMusicDataByPath(path).getMusicName());
                    notification.contentView.setTextViewText(R.id.artistName, getMusicDataByPath(path).getArtistName());

                    notification.bigContentView.setOnClickPendingIntent(R.id.close, PendingIntent.getService(PlayerService.this, 101, intent1, PendingIntent.FLAG_IMMUTABLE));
                    notification.bigContentView.setImageViewBitmap(R.id.mainImage, bitmap);
                    notification.bigContentView.setImageViewResource(R.id.play, icon);
                    notification.bigContentView.setTextViewText(R.id.musicName, getMusicDataByPath(path).getMusicName());
                    notification.bigContentView.setTextViewText(R.id.artistName, getMusicDataByPath(path).getArtistName());
                    notification.bigContentView.setOnClickPendingIntent(R.id.rewind, PendingIntent.getService(PlayerService.this, 103, intentRewind, PendingIntent.FLAG_IMMUTABLE));
                    notification.bigContentView.setOnClickPendingIntent(R.id.play, PendingIntent.getService(PlayerService.this, 103, intentPlay, PendingIntent.FLAG_IMMUTABLE));
                    notification.bigContentView.setOnClickPendingIntent(R.id.forward, PendingIntent.getService(PlayerService.this, 103, intentForward, PendingIntent.FLAG_IMMUTABLE));

                    startForeground(101, notification);
                }
            }
        });
    }

    public class MyBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    @Override
    public void onDestroy() {
        if (cpuWakeLock != null){
            if (cpuWakeLock.isHeld())
                cpuWakeLock.release();
        }

        if (notificationUpdaterTimer != null) {
            notificationUpdaterTimer.purge();
            notificationUpdaterTimer.cancel();
        }
        stopForeground(true);
        super.onDestroy();
    }

    public MainDataClass getMusicDataByPath(String path) {
        MainDataClass dataClass = new MainDataClass();
        try {
            MainObject results = Realm.getDefaultInstance().where(MainObject.class).equalTo("path", path).findFirst();
            dataClass.setMusicName(results.name);
            dataClass.setArtistName(results.artistName);
            dataClass.setPath(results.path);
            dataClass.setYear(results.year);
            dataClass.setPlaying(false);
        } catch (Exception e) {
        }

        return dataClass;
    }

}