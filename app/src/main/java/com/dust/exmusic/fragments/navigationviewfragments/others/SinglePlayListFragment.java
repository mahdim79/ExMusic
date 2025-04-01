package com.dust.exmusic.fragments.navigationviewfragments.others;

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
import com.dust.exmusic.activities.PlayerActivity;
import com.dust.exmusic.adapters.recyclerviews.AllMusicsRecyclerViewAdapter;
import com.dust.exmusic.adapters.recyclerviews.ChooseSongRecyclerViewAdapter;
import com.dust.exmusic.customviews.CTextView;
import com.dust.exmusic.dataclasses.MainDataClass;
import com.dust.exmusic.interfaces.OnSongChoose;
import com.dust.exmusic.realm.RealmHandler;
import com.dust.exmusic.services.PlayerService;
import com.dust.exmusic.sharedpreferences.SharedPreferencesCenter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class SinglePlayListFragment extends Fragment {
    private final List<MainDataClass> finList = new ArrayList<>();
    private final List<String> removeList = new ArrayList<>();

    private RecyclerView mainRecyclerView;
    private SharedPreferencesCenter sharedPreferencesCenter;
    private RealmHandler handler;
    private final AnimationSet set = new AnimationSet(true);
    private LinearLayout nothing_Linear;
    private ImageView backImg;
    private ImageView addPlayList;
    private FloatingActionButton floatingButton;
    private CTextView title;

    private OnPlayListChanged onPlayListChanged;
    private OnReceivePath onReceivePath;

    private OnItemLongClick onItemLongClick;

    private int DELETE_OFF = 0;
    private int DELETE_ON = 1;
    private int DELETE_MODE = DELETE_OFF;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_whole_playlist, container, false);
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

    private void setUpBackImage() {
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DELETE_MODE == DELETE_ON)
                    resetDeleteMode();
                else
                    getActivity().getSupportFragmentManager().popBackStack("SinglePlayListFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
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

    private void setUpRealmDb() {
        handler = new RealmHandler();
    }

    private void setUpSharedPreferences() {
        sharedPreferencesCenter = new SharedPreferencesCenter(getActivity());
    }

    private void setUpRecyclerView() {
        finList.clear();

        finList.addAll(handler.getPlayListData(getArguments().getString("NAME")));

        if (finList.isEmpty()) {
            setUpEmptyView();
            addPlayList.setVisibility(View.GONE);
            floatingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.drawerLayout, ChooseSongFragment.newInstance(getArguments().getString("NAME")))
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .addToBackStack("ChooseSongFragment")
                            .commit();
                }
            });
        } else {
            floatingButton.setImageResource(R.drawable.gradient_play);
            nothing_Linear.setVisibility(View.GONE);
            floatingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sharedPreferencesCenter.setPlaylistActive(getArguments().getString("NAME"));
                    startPlayerActivity(finList.get(0).getPath(), getArguments().getString("NAME"));
                }
            });
        }

        mainRecyclerView.setAdapter(new AllMusicsRecyclerViewAdapter(finList, getActivity(), getActivity().getSupportFragmentManager(), set, sharedPreferencesCenter, setSeparatedListMode("PlayList", getArguments().getString("NAME"))));
        sendPath();
    }

    private String setSeparatedListMode(String type, String name) {
        return type + "|" + name;
    }


    private void setUpViews(View view) {
        nothing_Linear = (LinearLayout) view.findViewById(R.id.nothing_Linear);
        mainRecyclerView = (RecyclerView) view.findViewById(R.id.mainRecyclerView);
        backImg = (ImageView) view.findViewById(R.id.backImg);
        title = (CTextView) view.findViewById(R.id.title);
        addPlayList = (ImageView) view.findViewById(R.id.addPlayList);
        addPlayList.setVisibility(View.VISIBLE);
        floatingButton = (FloatingActionButton) view.findViewById(R.id.floatingButton);
        floatingButton.setVisibility(View.VISIBLE);
        mainRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        addPlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.drawerLayout, ChooseSongFragment.newInstance(getArguments().getString("NAME")))
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack("ChooseSongFragment")
                        .commit();
            }
        });

        if (sharedPreferencesCenter.getEnglishLanguage())
            title.setTypeface(null);

        title.setText(getArguments().getString("NAME"));
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent("com.dust.exmusic.UNLOCK_MAIN_DRAWER");
        intent.putExtra("LOCK", true);
        getActivity().sendBroadcast(intent);

        onItemLongClick = new OnItemLongClick();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            getActivity().registerReceiver(onItemLongClick, new IntentFilter("com.dust.exmusic.OnItemLongClick"),RECEIVER_EXPORTED);
        }else {
            getActivity().registerReceiver(onItemLongClick, new IntentFilter("com.dust.exmusic.OnItemLongClick"));
        }
        onPlayListChanged = new OnPlayListChanged();
        onReceivePath = new OnReceivePath();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            getActivity().registerReceiver(onPlayListChanged, new IntentFilter("com.dust.exmusic.OnPlayListChanged"),RECEIVER_EXPORTED);
            getActivity().registerReceiver(onReceivePath, new IntentFilter("com.dust.exmusic.OnReceivePath"),RECEIVER_EXPORTED);
        }else {
            getActivity().registerReceiver(onPlayListChanged, new IntentFilter("com.dust.exmusic.OnPlayListChanged"));
            getActivity().registerReceiver(onReceivePath, new IntentFilter("com.dust.exmusic.OnReceivePath"));
        }

    }

    @Override
    public void onStop() {
        getActivity().unregisterReceiver(onPlayListChanged);
        getActivity().unregisterReceiver(onReceivePath);
        getActivity().unregisterReceiver(onItemLongClick);
        getActivity().sendBroadcast(new Intent("com.dust.exmusic.UNLOCK_MAIN_DRAWER"));
        super.onStop();
    }

    private class OnPlayListChanged extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            setUpRecyclerView();
        }
    }

    private class OnReceivePath extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (checkPlayListPlaying(intent.getExtras().getString("PATH"))) {
                if (sharedPreferencesCenter.getPlaylistActive().equals(getArguments().getString("NAME"))) {
                    if (intent.getExtras().getBoolean("IS_PLAYING")) {
                        floatingButton.setImageResource(R.drawable.gradient_pause);
                        floatingButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                setUpMajorAction("");
                            }
                        });
                    } else {
                        floatingButton.setImageResource(R.drawable.gradient_play);
                        floatingButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                setUpMajorAction(getArguments().getString("NAME"));
                            }
                        });
                    }
                } else {
                    if (finList.isEmpty()) {
                        setUpEmptyView();
                    } else {
                        nothing_Linear.setVisibility(View.GONE);
                        floatingButton.setImageResource(R.drawable.gradient_play);
                        floatingButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                setUpMajorAction(getArguments().getString("NAME"));
                                startPlayerActivity(finList.get(0).getPath(), getArguments().getString("NAME"));
                            }
                        });
                    }
                }
            } else {
                if (finList.isEmpty()) {
                    setUpEmptyView();
                } else {
                    nothing_Linear.setVisibility(View.GONE);
                    floatingButton.setImageResource(R.drawable.gradient_play);
                    floatingButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            setUpMajorAction(getArguments().getString("NAME"));
                            startPlayerActivity(finList.get(0).getPath(), getArguments().getString("NAME"));
                        }
                    });
                }
            }
        }
    }

    private void setUpEmptyView() {
        nothing_Linear.setVisibility(View.VISIBLE);
        floatingButton.setImageResource(R.drawable.ic_baseline_add_24);
    }

    private void setUpMajorAction(String activePlayList) {
        sharedPreferencesCenter.setPlaylistActive(activePlayList);
        sendPlayPauseAction();
        sendPath();
    }

    private void sendPath() {
        Intent intent = new Intent(getActivity(), PlayerService.class);
        intent.setAction("com.dust.exmusic.ACTION_SEND_PATH");
        getActivity().startService(intent);
    }

    private void sendPlayPauseAction() {
        Intent intent = new Intent(getActivity(), PlayerService.class);
        intent.setAction("com.dust.exmusic.ACTION_PLAY");
        getActivity().startService(intent);
    }

    private void startPlayerActivity(String PATH, String PLAYLISTNAME) {
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        intent.putExtra("PATH", PATH);
        intent.putExtra("PLAY_LIST", getJoinedShuffleMode("PlayList", PLAYLISTNAME));
        getActivity().startActivity(intent);
    }

    private String getJoinedShuffleMode(String Type, String name) {
        return Type + "|" + name;
    }

    private boolean checkPlayListPlaying(String path) {
        for (int i = 0; i < finList.size(); i++) {
            if (finList.get(i).getPath().equals(path))
                return true;
        }
        return false;
    }

    public static SinglePlayListFragment newInstance(String playListName) {
        Bundle args = new Bundle();
        args.putString("NAME", playListName);
        SinglePlayListFragment fragment = new SinglePlayListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public class OnItemLongClick extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            DELETE_MODE = DELETE_ON;
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
                        handler.removeFromPlayList(getArguments().getString("NAME"), removeList.get(i));
                    }
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.done), Toast.LENGTH_SHORT).show();
                    resetDeleteMode();
                    sendPlayListDataChangedBroadCast();
                }
            });

            mainRecyclerView.setAdapter(adapter2);
        }
    }

    private void resetDeleteMode() {
        title.setText(getArguments().getString("NAME"));
        removeList.clear();
        floatingButton.setImageResource(R.drawable.gradient_play);
        floatingButton.setVisibility(View.VISIBLE);
        sendPath();
        setUpRecyclerView();
        DELETE_MODE = DELETE_OFF;
    }

    private void sendPlayListDataChangedBroadCast() {
        requireContext().sendBroadcast(new Intent("com.dust.exmusic.OnPlayListChanged"));
    }
}

