package com.dust.exmusic.fragments.homepagefragments;

import static android.content.Context.RECEIVER_EXPORTED;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dust.exmusic.R;
import com.dust.exmusic.adapters.recyclerviews.PlayListRecyclerViewAdapter;
import com.dust.exmusic.dataclasses.MainDataClass;
import com.dust.exmusic.dataclasses.PlayListDataClass;
import com.dust.exmusic.realm.RealmHandler;
import com.dust.exmusic.sharedpreferences.SharedPreferencesCenter;

import java.util.ArrayList;
import java.util.List;

public class PlayListFragment extends Fragment {

    private RecyclerView mainRecyclerView;
    private SharedPreferencesCenter sharedPreferencesCenter;
    private RealmHandler handler;
    private final AnimationSet set = new AnimationSet(true);
    private LinearLayout nothing_Linear;
    private TextView title;

    private PlayListRecyclerViewAdapter playListRecyclerViewAdapter;

    List<PlayListDataClass> list = new ArrayList<>();

    private OnPlayListChanged onPlayListChanged;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpViews(view);
        setUpAlphaAnimation();
        setUpSharedPreferences();
        setUpRealmDb();
        setUpRecyclerView();
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

    private void setUpRealmDb() {
        handler = new RealmHandler();
    }

    private void setUpSharedPreferences() {
        sharedPreferencesCenter = new SharedPreferencesCenter(getActivity());
    }

    private void setUpRecyclerView() {
        setUpList();
        playListRecyclerViewAdapter = new PlayListRecyclerViewAdapter(list, getActivity().getSupportFragmentManager(), set);
        mainRecyclerView.setAdapter(playListRecyclerViewAdapter);
    }

    private void setUpList() {
        list.clear();
        String[] playLists = sharedPreferencesCenter.getPlayLists();
        if (playLists[0].equals("")) {
            nothing_Linear.setVisibility(View.VISIBLE);
            title.setText("");
            return;
        } else {
            nothing_Linear.setVisibility(View.GONE);
        }
        for (int i = 0; i < playLists.length; i++) {
            List<String> paths = new ArrayList<>();
            PlayListDataClass dataClass = new PlayListDataClass();
            List<MainDataClass> mainData = handler.getPlayListData(playLists[i]);
            if (!mainData.isEmpty()) {
                for (int j = 0; j < mainData.size(); j++) {
                    paths.add(mainData.get(j).getPath());
                    if (j == 3)
                        break;
                }
            }
            dataClass.setPlayListName(playLists[i]);
            dataClass.setPaths(paths);
            list.add(dataClass);
        }
    }

    private void setUpViews(View view) {
        title = (TextView) view.findViewById(R.id.title);
        nothing_Linear = (LinearLayout) view.findViewById(R.id.nothing_Linear);
        mainRecyclerView = (RecyclerView) view.findViewById(R.id.mainRecyclerView);
        mainRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void onStart() {
        super.onStart();
        onPlayListChanged = new OnPlayListChanged();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            getActivity().registerReceiver(onPlayListChanged, new IntentFilter("com.dust.exmusic.OnPlayListChanged"),RECEIVER_EXPORTED);
        else
            getActivity().registerReceiver(onPlayListChanged, new IntentFilter("com.dust.exmusic.OnPlayListChanged"));
    }

    @Override
    public void onStop() {
        getActivity().unregisterReceiver(onPlayListChanged);
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private class OnPlayListChanged extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            setUpList();
            playListRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

}
