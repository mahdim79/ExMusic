package com.dust.exmusic.fragments.navigationviewfragments.others;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dust.exmusic.R;
import com.dust.exmusic.activities.PlayerActivity;
import com.dust.exmusic.adapters.recyclerviews.AllMusicsRecyclerViewAdapter;
import com.dust.exmusic.customviews.CTextView;
import com.dust.exmusic.dataclasses.MainDataClass;
import com.dust.exmusic.dataproviders.MetaDataLoader;
import com.dust.exmusic.interfaces.OnLoadPicture;
import com.dust.exmusic.realm.RealmHandler;
import com.dust.exmusic.services.PlayerService;
import com.dust.exmusic.sharedpreferences.SharedPreferencesCenter;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ArtistDetailsFragment extends Fragment {
    private ImageView backImg;
    private ImageView headerImage;
    private final AnimationSet set = new AnimationSet(true);
    private RecyclerView mainRecyclerView;
    private CollapsingToolbarLayout mainCollapsing;
    private FloatingActionButton floatingButton;
    private CTextView title;

    private SharedPreferencesCenter sharedPreferencesCenter;

    List<MainDataClass> data = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artist_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpViews(view);
        setUpSharedPreferences();
        setUpBackImg();
        setUpAlphaAnimation();
        setUpRecyclerView();
        setUpFloatingActionButton();
    }

    private void setUpSharedPreferences() {
        sharedPreferencesCenter = new SharedPreferencesCenter(getActivity());
    }

    private void setUpFloatingActionButton() {
        if (data.isEmpty())
            floatingButton.setVisibility(View.GONE);

        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferencesCenter.setPlaylistActive("");
                sharedPreferencesCenter.setShuffleMode(getJoinedShuffleMode("Artists", getArguments().getString("NAME")));
                Intent intent = new Intent(getActivity(), PlayerActivity.class);
                intent.putExtra("PATH", data.get(0).getPath());
                intent.putExtra("SHUFFLE_MODE", "Artists");
                intent.putExtra("PLAY_LIST", getJoinedShuffleMode("Artists", getArguments().getString("NAME")));
                getActivity().startActivity(intent);
            }
        });
    }

    private String getJoinedShuffleMode(String Type, String name) {
        return Type + "|" + name;
    }

    private void setUpRecyclerView() {
        data.addAll(new RealmHandler().getArtistSongs(getArguments().getString("NAME")));
        new MetaDataLoader(getActivity()).getPicture(data.get(0).getPath(), new OnLoadPicture() {
            @Override
            public void onGetPicture(Bitmap bitmap) {
                if (bitmap != null)
                    headerImage.setImageBitmap(bitmap);
                else
                    headerImage.setImageResource(R.drawable.empty_music_pic);

            }
        });
        mainRecyclerView.setAdapter(new AllMusicsRecyclerViewAdapter(data, getActivity(), getActivity().getSupportFragmentManager(), set, new SharedPreferencesCenter(getActivity()), setSeparatedListMode("Artists", getArguments().getString("NAME"))));
    }

    private String setSeparatedListMode(String type, String name) {
        return type + "|" + name;
    }

    private void setUpBackImg() {
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack("ArtistDetailsFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
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

    private void setUpViews(View view) {
        backImg = (ImageView) view.findViewById(R.id.backImg);
        floatingButton = (FloatingActionButton) view.findViewById(R.id.floatingButton);
        headerImage = (ImageView) view.findViewById(R.id.headerImage);
        mainRecyclerView = (RecyclerView) view.findViewById(R.id.mainRecyclerView);
        mainCollapsing = (CollapsingToolbarLayout) view.findViewById(R.id.mainCollapsing);
        title = (CTextView) view.findViewById(R.id.title);
        mainCollapsing.setTitle(getArguments().getString("NAME"));
        mainRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        title.setText(getArguments().getString("NAME"));
    }

    public static ArtistDetailsFragment newInstance(String artistName) {

        Bundle args = new Bundle();
        args.putString("NAME", artistName);
        ArtistDetailsFragment fragment = new ArtistDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        Intent intent1 = new Intent(getActivity(), PlayerService.class);
        intent1.setAction("com.dust.exmusic.ACTION_SEND_PATH");
        getActivity().startService(intent1);

        Intent intent = new Intent("com.dust.exmusic.UNLOCK_MAIN_DRAWER");
        intent.putExtra("LOCK", true);
        getActivity().sendBroadcast(intent);
        super.onStart();
    }

    @Override
    public void onStop() {
        getActivity().sendBroadcast(new Intent("com.dust.exmusic.UNLOCK_MAIN_DRAWER"));
        super.onStop();
    }
}
