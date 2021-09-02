package com.dust.exmusic.dataproviders;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;

public class DiskLruManager {
    private final String DISK_CACHE_NAME = "thumbnails";
    private final int DISK_CACHE_SIZE = 1024 * 1024 * 50;

    private DiskLruCache diskLruCache;

    public DiskLruManager(String dir) {
        initializeDiskCacheManager(dir);
    }

    public void initializeDiskCacheManager(String dir) {
        File cache = new File(dir + File.separator + DISK_CACHE_NAME);
        try {
            diskLruCache = DiskLruCache.open(cache, 1, 1, DISK_CACHE_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cacheBitmap(String name, Bitmap bitmap) {
        if (bitmap == null)
            return;
        try {
            DiskLruCache.Editor editor = diskLruCache.edit(name);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, editor.newOutputStream(0));
            editor.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Bitmap getBitmap(String name) {
        try {
            DiskLruCache.Editor editor = diskLruCache.edit(name);
            Bitmap bitmap = BitmapFactory.decodeStream(editor.newInputStream(0));
            editor.abort();
            return bitmap;
        } catch (IOException e) {
            return null;
        }
    }
}
