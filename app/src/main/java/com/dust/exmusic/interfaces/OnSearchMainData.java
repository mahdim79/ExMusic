package com.dust.exmusic.interfaces;

import com.dust.exmusic.dataclasses.MainDataClass;

import java.util.List;

public interface OnSearchMainData {
    void onComplete(List<MainDataClass> results);
}
