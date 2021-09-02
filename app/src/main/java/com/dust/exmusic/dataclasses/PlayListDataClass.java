package com.dust.exmusic.dataclasses;

import java.util.List;

public class PlayListDataClass {
    private String playListName;
    private List<String> paths;

    public String getPlayListName() {
        return playListName;
    }

    public void setPlayListName(String playListName) {
        this.playListName = playListName;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }
}
