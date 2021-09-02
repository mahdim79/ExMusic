package com.dust.exmusic.fragments.homepagefragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dust.exmusic.R;
import com.dust.exmusic.adapters.recyclerviews.ArtistsRecyclerViewAdapter;
import com.dust.exmusic.dataclasses.MainDataClass;
import com.dust.exmusic.realm.RealmHandler;

import java.util.ArrayList;
import java.util.List;

public class ArtistsFragment extends Fragment {

    private RecyclerView mainRecyclerView;
    private RealmHandler handler;
    private NestedScrollView nestedMain;
    private int pagination = 1;
    private LinearLayout nothing_Linear;
    private TextView title;
    private List<MainDataClass> listMain = new ArrayList<>();
    private ArtistsRecyclerViewAdapter mAdapter;
    private AnimationSet set = new AnimationSet(true);

    private OnFolderListChanged onFolderListChanged;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpAlphaAnimation();
        setUpViews(view);
        setUpRealmDb();
        setUpRecyclerView();
        setUpPagination();
    }

    private void setUpAlphaAnimation() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setDuration(1000);
        alphaAnimation.setFillAfter(true);

        ScaleAnimation scaleAnimation = new ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(1000);

        set.addAnimation(alphaAnimation);
        set.addAnimation(scaleAnimation);
    }


    private void setUpPagination() {
        nestedMain.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    pagination++;
                    int lastCount = listMain.size();
                    listMain.addAll(handler.getArtistsFirstSong(pagination));
                    if (lastCount != listMain.size()) {
                        mAdapter.notifyItemRangeInserted(lastCount, listMain.size() - lastCount);
                    }
                }
            }
        });
    }

    private void setUpRealmDb() {
        handler = new RealmHandler();
    }

    private void setUpRecyclerView() {

        listMain.addAll(handler.getArtistsFirstSong(pagination));
        mAdapter = new ArtistsRecyclerViewAdapter(listMain, getActivity().getSupportFragmentManager(), set);
        mainRecyclerView.setAdapter(mAdapter);


        if (listMain.isEmpty()) {
            nothing_Linear.setVisibility(View.VISIBLE);
            title.setVisibility(View.INVISIBLE);
        } else {
            nothing_Linear.setVisibility(View.GONE);
            title.setVisibility(View.VISIBLE);
        }
    }

    private void setUpViews(View view) {
        title = (TextView) view.findViewById(R.id.title);
        nothing_Linear = (LinearLayout) view.findViewById(R.id.nothing_Linear);
        mainRecyclerView = (RecyclerView) view.findViewById(R.id.mainRecyclerView);
        nestedMain = (NestedScrollView) view.findViewById(R.id.nestedMain);
        mainRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void onStart() {
        super.onStart();
        onFolderListChanged = new OnFolderListChanged();
        getActivity().registerReceiver(onFolderListChanged, new IntentFilter("com.dust.exmusic.OnFolderListChanged"));
    }

    @Override
    public void onStop() {
        getActivity().unregisterReceiver(onFolderListChanged);
        super.onStop();
    }

    private class OnFolderListChanged extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                setUpRecyclerView();
            } catch (Exception e) {
            }
        }
    }

}
