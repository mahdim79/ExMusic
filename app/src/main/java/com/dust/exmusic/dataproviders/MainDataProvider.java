package com.dust.exmusic.dataproviders;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;

import com.dust.exmusic.dataclasses.MainDataClass;
import com.dust.exmusic.interfaces.OnLoadData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainDataProvider {
    private OnLoadData onLoadData;

    public void getMainData(OnLoadData onLoadData) {
        this.onLoadData = onLoadData;
        GetMainDataAsync dataAsync = new GetMainDataAsync();
        dataAsync.execute();
    }

    public List<Pair<String, Long>> getMainRawData(String rootPath) {
        List<Pair<String, Long>> list = new ArrayList<>();
        try {
            File rootFile = new File(rootPath);
            File[] listFiles = rootFile.listFiles();
            for (int i = 0; i < listFiles.length; i++) {
                if (!listFiles[i].isDirectory()) {
                    if (listFiles[i].getName().endsWith(".mp3")) {
                        list.add(new Pair<>(listFiles[i].getPath(), listFiles[i].lastModified()));
                    }
                } else {
                    if (rootPath.equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                        if (!listFiles[i].getName().equals("Android")
                                && !listFiles[i].getName().equals("Telegram"))
                            list.addAll(getMainRawData(listFiles[i].getAbsolutePath()));
                    } else {
                        list.addAll(getMainRawData(listFiles[i].getAbsolutePath()));
                    }
                }
            }

        } catch (Exception e) {
            Log.i("getFiles", e.getMessage());
        }
        return list;
    }

    private class GetMainDataAsync extends AsyncTask<Void, Void, List<MainDataClass>> {

        @Override
        protected List<MainDataClass> doInBackground(Void... voids) {
            List<MainDataClass> list = new ArrayList<>();

            List<Pair<String, Long>> map = getMainRawData(Environment.getExternalStorageDirectory().getAbsolutePath());
            for (int i = 0; i < map.size(); i++) {
                try{
                    String result = map.get(i).first;
                    MainDataClass dataClass = new MainDataClass();
                    MetaDataLoader metaDataLoader = new MetaDataLoader(result);
                    String name = metaDataLoader.getName();
                    if (name == null || name.equals(""))
                        dataClass.setMusicName(result.substring(result.lastIndexOf("/"), result.length()));
                    else
                        dataClass.setMusicName(name);

                    String artistName = metaDataLoader.getArtistName();

                    if (artistName == null || artistName.equals(""))
                        dataClass.setArtistName("ناشناس");
                    else
                        dataClass.setArtistName(artistName);

                    String year = metaDataLoader.getYear();

                    if (year == null || year.equals(""))
                        dataClass.setYear("نامشخص");
                    else
                        dataClass.setYear(year);

                    String album = metaDataLoader.getAlbum();

                    if (album == null || album.equals(""))
                        dataClass.setAlbum("نامشخص");
                    else
                        dataClass.setAlbum(album);

                    dataClass.setLastModification(map.get(i).second);

                    dataClass.setPath(result);
                    dataClass.setPlaying(true);
                    list.add(dataClass);
                }catch (Exception e){
                    Log.i("loadSongLog","exception: " + e.getMessage());
                }
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<MainDataClass> list) {
            super.onPostExecute(list);
            onLoadData.onLoadData(list);
        }
    }
}
