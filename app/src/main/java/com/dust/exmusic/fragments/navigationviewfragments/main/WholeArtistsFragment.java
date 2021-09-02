package com.dust.exmusic.fragments.navigationviewfragments.main;

import android.content.Intent;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dust.exmusic.R;
import com.dust.exmusic.adapters.recyclerviews.ArtistsRecyclerViewAdapter;
import com.dust.exmusic.dataclasses.MainDataClass;
import com.dust.exmusic.fragments.searchfragments.ArtistsSearchFragment;
import com.dust.exmusic.realm.RealmHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class WholeArtistsFragment extends Fragment {

    private RecyclerView mainRecyclerView;
    private RealmHandler handler;
    private NestedScrollView nestedMain;
    private int pagination = 1;
    private LinearLayout nothing_Linear;
    private TextView title;
    private List<MainDataClass> listMain = new ArrayList<>();
    private ArtistsRecyclerViewAdapter mAdapter;
    private ImageView backImg;
    private FloatingActionButton floatingButton;
    private AnimationSet set = new AnimationSet(true);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_whole_artists, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpAlphaAnimation();
        setUpViews(view);
        setUpRealmDb();
        setUpRecyclerView();
        setUpPagination();
        setUpBackImage(view);
        setUpFloatingActionButton();
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


    private void setUpFloatingActionButton() {
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.drawerLayout, new ArtistsSearchFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .addToBackStack("ArtistsSearchFragment")
                        .commit();
            }
        });
    }

    private void setUpBackImage(View view) {
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack("WholeArtistsFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().sendBroadcast(new Intent("com.dust.exmusic.UNLOCK_MAIN_DRAWER"));
    }

    @Override
    public void onStart() {
        Intent intent = new Intent("com.dust.exmusic.UNLOCK_MAIN_DRAWER");
        intent.putExtra("LOCK", true);
        getActivity().sendBroadcast(intent);
        super.onStart();
    }

    private void setUpViews(View view) {
        title = (TextView) view.findViewById(R.id.title);
        nothing_Linear = (LinearLayout) view.findViewById(R.id.nothing_Linear);
        mainRecyclerView = (RecyclerView) view.findViewById(R.id.mainRecyclerView);
        nestedMain = (NestedScrollView) view.findViewById(R.id.nestedMain);
        backImg = (ImageView) view.findViewById(R.id.backImg);
        floatingButton = (FloatingActionButton) view.findViewById(R.id.floatingButton);
        mainRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    }
}
