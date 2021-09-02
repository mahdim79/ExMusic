package com.dust.exmusic.realm;

import android.util.Log;

import com.dust.exmusic.dataclasses.MainDataClass;
import com.dust.exmusic.datasort.DataSorter;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class ExternalRealmHandler {
    private Realm realm;

    private final int SORT_BY_NAME = 1;
    private final int SORT_BY_LAST_MODIFICATION_DATE = 0;
    private final int SORT_BY_Year = 2;

    public ExternalRealmHandler(Realm realm) {

        this.realm = realm;
    }

    public List<MainDataClass> getAlbumSongs(String albumName) {
        List<MainDataClass> results = new ArrayList<>();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm r) {
                RealmResults<MainObject> resultData = r.where(MainObject.class).equalTo("album", albumName).findAll();
                for (int i = 0; i < resultData.size(); i++)
                    results.add(convertObjectToDataClass(resultData.get(i)));
            }
        });
        return results;
    }

    public List<MainDataClass> getArtistsFirstSong() {
        List<MainDataClass> list = new ArrayList<>();
        List<String> listArtists = getArtistsNames();
        try {
            for (int i = 0; i < listArtists.size(); i++) {
                MainObject object = realm.where(MainObject.class).equalTo("artistName", listArtists.get(i)).findFirst();
                list.add(convertObjectToDataClass(object));
            }
        } catch (Exception e) {
        }
        return list;
    }

    public MainDataClass convertObjectToDataClass(MainObject object) {
        MainDataClass dataClass = new MainDataClass();
        dataClass.setMusicName(object.name);
        dataClass.setArtistName(object.artistName);
        dataClass.setPath(object.path);
        dataClass.setYear(object.year);
        dataClass.setAlbum(object.album);
        dataClass.setPlaying(false);
        dataClass.setLastModification(object.lastModification);
        dataClass.setKey(object.id);
        return dataClass;
    }

    public List<String> getArtistsNames() {
        List<String> listArtists = new ArrayList<>();
        RealmResults<MainObject> results = realm.where(MainObject.class).findAll();
        for (int i = 0; i < results.size(); i++) {
            if (!listArtists.contains(results.get(i).artistName))
                listArtists.add(results.get(i).artistName);
        }
        return listArtists;
    }

    public List<MainDataClass> getAlbumsFirstSong() {
        List<MainDataClass> list = new ArrayList<>();
        List<String> listAlbums = getAlbumsNames();
        try {
            for (int i = 0; i < listAlbums.size(); i++) {
                MainObject object = realm.where(MainObject.class).equalTo("album", listAlbums.get(i)).findFirst();
                list.add(convertObjectToDataClass(object));
            }
        } catch (Exception e) {
        }
        return list;
    }

    public List<String> getAlbumsNames() {
        List<String> listArtists = new ArrayList<>();
        RealmResults<MainObject> results = realm.where(MainObject.class).findAll();
        for (int i = 0; i < results.size(); i++) {
            if (!listArtists.contains(results.get(i).album))
                listArtists.add(results.get(i).album);
        }
        return listArtists;
    }

    public List<MainDataClass> getFoldersFirstSong() {
        List<MainDataClass> list = new ArrayList<>();
        List<String> foldersNames = getFoldersName();
        for (int i = 0; i < foldersNames.size(); i++)
            list.add(getFoldersSong(foldersNames.get(i)).get(0));
        return list;
    }

    public List<String> getFoldersName() {
        List<String> results = new ArrayList<>();
        List<MainDataClass> allData = getAllMainData();
        for (int i = 0; i < allData.size(); i++) {
            String folderName = allData.get(i).getPath().substring(0, allData.get(i).getPath().lastIndexOf("/"));
            if (!results.contains(folderName))
                results.add(folderName);
        }
        return results;
    }

    public List<MainDataClass> getFoldersSong(String Name) {
        List<MainDataClass> results = new ArrayList<>();
        List<MainDataClass> allData = getAllMainData();
        for (int i = 0; i < allData.size(); i++) {
            String folderName = allData.get(i).getPath().substring(0, allData.get(i).getPath().lastIndexOf("/"));
            if (folderName.equals(Name))
                results.add(allData.get(i));
        }
        return results;
    }

    public MainDataClass getMusicDataByPath(String path) {
        try {
            MainObject results = realm.where(MainObject.class).equalTo("path", path).findFirst();
            return convertObjectToDataClass(results);
        } catch (Exception e) {
            return null;
        }
    }

    public List<MainDataClass> getAllMainData() {
        List<MainDataClass> list = new ArrayList<>();
        try {
            RealmResults<MainObject> results = realm.where(MainObject.class).findAll();
            for (int i = 0; i < results.size(); i++) {
                list.add(convertObjectToDataClass(results.get(i)));
            }
        } catch (Exception e) {
        }
        return list;
    }

    public List<MainDataClass> getArtistSongs(String artistName) {
        List<MainDataClass> results = new ArrayList<>();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm r) {
                RealmResults<MainObject> resultData = r.where(MainObject.class).equalTo("artistName", artistName).findAll();
                for (int i = 0; i < resultData.size(); i++)
                    results.add(convertObjectToDataClass(resultData.get(i)));

            }
        });
        return results;
    }

    public List<MainDataClass> getAllSortedMainData(int sortType) {
        DataSorter dataSorter = new DataSorter();
        List<MainDataClass> sortedList = new ArrayList<>();

        try {
            RealmResults<MainObject> results = realm.where(MainObject.class).findAll();

            for (int i = 0; i < results.size(); i++) {
                sortedList.add(convertObjectToDataClass(results.get(i)));
            }

            switch (sortType) {
                case SORT_BY_Year:
                    dataSorter.sortByYear(sortedList);
                    break;
                case SORT_BY_NAME:
                    dataSorter.sortByName(sortedList);
                    break;
                case SORT_BY_LAST_MODIFICATION_DATE:
                    dataSorter.sortByLastModification(sortedList);
                    break;
            }
        } catch (Exception e) {
            Log.i("sortError", e.getMessage());
        }
        return sortedList;
    }

    public List<MainDataClass> getPlayListData(String playListName) {
        List<MainDataClass> dataClasses = new ArrayList<>();
        RealmResults<MainObject> results = realm.where(MainObject.class).findAll();
        for (int i = 0; i < results.size(); i++) {
            String[] list = results.get(i).playLists.split(",");
            for (int j = 0; j < list.length; j++) {
                if (list[j].equals(playListName))
                    dataClasses.add(convertObjectToDataClass(results.get(i)));
            }
        }
        return dataClasses;
    }

}
