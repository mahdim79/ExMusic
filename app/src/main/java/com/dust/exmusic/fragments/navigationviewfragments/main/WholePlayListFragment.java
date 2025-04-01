package com.dust.exmusic.fragments.navigationviewfragments.main;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dust.exmusic.R;
import com.dust.exmusic.adapters.recyclerviews.ChoosePlayListRecyclerViewAdapter;
import com.dust.exmusic.adapters.recyclerviews.PlayListRecyclerViewAdapter;
import com.dust.exmusic.customviews.CButton;
import com.dust.exmusic.customviews.CTextView;
import com.dust.exmusic.dataclasses.MainDataClass;
import com.dust.exmusic.dataclasses.PlayListDataClass;
import com.dust.exmusic.interfaces.OnSongChoose;
import com.dust.exmusic.realm.RealmHandler;
import com.dust.exmusic.sharedpreferences.SharedPreferencesCenter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WholePlayListFragment extends Fragment {

    private RecyclerView mainRecyclerView;
    private SharedPreferencesCenter sharedPreferencesCenter;
    private RealmHandler handler;
    private LinearLayout nothing_Linear;
    private ImageView backImg;
    private FloatingActionButton floatingButton;
    private SpeedDialView speedDial;
    private CTextView title;
    private AnimationSet set = new AnimationSet(true);

    private final List<String> removeList = new ArrayList<>();
    List<PlayListDataClass> list = new ArrayList<>();

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
        setUpAlphaAnimation();
        setUpSharedPreferences();
        setUpViews(view);
        setUpRealmDb();
        setUpRecyclerView();
        setUpBackImage();
        setUpSpeedDialView();
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

    private void setUpSpeedDialView() {

        speedDial.addActionItem(new SpeedDialActionItem.Builder(R.id.delete, R.drawable.ic_baseline_delete_24)
                .setLabel(getActivity().getResources().getString(R.string.delete))
                .setFabBackgroundColor(Color.RED)
                .setLabelColor(Color.WHITE)
                .setFabImageTintColor(Color.WHITE)
                .setLabelBackgroundColor(Color.RED)
                .create());

        speedDial.addActionItem(new SpeedDialActionItem.Builder(R.id.add, R.drawable.ic_baseline_add_24)
                .setLabel(getActivity().getResources().getString(R.string.add))
                .setFabBackgroundColor(ContextCompat.getColor(getActivity(), R.color.dark_green))
                .setLabelColor(Color.WHITE)
                .setFabImageTintColor(Color.WHITE)
                .setLabelBackgroundColor(ContextCompat.getColor(getActivity(), R.color.dark_green))
                .create());

        speedDial.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                switch (actionItem.getId()) {
                    case R.id.delete:
                        if (list.isEmpty())
                            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.nothingToDelete), Toast.LENGTH_SHORT).show();
                        else
                            startDeleteAction();
                        speedDial.close();
                        break;
                    case R.id.add:
                        setUpDialAddButton();
                        speedDial.close();
                        break;
                }
                return true;
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
                    getActivity().getSupportFragmentManager().popBackStack("WholePlayListFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }

    private void setUpRealmDb() {
        handler = new RealmHandler();
    }

    private void setUpSharedPreferences() {
        sharedPreferencesCenter = new SharedPreferencesCenter(getActivity());
    }

    private void setUpRecyclerView() {
        list.clear();
        String[] playLists = sharedPreferencesCenter.getPlayLists();
        if (playLists[0].equals("")) {
            mainRecyclerView.setAdapter(new PlayListRecyclerViewAdapter(new ArrayList<>(), getActivity().getSupportFragmentManager(), set));
            nothing_Linear.setVisibility(View.VISIBLE);
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
        mainRecyclerView.setAdapter(new PlayListRecyclerViewAdapter(list, getActivity().getSupportFragmentManager(), set));

    }

    private void setUpViews(View view) {
        speedDial = (SpeedDialView) view.findViewById(R.id.speedDial);
        speedDial.setVisibility(View.VISIBLE);
        nothing_Linear = (LinearLayout) view.findViewById(R.id.nothing_Linear);
        mainRecyclerView = (RecyclerView) view.findViewById(R.id.mainRecyclerView);
        backImg = (ImageView) view.findViewById(R.id.backImg);
        title = (CTextView) view.findViewById(R.id.title);
        floatingButton = (FloatingActionButton) view.findViewById(R.id.floatingButton);
        floatingButton.setImageResource(R.drawable.ic_baseline_delete_24);

        mainRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2, LinearLayoutManager.VERTICAL, false));

        if (sharedPreferencesCenter.getEnglishLanguage())
            title.setTypeface(null);
    }

    private void setUpDialAddButton() {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_addplaylist);
        TextInputLayout textInput = (TextInputLayout) dialog.findViewById(R.id.textInput);
        CButton add_button = (CButton) dialog.findViewById(R.id.add_button);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!textInput.getEditText().getText().toString().equals("")) {
                    List<String> currentPlayList = new ArrayList<>(Arrays.asList(sharedPreferencesCenter.getPlayLists()));
                    if (!currentPlayList.contains(textInput.getEditText().getText().toString())) {
                        int length = textInput.getEditText().getText().toString().length();
                        if (length >= 2 && length <= 15) {
                            sharedPreferencesCenter.addToPlayList(textInput.getEditText().getText().toString());
                            setUpRecyclerView();
                            dialog.dismiss();
                            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.added), Toast.LENGTH_SHORT).show();
                            sendPlayListDataChangedBroadCast();
                        } else {
                            textInput.setError(getActivity().getResources().getString(R.string.characterError));
                        }
                    } else {
                        textInput.setError(getActivity().getResources().getString(R.string.existList));
                    }
                } else {
                    textInput.setError(getActivity().getResources().getString(R.string.insertListName));
                }
            }
        });
        dialog.setCancelable(true);
        dialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().sendBroadcast(new Intent("com.dust.exmusic.UNLOCK_MAIN_DRAWER"));
    }

    private void startDeleteAction() {
        DELETE_MODE = DELETE_ON;
        title.setText(getResources().getString(R.string.deleteItem, removeList.size()));
        speedDial.setVisibility(View.GONE);
        ChoosePlayListRecyclerViewAdapter adapter2 = new ChoosePlayListRecyclerViewAdapter(getActivity(), list, new OnSongChoose() {
            @Override
            public void onSongSelected(String path) {
                removeList.add(path);
                title.setText(getResources().getString(R.string.deleteItem, removeList.size()));
                floatingButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSongDeSelected(String path) {
                removeList.remove(path);
                title.setText(getResources().getString(R.string.deleteItem, removeList.size()));
                if (removeList.isEmpty())
                    floatingButton.setVisibility(View.GONE);
            }
        }, set);
        floatingButton.setImageResource(R.drawable.ic_baseline_delete_24);
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < removeList.size(); i++) {
                    sharedPreferencesCenter.removePlayList(removeList.get(i));
                    handler.deletePlayListSongs(removeList.get(i));
                }
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.done), Toast.LENGTH_SHORT).show();
                resetDeleteMode();
                sendPlayListDataChangedBroadCast();
            }
        });

        mainRecyclerView.setAdapter(adapter2);

    }

    private void resetDeleteMode() {
        title.setText(getActivity().getResources().getString(R.string.playList));
        removeList.clear();
        floatingButton.setVisibility(View.GONE);
        setUpRecyclerView();
        speedDial.setVisibility(View.VISIBLE);
        DELETE_MODE = DELETE_OFF;
    }

    private void sendPlayListDataChangedBroadCast() {
        requireContext().sendBroadcast(new Intent("com.dust.exmusic.OnPlayListChanged"));
    }

    @Override
    public void onStart() {
        Intent intent = new Intent("com.dust.exmusic.UNLOCK_MAIN_DRAWER");
        intent.putExtra("LOCK", true);
        getActivity().sendBroadcast(intent);
        super.onStart();
    }
}
