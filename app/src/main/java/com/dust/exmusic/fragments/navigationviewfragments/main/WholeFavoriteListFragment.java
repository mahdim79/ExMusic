package com.dust.exmusic.fragments.navigationviewfragments.main;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dust.exmusic.R;
import com.dust.exmusic.adapters.recyclerviews.AllMusicsRecyclerViewAdapter;
import com.dust.exmusic.adapters.recyclerviews.ChooseSongRecyclerViewAdapter;
import com.dust.exmusic.customviews.CTextView;
import com.dust.exmusic.dataclasses.MainDataClass;
import com.dust.exmusic.fragments.navigationviewfragments.others.ChooseFavoriteSongFragment;
import com.dust.exmusic.interfaces.OnSongChoose;
import com.dust.exmusic.realm.RealmHandler;
import com.dust.exmusic.services.PlayerService;
import com.dust.exmusic.sharedpreferences.SharedPreferencesCenter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class WholeFavoriteListFragment extends Fragment {

    private ImageView backImg;
    private RecyclerView mainRecyclerView;
    private LinearLayout nothing_Linear;
    private FloatingActionButton floatingButton;
    private CTextView title;

    private final List<String> removeList = new ArrayList<>();
    List<MainDataClass> finList = new ArrayList<>();

    private final AnimationSet set = new AnimationSet(true);

    private OnItemLongClick onItemLongClick;

    private int DELETE_OFF = 0;
    private int DELETE_ON = 1;
    private int DELETE_MODE = DELETE_OFF;

    private SharedPreferencesCenter sharedPreferencesCenter;
    private RealmHandler handler;

    private OnFavoriteListChanged onFavoriteListChanged;

    private AllMusicsRecyclerViewAdapter allMusicsRecyclerViewAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_whole_favoritelist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpSharedPreferences();
        setUpViews(view);
        setUpAlphaAnimation();
        setUpRealmDb();
        setUpRecyclerView();
        setUpBackImage();
    }

    private void setUpList() {
        finList.clear();
        String[] list = sharedPreferencesCenter.getFavoriteListPaths();
        if (list[0].equals(""))
            list = new String[]{};

        for (int i = 0; i < list.length; i++) {
            finList.add(handler.getMusicDataByPath(list[i]));
        }
        if (finList.isEmpty()) {
            nothing_Linear.setVisibility(View.VISIBLE);
        } else {
            nothing_Linear.setVisibility(View.GONE);
        }
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

    private void setUpAlphaAnimation() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setDuration(500);
        alphaAnimation.setFillAfter(true);

        ScaleAnimation scaleAnimation = new ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(500);

        set.addAnimation(alphaAnimation);
        set.addAnimation(scaleAnimation);
    }

    private void setUpViews(View view) {
        backImg = (ImageView) view.findViewById(R.id.backImg);
        mainRecyclerView = (RecyclerView) view.findViewById(R.id.mainRecyclerView);
        nothing_Linear = (LinearLayout) view.findViewById(R.id.nothing_Linear);
        floatingButton = (FloatingActionButton) view.findViewById(R.id.floatingButton);
        title = (CTextView) view.findViewById(R.id.title);
        mainRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        setUpFloatingActionButton();

        if (sharedPreferencesCenter.getEnglishLanguage())
            title.setTypeface(null);
    }

    private void setUpFloatingActionButton() {
        floatingButton.setImageResource(R.drawable.ic_baseline_add_24);
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.drawerLayout, ChooseFavoriteSongFragment.newInstance())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack("ChooseFavoriteSongFragment")
                        .commit();
            }
        });
    }

    private void setUpBackImage() {
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DELETE_MODE == DELETE_ON)
                    resetDeleteMode();
                else
                    getActivity().getSupportFragmentManager().popBackStack("WholeFavoriteListFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }

    private void setUpRealmDb() {
        handler = new RealmHandler();
    }

    private void setUpSharedPreferences() {
        sharedPreferencesCenter = new SharedPreferencesCenter(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().sendBroadcast(new Intent("com.dust.exmusic.UNLOCK_MAIN_DRAWER"));
    }

    public class OnItemLongClick extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            DELETE_MODE = DELETE_ON;
            floatingButton.setVisibility(View.VISIBLE);
            removeList.add(finList.get(intent.getExtras().getInt("POSITION")).getPath());
            title.setText(getResources().getString(R.string.deleteSong, removeList.size()));
            ChooseSongRecyclerViewAdapter adapter2 = new ChooseSongRecyclerViewAdapter(getActivity(), finList, intent.getExtras().getInt("POSITION"), new OnSongChoose() {
                @Override
                public void onSongSelected(String path) {
                    removeList.add(path);
                    title.setText(getResources().getString(R.string.deleteSong, removeList.size()));
                    floatingButton.setVisibility(View.VISIBLE);
                }

                @Override
                public void onSongDeSelected(String path) {
                    removeList.remove(path);
                    title.setText(getResources().getString(R.string.deleteSong, removeList.size()));
                    if (removeList.isEmpty())
                        floatingButton.setVisibility(View.GONE);
                }
            }, set);
            floatingButton.setImageResource(R.drawable.ic_baseline_delete_24);
            floatingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (int i = 0; i < removeList.size(); i++) {
                        sharedPreferencesCenter.removeFavoriteList(removeList.get(i));
                    }
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.done), Toast.LENGTH_SHORT).show();
                    resetDeleteMode();
                    getActivity().sendBroadcast(new Intent("com.dust.exmusic.OnFavoriteListChanged"));
                }
            });

            mainRecyclerView.setAdapter(adapter2);
        }
    }

    private void resetDeleteMode() {
        title.setText(getActivity().getResources().getString(R.string.favoriteList));
        removeList.clear();
        setUpRecyclerView();
        DELETE_MODE = DELETE_OFF;
        setUpFloatingActionButton();
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent("com.dust.exmusic.UNLOCK_MAIN_DRAWER");
        intent.putExtra("LOCK", true);
        getActivity().sendBroadcast(intent);

        onItemLongClick = new OnItemLongClick();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            getActivity().registerReceiver(onItemLongClick, new IntentFilter("com.dust.exmusic.OnItemLongClick"), RECEIVER_EXPORTED);
        else
            getActivity().registerReceiver(onItemLongClick, new IntentFilter("com.dust.exmusic.OnItemLongClick"));
        onFavoriteListChanged = new OnFavoriteListChanged();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            getActivity().registerReceiver(onFavoriteListChanged, new IntentFilter("com.dust.exmusic.OnFavoriteListChanged"), RECEIVER_EXPORTED);
        else
            getActivity().registerReceiver(onFavoriteListChanged, new IntentFilter("com.dust.exmusic.OnFavoriteListChanged"));
    }

    @Override
    public void onStop() {
        getActivity().unregisterReceiver(onItemLongClick);
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
