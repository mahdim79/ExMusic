package com.dust.exmusic.adapters.viewpagers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.dust.exmusic.R;
import com.dust.exmusic.fragments.homepagefragments.ArtistsFragment;
import com.dust.exmusic.fragments.homepagefragments.FavoriteListFragment;
import com.dust.exmusic.fragments.homepagefragments.HomeFragment;
import com.dust.exmusic.fragments.homepagefragments.PlayListFragment;

public class MainViewPagerAdapter extends FragmentPagerAdapter {
    private Context context;

    public MainViewPagerAdapter(@NonNull FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new PlayListFragment();
            case 2:
                return new FavoriteListFragment();
            default:
                return new ArtistsFragment();
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getResources().getString(R.string.all);
            case 1:
                return context.getResources().getString(R.string.playList);
            case 2:
                return context.getResources().getString(R.string.favorites);
            default:
                return context.getResources().getString(R.string.artists);
        }
    }
}
