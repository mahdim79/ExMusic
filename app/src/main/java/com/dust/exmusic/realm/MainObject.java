package com.dust.exmusic.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class MainObject extends RealmObject {

    @PrimaryKey
    public int id;
    public String name;
    public String path;
    public String artistName;
    public String year;
    public String album;
    public String playLists;
    public long lastModification;
}
