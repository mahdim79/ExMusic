package com.dust.exmusic.dataproviders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.Log;

import com.dust.exmusic.interfaces.OnLoadPicture;
import com.dust.exmusic.realm.ExternalRealmHandler;

import java.io.File;
import java.io.FileInputStream;

import io.realm.Realm;

public class MetaDataLoader {
    private MediaMetadataRetriever mediaMetadataRetriever;
    private OnLoadPicture onLoadPicture;
    private OnLoadPicture onLoadSinglePicture;
    private Object diskLruLockObject = new Object();
    DiskLruManager diskLruManager;

    private final int LARGE_PIC_SIZE = 1;
    private final int SMALL_PIC_SIZE = 0;

    public MetaDataLoader(String path) {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(path);
            this.mediaMetadataRetriever = mediaMetadataRetriever;

    }

    public MetaDataLoader(Context context) {
        diskLruManager = new DiskLruManager(context.getCacheDir().getPath());
    }

    public String getName() {
        return mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
    }

    public String getArtistName() {
        return mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
    }

    public String getAlbum() {
        return mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST);
    }

    public String getYear() {
        return mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);
    }

    public void getPicture(String path, OnLoadPicture onLoadPicture) {
        String key = getKey(LARGE_PIC_SIZE, path);
        Bitmap bitmap = getCachedBitmap(key);
        if (bitmap != null) {
            onLoadPicture.onGetPicture(bitmap);
            return;
        }
        this.onLoadSinglePicture = onLoadPicture;
        GetPictureAsync getPictureAsync = new GetPictureAsync();
        getPictureAsync.execute(path, key);
    }

    public void getLowSizePictureAsync(String path, OnLoadPicture onLoadPicture) {
        this.onLoadPicture = onLoadPicture;
        GetScaledPictureAsync getScaledPictureAsync = new GetScaledPictureAsync();
        getScaledPictureAsync.execute(path);
    }

    private Bitmap getCachedBitmap(String key) {
        return diskLruManager.getBitmap(key);
    }

    private void cacheBitmap(Bitmap bitmap, String key) {
        if (bitmap != null)
            diskLruManager.cacheBitmap(key, bitmap);
    }

    private String getKey(int PicSize, String path) {
        int key = new ExternalRealmHandler(Realm.getDefaultInstance()).getMusicDataByPath(path).getKey();
        if (PicSize == LARGE_PIC_SIZE)
            return String.valueOf(key);
        return key + "_small";
    }

    public class GetScaledPictureAsync extends AsyncTask<String, String, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {

            String key = getKey(SMALL_PIC_SIZE, strings[0]);
            Bitmap bitmap1 = getCachedBitmap(key);
            if (bitmap1 != null)
                return bitmap1;

            Bitmap bitmap = generateScaledBitmap(strings[0], 100, 100);
            cacheBitmap(bitmap, key);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            onLoadPicture.onGetPicture(bitmap);
        }
    }

    public class GetPictureAsync extends AsyncTask<String, String, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bitmap = generateScaledBitmap(strings[0], 600, 600);
            cacheBitmap(bitmap, strings[1]);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            onLoadSinglePicture.onGetPicture(bitmap);
        }
    }

    private Bitmap generateScaledBitmap(String path, int targetWidth, int targetHeight) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        Bitmap bitmap = null;
        try {
            mediaMetadataRetriever.setDataSource(path);
            byte[] data = mediaMetadataRetriever.getEmbeddedPicture();
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        } catch (Exception e) {
            if (bitmap != null)
                bitmap.recycle();
            return null;
        }

        if (checkResizeNeeded(bitmap.getWidth(), bitmap.getHeight(), targetWidth, targetHeight)) {
            try {
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false);
                bitmap.recycle();
                return scaledBitmap;
            } catch (Exception e) {
                if (bitmap != null)
                    bitmap.recycle();
                return null;
            }
        }
        return bitmap;
    }

    private boolean checkResizeNeeded(int minWidth, int minHeight, int targetWidth, int targetHeight) {

        if (minWidth > targetWidth || minHeight > targetHeight)
            return true;
        return false;

    }

}
