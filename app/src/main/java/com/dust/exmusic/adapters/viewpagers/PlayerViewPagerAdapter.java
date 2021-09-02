package com.dust.exmusic.adapters.viewpagers;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.dust.exmusic.dataclasses.MainDataClass;
import com.dust.exmusic.fragments.others.PlayerPartFragment;

import java.util.List;

public class PlayerViewPagerAdapter extends FragmentPagerAdapter {
    private List<MainDataClass> list;

    public PlayerViewPagerAdapter(@NonNull FragmentManager fm, List<MainDataClass> list) {
        super(fm);
        this.list = list;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return new PlayerPartFragment(list.get(position));
    }

    @Override
    public int getCount() {
        return list.size();
    }
}
