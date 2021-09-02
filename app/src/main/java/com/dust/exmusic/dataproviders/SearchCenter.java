package com.dust.exmusic.dataproviders;

import android.os.AsyncTask;

import com.dust.exmusic.dataclasses.MainDataClass;
import com.dust.exmusic.interfaces.OnAlbumSearchComplete;
import com.dust.exmusic.interfaces.OnArtistSearchComplete;
import com.dust.exmusic.interfaces.OnFolderSearchComplete;
import com.dust.exmusic.interfaces.OnSearchMainData;
import com.dust.exmusic.realm.ExternalRealmHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import io.realm.Realm;

public class SearchCenter {
    private OnArtistSearchComplete onArtistSearchComplete;
    private OnAlbumSearchComplete onAlbumSearchComplete;
    private OnFolderSearchComplete onFolderSearchComplete;
    private OnSearchMainData onSearchMainData;

    public void searchArtist(String name, OnArtistSearchComplete onArtistSearchComplete) {
        this.onArtistSearchComplete = onArtistSearchComplete;
        ArtistSearch artistSearch = new ArtistSearch();
        artistSearch.execute(name);
    }

    public void searchAlbum(String name, OnAlbumSearchComplete onAlbumSearchComplete) {
        this.onAlbumSearchComplete = onAlbumSearchComplete;
        AlbumSearch albumSearch = new AlbumSearch();
        albumSearch.execute(name);
    }

    public void searchFolder(String name, OnFolderSearchComplete onFolderSearchComplete) {
        this.onFolderSearchComplete = onFolderSearchComplete;
        FolderSearch folderSearch = new FolderSearch();
        folderSearch.execute(name);
    }

    public String getOptimizesFolderName(String folderName) {
        String RawFolderName = folderName.substring(0, folderName.lastIndexOf("/"));
        if (RawFolderName.contains("/storage/emulated/0/"))
            return RawFolderName.replace("/storage/emulated/0/", "");
        return RawFolderName;
    }

    public void searchMainData(String name, OnSearchMainData onSearchMainData) {
        this.onSearchMainData = onSearchMainData;
        MainSearch mainSearch = new MainSearch();
        mainSearch.execute(name);
    }

    public class ArtistSearch extends AsyncTask<String, String, List<MainDataClass>> {

        @Override
        protected List<MainDataClass> doInBackground(String... strings) {
            List<MainDataClass> results = new ArrayList<>();
            ExternalRealmHandler externalRealmHandler = new ExternalRealmHandler(Realm.getDefaultInstance());

            List<MainDataClass> dataClasses = externalRealmHandler.getArtistsFirstSong();
            for (int i = 0; i < dataClasses.size(); i++) {
                if (Pattern.compile(Pattern.quote(strings[0]), Pattern.CASE_INSENSITIVE).matcher(dataClasses.get(i).getArtistName()).find())
                    results.add(dataClasses.get(i));
            }

            return results;
        }

        @Override
        protected void onPostExecute(List<MainDataClass> list) {
            super.onPostExecute(list);
            onArtistSearchComplete.onSearchArtistComplete(list);
        }
    }

    public class AlbumSearch extends AsyncTask<String, String, List<MainDataClass>> {

        @Override
        protected List<MainDataClass> doInBackground(String... strings) {
            List<MainDataClass> results = new ArrayList<>();
            ExternalRealmHandler externalRealmHandler = new ExternalRealmHandler(Realm.getDefaultInstance());

            List<MainDataClass> dataClasses = externalRealmHandler.getAlbumsFirstSong();
            for (int i = 0; i < dataClasses.size(); i++) {
                if (Pattern.compile(Pattern.quote(strings[0]), Pattern.CASE_INSENSITIVE).matcher(dataClasses.get(i).getAlbum()).find())
                    results.add(dataClasses.get(i));
            }

            return results;
        }

        @Override
        protected void onPostExecute(List<MainDataClass> list) {
            super.onPostExecute(list);
            onAlbumSearchComplete.onSearchAlbumComplete(list);
        }
    }

    public class FolderSearch extends AsyncTask<String, String, List<MainDataClass>> {

        @Override
        protected List<MainDataClass> doInBackground(String... strings) {
            List<MainDataClass> results = new ArrayList<>();
            ExternalRealmHandler externalRealmHandler = new ExternalRealmHandler(Realm.getDefaultInstance());

            List<MainDataClass> dataClasses = externalRealmHandler.getFoldersFirstSong();
            for (int i = 0; i < dataClasses.size(); i++) {
                if (Pattern.compile(Pattern.quote(strings[0]), Pattern.CASE_INSENSITIVE).matcher(getOptimizesFolderName(dataClasses.get(i).getPath())).find())
                    results.add(dataClasses.get(i));
            }

            return results;
        }

        @Override
        protected void onPostExecute(List<MainDataClass> list) {
            super.onPostExecute(list);
            onFolderSearchComplete.onSearchFolderComplete(list);
        }
    }

    public class MainSearch extends AsyncTask<String, String, List<MainDataClass>> {

        @Override
        protected List<MainDataClass> doInBackground(String... strings) {
            List<MainDataClass> results = new ArrayList<>();
            ExternalRealmHandler externalRealmHandler = new ExternalRealmHandler(Realm.getDefaultInstance());

            List<MainDataClass> dataClasses = externalRealmHandler.getAllMainData();
            for (int i = 0; i < dataClasses.size(); i++) {
                if (Pattern.compile(Pattern.quote(strings[0]), Pattern.CASE_INSENSITIVE).matcher(dataClasses.get(i).getMusicName()).find())
                    results.add(dataClasses.get(i));
            }

            for (int i = 0; i < dataClasses.size(); i++) {
                if (Pattern.compile(Pattern.quote(strings[0]), Pattern.CASE_INSENSITIVE).matcher(dataClasses.get(i).getArtistName()).find())
                    if (!results.contains(dataClasses.get(i)))
                        results.add(dataClasses.get(i));
            }

            return results;
        }

        @Override
        protected void onPostExecute(List<MainDataClass> list) {
            super.onPostExecute(list);
            onSearchMainData.onComplete(list);
        }
    }


}
