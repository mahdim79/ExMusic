package com.dust.exmusic.dataproviders;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;

import com.dust.exmusic.dataclasses.MainDataClass;
import com.dust.exmusic.interfaces.OnLoadData;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
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

    private List<Pair<String,Long>> filterDuplicateData(List<Pair<String,Long>> data){
        List<Pair<String,Long>> filteredData = new ArrayList<>();

        // move duplicated musics to the end of the list
        data.sort(new Comparator<Pair<String, Long>>() {
            @Override
            public int compare(Pair<String, Long> o1, Pair<String, Long> o2) {
                if ((o1.first.contains("(") && o1.first.contains(")") && (o1.first.indexOf(")") - o1.first.indexOf("(") == 2)) && !(o2.first.contains("(") && o2.first.contains(")") && (o2.first.indexOf(")") - o2.first.indexOf("(") == 2))){
                    return 1;
                }
                if (!(o1.first.contains("(") && o1.first.contains(")") && (o1.first.indexOf(")") - o1.first.indexOf("(") == 2)) && (o2.first.contains("(") && o2.first.contains(")") && (o2.first.indexOf(")") - o2.first.indexOf("(") == 2))){
                    return -1;
                }
                return 0;
            }
        });

        for (int i = 0;i<data.size();i++){
            String path = data.get(i).first;
            if (path.contains("(") && path.contains(")") && (path.indexOf(")") - path.indexOf("(") == 2)){
                try{
                    String num = path.substring(path.indexOf("(") +1 ,path.indexOf(")"));
                    int a = Integer.parseInt(num);
                    String testPath = path.substring(0,path.lastIndexOf("(")).trim();
                    boolean found = false;
                    for (int j = 0;j<filteredData.size();j++){
                        if (filteredData.get(j).first.contains(testPath)){
                            found = true;
                        }
                    }
                    if (!found){
                        filteredData.add(data.get(i));
                    }
                }catch (Exception e){
                    filteredData.add(data.get(i));
                }
            }else {
                filteredData.add(data.get(i));
            }
        }
        return filteredData;
    }

    private class GetMainDataAsync extends AsyncTask<Void, Void, List<MainDataClass>> {

        @Override
        protected List<MainDataClass> doInBackground(Void... voids) {
            List<MainDataClass> list = new ArrayList<>();

            List<Pair<String, Long>> map = filterDuplicateData(getMainRawData(Environment.getExternalStorageDirectory().getAbsolutePath()));
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
