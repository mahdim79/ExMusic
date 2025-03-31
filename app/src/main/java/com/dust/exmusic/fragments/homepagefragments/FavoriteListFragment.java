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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dust.exmusic.R;
import com.dust.exmusic.adapters.recyclerviews.AllMusicsRecyclerViewAdapter;
import com.dust.exmusic.dataclasses.MainDataClass;
import com.dust.exmusic.realm.RealmHandler;
import com.dust.exmusic.services.PlayerService;
import com.dust.exmusic.sharedpreferences.SharedPreferencesCenter;

import java.util.ArrayList;
import java.util.List;

public class FavoriteListFragment extends Fragment {
    private RecyclerView mainRecyclerView;
    private SharedPreferencesCenter sharedPreferencesCenter;
    private RealmHandler handler;
    private final AnimationSet set = new AnimationSet(true);
    private LinearLayout nothing_Linear;
    private TextView title;
    private OnFavoriteListChanged onFavoriteListChanged;

    List<MainDataClass> finList = new ArrayList<>();

    private AllMusicsRecyclerViewAdapter allMusicsRecyclerViewAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favoritelist, container, false);
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
        allMusicsRecyclerViewAdapter = new AllMusicsRecyclerViewAdapter(finList, getActivity(), getActivity().getSupportFragmentManager(), set, sharedPreferencesCenter, "FavoriteList|FavoriteList");
        mainRecyclerView.setAdapter(allMusicsRecyclerViewAdapter);
        sendPath();
    }

    private void sendPath() {
        Intent intent = new Intent(getActivity(), PlayerService.class);
        intent.setAction("com.dust.exmusic.ACTION_SEND_PATH");
        getActivity().startService(intent);
    }

    private void setUpList() {
        finList.clear();
        String[] list = sharedPreferencesCenter.getFavoriteListPaths();
        if (list[0].equals(""))
            list = new String[]{};

        for (int i = 0; i < list.length; i++) {
            MainDataClass dataClass = handler.getMusicDataByPath(list[i]);
            if (dataClass == null) {
                sharedPreferencesCenter.removeFavoriteList(list[i]);
                continue;
            }
            finList.add(dataClass);
        }

        if (finList.isEmpty()) {
            nothing_Linear.setVisibility(View.VISIBLE);
            title.setVisibility(View.INVISIBLE);
        } else {
            nothing_Linear.setVisibility(View.GONE);
            title.setVisibility(View.VISIBLE);
        }
    }

    private void setUpViews(View view) {
        mainRecyclerView = (RecyclerView) view.findViewById(R.id.mainRecyclerView);
        title = (TextView) view.findViewById(R.id.title);
        nothing_Linear = (LinearLayout) view.findViewById(R.id.nothing_Linear);
        mainRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void onStart() {
        super.onStart();
        onFavoriteListChanged = new OnFavoriteListChanged();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
           getActivity().registerReceiver(onFavoriteListChanged, new IntentFilter("com.dust.exmusic.OnFavoriteListChanged"),RECEIVER_EXPORTED);
        else
            getActivity().registerReceiver(onFavoriteListChanged, new IntentFilter("com.dust.exmusic.OnFavoriteListChanged"));
    }

    @Override
    public void onStop() {
        getActivity().unregisterReceiver(onFavoriteListChanged);
        super.onStop();
    }

    private class OnFavoriteListChanged extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                setUpList();
                allMusicsRecyclerViewAdapter.notifyDataSetChanged();
                sendPath();
            } catch (Exception e) {
            }
        }
    }

}
