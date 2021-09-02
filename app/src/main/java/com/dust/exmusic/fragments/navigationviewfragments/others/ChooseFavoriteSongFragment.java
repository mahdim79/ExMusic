package com.dust.exmusic.fragments.navigationviewfragments.others;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dust.exmusic.R;
import com.dust.exmusic.adapters.recyclerviews.ChooseSongRecyclerViewAdapter;
import com.dust.exmusic.customviews.CTextView;
import com.dust.exmusic.dataclasses.MainDataClass;
import com.dust.exmusic.dataproviders.SearchCenter;
import com.dust.exmusic.interfaces.OnSearchMainData;
import com.dust.exmusic.interfaces.OnSongChoose;
import com.dust.exmusic.realm.RealmHandler;
import com.dust.exmusic.sharedpreferences.SharedPreferencesCenter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ChooseFavoriteSongFragment extends Fragment {

    private RecyclerView mainRecyclerView;
    private RealmHandler handler;
    private List<MainDataClass> list = new ArrayList<>();
    private List<String> result = new ArrayList<>();
    private FloatingActionButton floatingButton;
    private LinearLayout nothing_Linear;
    private NestedScrollView nestedMain;
    private CTextView title;
    private ImageView backImg;
    private ImageView searchImage;
    private CardView searchBox;
    private EditText searchEditText;
    private SearchCenter searchCenter = new SearchCenter();

    private Handler handler2;
    private Runnable runnable;
    private final int SORT_BY_LAST_MODIFICATION_DATE = 1;

    private int pagination = 1;

    private ChooseSongRecyclerViewAdapter mAdapter;

    private SharedPreferencesCenter sharedPreferencesCenter;
    private boolean paginationOn = true;

    private AnimationSet set = new AnimationSet(true);

    private final int SEARCH_MODE_OFF = 0;
    private final int SEARCH_MODE_ON = 1;

    private int SEARCH_MODE = SEARCH_MODE_OFF;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_choose_song, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpAlphaAnimation();
        setUpSharedPreferences();
        setUpViews(view);
        setUpRealmDB();
        setUpRecyclerView();
        setUpPagination();
        setUpBackImage();
        setUpSearchBox();
        setUpSearchEditText();
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

    private void setUpSearchEditText() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (searchEditText.getText().toString().equals("")) {
                    list.clear();
                    nothing_Linear.setVisibility(View.VISIBLE);
                    mAdapter.notifyDataSetChanged();
                    try {
                        handler2.removeCallbacks(runnable);
                    } catch (Exception e) {
                    }
                    return;
                } else {
                    nothing_Linear.setVisibility(View.GONE);
                }

                if (handler2 != null)
                    handler2.removeCallbacks(runnable);

                createSearchRequest(searchEditText.getText().toString());
            }
        });
    }

    private void createSearchRequest(String text) {
        handler2 = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                nothing_Linear.setVisibility(View.GONE);
                searchCenter.searchMainData(text, new OnSearchMainData() {
                    @Override
                    public void onComplete(List<MainDataClass> results) {
                        list.clear();
                        try {
                            list.addAll(results.subList(0, 10));
                        } catch (Exception e) {
                            list.addAll(results);
                        }
                        optimizeList(list);
                        mAdapter.notifyDataSetChanged();
                        if (list.isEmpty())
                            nothing_Linear.setVisibility(View.VISIBLE);
                        else
                            nothing_Linear.setVisibility(View.GONE);
                    }
                });
                handler2 = null;
            }
        };
        handler2.postDelayed(runnable, 750);
    }

    private void setUpSearchBox() {
        searchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SEARCH_MODE == SEARCH_MODE_OFF) {
                    setSearchModeOn();
                } else {
                    setSearchModeOff();
                }
            }
        });
    }

    private void releaseSelectMode() {
        result.clear();
        floatingButton.setVisibility(View.GONE);
        title.setText(getActivity().getResources().getString(R.string.chooseMusic));
        getActivity().sendBroadcast(new Intent("com.dust.exmusic.OnUnSelectOrder"));
    }

    private void setSearchModeOn() {
        paginationOn = false;
        SEARCH_MODE = SEARCH_MODE_ON;
        floatingButton.setVisibility(View.GONE);
        searchBox.setVisibility(View.VISIBLE);
        list.clear();
        title.setText(getActivity().getResources().getString(R.string.chooseMusic));
        result.clear();
        mAdapter.notifyDataSetChanged();
    }

    private void setSearchModeOff() {
        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
        paginationOn = true;
        SEARCH_MODE = SEARCH_MODE_OFF;
        floatingButton.setVisibility(View.GONE);
        searchBox.setVisibility(View.GONE);
        list.clear();
        setPrimaryData();
        title.setText(getActivity().getResources().getString(R.string.chooseMusic));
        result.clear();
        mAdapter.notifyDataSetChanged();
    }

    private void setPrimaryData() {
        list.addAll(handler.getMainData(pagination, SORT_BY_LAST_MODIFICATION_DATE));
        optimizeList(list);
        if (list.isEmpty()) {
            nothing_Linear.setVisibility(View.VISIBLE);
        } else {
            nothing_Linear.setVisibility(View.GONE);
        }
    }

    private void setUpSharedPreferences() {
        sharedPreferencesCenter = new SharedPreferencesCenter(getActivity());
    }

    private void setUpBackImage() {
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SEARCH_MODE == SEARCH_MODE_ON) {
                    setSearchModeOff();
                    releaseSelectMode();
                    return;
                }
                if (result.isEmpty()) {
                    getActivity().getSupportFragmentManager().popBackStack("ChooseFavoriteSongFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                } else {
                    releaseSelectMode();
                }
            }
        });
    }

    private void setUpPagination() {
        nestedMain.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    pagination++;
                    List<MainDataClass> dataClasses = handler.getMainData(pagination, SORT_BY_LAST_MODIFICATION_DATE);
                    if (!dataClasses.isEmpty()) {
                        optimizeList(dataClasses);
                        int startCount = list.size();
                        list.addAll(dataClasses);
                        mAdapter.notifyItemRangeInserted(startCount, list.size() - startCount);
                    }
                }
            }
        });
    }

    private void setUpRealmDB() {
        handler = new RealmHandler();
    }

    private void optimizeList(List<MainDataClass> list) {
        List<MainDataClass> currentList = new ArrayList<>();
        String[] data = sharedPreferencesCenter.getFavoriteListPaths();
        if (data[0].equals(""))
            return;
        for (int i = 0; i < data.length; i++) {
            currentList.add(handler.getMusicDataByPath(data[i]));
        }
        for (int i = 0; i < currentList.size(); i++) {
            for (int j = 0; j < list.size(); j++) {
                if (currentList.get(i).getPath().equals(list.get(j).getPath()))
                    list.remove(j);
            }
        }
    }

    private void setUpRecyclerView() {
        setPrimaryData();
        mAdapter = new ChooseSongRecyclerViewAdapter(getActivity(), list, -1, new OnSongChoose() {
            @Override
            public void onSongSelected(String path) {
                result.add(path);
                title.setText(getResources().getString(R.string.deleteSong, result.size()));
                syncFloatingActionButton();
            }

            @Override
            public void onSongDeSelected(String path) {
                result.remove(path);
                if (result.isEmpty())
                    title.setText(getActivity().getResources().getString(R.string.chooseMusic));
                else
                    title.setText(getResources().getString(R.string.deleteSong, result.size()));
                syncFloatingActionButton();
            }
        }, set);

        mainRecyclerView.setAdapter(mAdapter);
    }

    private void syncFloatingActionButton() {
        if (result.isEmpty())
            floatingButton.setVisibility(View.GONE);
        else
            floatingButton.setVisibility(View.VISIBLE);
    }

    private void setUpViews(View view) {
        mainRecyclerView = (RecyclerView) view.findViewById(R.id.mainRecyclerView);
        floatingButton = (FloatingActionButton) view.findViewById(R.id.floatingButton);
        nothing_Linear = (LinearLayout) view.findViewById(R.id.nothing_Linear);
        nestedMain = (NestedScrollView) view.findViewById(R.id.nestedMain);
        title = (CTextView) view.findViewById(R.id.title);
        backImg = (ImageView) view.findViewById(R.id.backImg);
        searchImage = (ImageView) view.findViewById(R.id.searchImage);
        searchBox = (CardView) view.findViewById(R.id.searchBox);
        searchEditText = (EditText) view.findViewById(R.id.searchEditText);
        mainRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < result.size(); i++)
                    sharedPreferencesCenter.addToFavoriteListPaths(result.get(i));

                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.added), Toast.LENGTH_SHORT).show();
                getActivity().sendBroadcast(new Intent("com.dust.exmusic.OnFavoriteListChanged"));
                getActivity().getSupportFragmentManager().popBackStack("ChooseFavoriteSongFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        if (sharedPreferencesCenter.getEnglishLanguage())
            title.setTypeface(null);
    }

    public static ChooseFavoriteSongFragment newInstance() {
        Bundle args = new Bundle();
        ChooseFavoriteSongFragment fragment = new ChooseFavoriteSongFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        Intent intent = new Intent("com.dust.exmusic.UNLOCK_MAIN_DRAWER");
        intent.putExtra("LOCK", true);
        getActivity().sendBroadcast(intent);
        super.onStart();
    }
}