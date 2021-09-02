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
import com.dust.exmusic.adapters.recyclerviews.FolderRecyclerViewAdapter;
import com.dust.exmusic.fragments.searchfragments.FolderSearchFragment;
import com.dust.exmusic.realm.RealmHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class WholeFolderFragment extends Fragment {

    private RecyclerView mainRecyclerView;
    private RealmHandler handler;
    private NestedScrollView nestedMain;
    private int pagination = 1;
    private LinearLayout nothing_Linear;
    private TextView title;
    private FolderRecyclerViewAdapter mAdapter;
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
                        .replace(R.id.drawerLayout, new FolderSearchFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .addToBackStack("FolderSearchFragment")
                        .commit();
            }
        });
    }

    private void setUpBackImage(View view) {
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack("WholeFolderFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }

    private void setUpRealmDb() {
        handler = new RealmHandler();
    }

    private void setUpRecyclerView() {
        mAdapter = new FolderRecyclerViewAdapter(handler.getFoldersFirstSong(), getActivity().getSupportFragmentManager(), set);
        mainRecyclerView.setAdapter(mAdapter);

        if (mAdapter.getItemCount() == 0) {
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
        title.setText(getActivity().getResources().getString(R.string.folders));
        nothing_Linear = (LinearLayout) view.findViewById(R.id.nothing_Linear);
        mainRecyclerView = (RecyclerView) view.findViewById(R.id.mainRecyclerView);
        nestedMain = (NestedScrollView) view.findViewById(R.id.nestedMain);
        backImg = (ImageView) view.findViewById(R.id.backImg);
        floatingButton = (FloatingActionButton) view.findViewById(R.id.floatingButton);
        mainRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    }
}
