package com.dust.exmusic.sharedpreferences;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class SharedPreferencesCenter {

    private Context context;

    public SharedPreferencesCenter(Context context) {

        this.context = context;
    }

    public int getRepeatMode() {
        return context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE).getInt("REPEAT_MODE", 0);
    }

    public void setRepeatMode(int i) {
        context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE).edit().putInt("REPEAT_MODE", i).apply();
    }

    public void addToPlayList(String name) {
        List<String> list = new ArrayList<>();
        String[] result = context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE).getString("PLAYLIST", "").split(",");
        list.add(name);
        for (int i = 0; i < result.length; i++) {
            list.add(result[i]);
        }
        context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE).edit().putString("PLAYLIST", TextUtils.join(",", list)).apply();
    }

    public String[] getPlayLists() {
        return context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE).getString("PLAYLIST", "").split(",");
    }

    public void removePlayList(String name) {
        List<String> list = new ArrayList<>();
        String[] data = getPlayLists();
        for (int i = 0; i < data.length; i++) {
            if (!data[i].equals(name))
                list.add(data[i]);
        }
        context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE).edit().putString("PLAYLIST", TextUtils.join(",", list)).apply();
    }

    public boolean checkPlayListAvailability(String name) {
        String[] list = getPlayLists();
        for (int i = 0; i < list.length; i++) {
            if (list[i].equals(name))
                return true;
        }
        return false;
    }

    public void addToFavoriteListPaths(String path) {
        List<String> list = new ArrayList<>();
        String[] result = context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE).getString("FAVORITE_LIST", "").split(",");
        list.add(path);
        for (int i = 0; i < result.length; i++) {
            list.add(result[i]);
        }
        context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE).edit().putString("FAVORITE_LIST", TextUtils.join(",", list)).apply();
    }

    public String[] getFavoriteListPaths() {
        return context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE).getString("FAVORITE_LIST", "").split(",");
    }

    public void removeFavoriteList(String path) {
        List<String> list = new ArrayList<>();
        String[] data = getFavoriteListPaths();
        for (int i = 0; i < data.length; i++) {
            if (!data[i].equals(path))
                list.add(data[i]);
        }
        context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE).edit().putString("FAVORITE_LIST", TextUtils.join(",", list)).apply();
    }

    public boolean checkFavoriteListPathAvailability(String path) {
        String[] list = getFavoriteListPaths();
        for (int i = 0; i < list.length; i++) {
            if (list[i].equals(path))
                return true;
        }
        return false;
    }

    public void setPlaylistActive(String name) {
        context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE).edit().putString("PLAY_LIST", name).apply();
    }

    public String getPlaylistActive() {
        return context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE).getString("PLAY_LIST", "");
    }

    public void setShuffleMode(String mode) {
        context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE).edit().putString("SHUFFLE_MODE", mode).apply();
    }

    public String getShuffleMode() {
        return context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE).getString("SHUFFLE_MODE", "");
    }

    public int getSortType() {
        return context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE).getInt("SORT_TYPE", 1);

    }

    public void setSortType(int type) {
        context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE).edit().putInt("SORT_TYPE", type).apply();

    }

    public void setPlayPair(String pair) {
        context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE).edit().putString("PLAY_PAIR", pair).apply();
    }

    public String getPlayPair() {
        return context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE).getString("PLAY_PAIR", "ALL|ALL");
    }

    public void setDarkTheme(boolean darkTheme) {
        context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE).edit().putBoolean("DARK_THEME", darkTheme).apply();
    }

    public boolean getDarkTheme() {
        return context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE).getBoolean("DARK_THEME", true);
    }

    public void setEnglishLanguage(boolean englishLanguage) {
        context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE).edit().putBoolean("ENGLISH_LANGUAGE", englishLanguage).apply();
    }

    public boolean getEnglishLanguage() {
        return context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE).getBoolean("ENGLISH_LANGUAGE", false);
    }

}
