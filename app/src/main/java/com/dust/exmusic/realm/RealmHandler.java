package com.dust.exmusic.realm;

import android.text.TextUtils;
import android.util.Log;

import com.dust.exmusic.dataclasses.MainDataClass;
import com.dust.exmusic.datasort.DataSorter;
import com.dust.exmusic.interfaces.OnMainDataAdded;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class RealmHandler {

    private final int SORT_BY_NAME = 1;
    private final int SORT_BY_LAST_MODIFICATION_DATE = 0;
    private final int SORT_BY_Year = 2;

    private boolean changeInDataBase = false;

    private Realm realm = Realm.getDefaultInstance();

    public void insertMainData(List<MainDataClass> list, OnMainDataAdded onMainDataAdded) {

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm r) {
                int id;
                for (int i = 0; i < list.size(); i++) {
                    if (checkSongRealmAvailability(list.get(i).getPath(), r))
                        continue;

                    try {
                        id = Integer.parseInt(r.where(MainObject.class).findAll().max("id").toString()) + 1;
                    } catch (Exception e) {
                        id = 0;
                    }
                    MainObject object = r.createObject(MainObject.class, id);
                    object.name = list.get(i).getMusicName();
                    object.path = list.get(i).getPath();
                    object.artistName = list.get(i).getArtistName();
                    object.year = list.get(i).getYear();
                    object.album = list.get(i).getAlbum();
                    object.playLists = "";
                    object.lastModification = list.get(i).getLastModification();
                    changeInDataBase = true;
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                List<MainDataClass> totalList = getAllMainData();
                for (int i = 0; i < totalList.size(); i++) {
                    if (checkListSongAvailability(totalList.get(i).getPath(), list))
                        continue;
                    removeSong(totalList.get(i).getPath());
                    changeInDataBase = true;
                }

                if (changeInDataBase)
                    onMainDataAdded.onMainDataAdded();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Log.i("RealmInsert", error.getMessage());
            }
        });

    }

    private boolean checkListSongAvailability(String path, List<MainDataClass> list) {
        for (int j = 0; j < list.size(); j++)
            if (path.equals(list.get(j).getPath()))
                return true;
        return false;
    }

    private void removeSong(String path) {

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm r) {
                try {
                    r.where(MainObject.class).equalTo("path", path).findFirst().deleteFromRealm();
                } catch (Exception e) {
                }
            }
        });
    }

    public boolean checkSongRealmAvailability(String path, Realm r) {
        try {
            if (r.where(MainObject.class).equalTo("path", path).findFirst() == null)
                return false;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<MainDataClass> getMainData(int pagination, int sortType) {
        List<MainDataClass> list = new ArrayList<>();
        DataSorter dataSorter = new DataSorter();

        int start = (pagination - 1) * 16;
        int stop = pagination * 16;
        try {
            RealmResults<MainObject> results = realm.where(MainObject.class).findAll();
            List<MainDataClass> sortedList = new ArrayList<>();

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

            for (int i = start; i < stop; i++) {
                list.add(sortedList.get(i));
            }
        } catch (Exception e) {
            Log.i("sortError", e.getMessage());
        }
        return list;
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

    public MainDataClass getMusicDataByPath(String path) {
        try {
            MainObject results = realm.where(MainObject.class).equalTo("path", path).findFirst();
            return convertObjectToDataClass(results);
        } catch (Exception e) {
            return null;
        }
    }

    public List<MainDataClass> getAlbumByPath(String path) {
        List<MainDataClass> list = new ArrayList<>();
        String album = realm.where(MainObject.class).equalTo("path", path).findFirst().album;
        RealmResults<MainObject> results = realm.where(MainObject.class).equalTo("album", album).findAll();
        for (int i = 0; i < results.size(); i++) {
            list.add(convertObjectToDataClass(results.get(i)));
        }
        return list;
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

    public List<MainDataClass> getArtistsFirstSong(int pagination) {
        List<MainDataClass> list = new ArrayList<>();
        List<String> listArtists = getArtistsNames();
        int start = (pagination - 1) * 30;
        int stop = pagination * 30;
        try {
            for (int i = start; i < stop; i++) {
                MainObject object = realm.where(MainObject.class).equalTo("artistName", listArtists.get(i)).findFirst();
                list.add(convertObjectToDataClass(object));
            }
        } catch (Exception e) {
        }
        return list;
    }

    public int getAllDataCount() {
        try {
            return realm.where(MainObject.class).findAll().size();
        } catch (Exception e) {
            return 0;
        }
    }

    public void setIntoPlayList(String path, String playListName) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm r) {
                List<String> newList = new ArrayList<>();
                MainObject mainObject = r.where(MainObject.class).equalTo("path", path).findFirst();
                String[] list = mainObject.playLists.split(",");
                newList.add(playListName);
                newList.addAll(Arrays.asList(list));
                mainObject.playLists = TextUtils.join(",", newList);
                r.copyToRealmOrUpdate(mainObject);
            }
        });
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

    public void removeFromPlayList(String playList, String path) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm r) {
                MainObject mainObject = r.where(MainObject.class).equalTo("path", path).findFirst();
                String[] list = mainObject.playLists.split(",");
                List<String> newList = new ArrayList<>(Arrays.asList(list));
                newList.remove(playList);
                mainObject.playLists = TextUtils.join(",", newList);
                r.copyToRealmOrUpdate(mainObject);
            }
        });
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

    public void deletePlayListSongs(String playListName) {
        List<MainObject> list = new ArrayList<>();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm r) {
                RealmResults<MainObject> results = r.where(MainObject.class).findAll();
                List<String> tempList;
                for (int i = 0; i < results.size(); i++) {
                    String[] playLists = results.get(i).playLists.split(",");
                    tempList = new ArrayList<>(Arrays.asList(playLists));
                    if (tempList.contains(playListName)) {
                        tempList.remove(playListName);
                        results.get(i).playLists = TextUtils.join(",", tempList);
                        r.copyToRealmOrUpdate(results.get(i));
                    }
                }
            }
        });

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

    public List<MainDataClass> getAlbumsFirstSong(int pagination) {
        List<MainDataClass> list = new ArrayList<>();
        List<String> listAlbums = getAlbumsNames();
        int start = (pagination - 1) * 30;
        int stop = pagination * 30;
        try {
            for (int i = start; i < stop; i++) {
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

    public List<MainDataClass> getFoldersFirstSong() {
        List<MainDataClass> list = new ArrayList<>();
        List<String> foldersNames = getFoldersName();
        for (int i = 0; i < foldersNames.size(); i++)
            list.add(getFoldersSong(foldersNames.get(i)).get(0));
        return list;
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

}
