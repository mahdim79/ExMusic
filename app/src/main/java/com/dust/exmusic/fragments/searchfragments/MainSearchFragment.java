package com.dust.exmusic.fragments.searchfragments;

import android.content.Context;
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
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dust.exmusic.R;
import com.dust.exmusic.adapters.recyclerviews.AllMusicsRecyclerViewAdapter;
import com.dust.exmusic.dataclasses.MainDataClass;
import com.dust.exmusic.dataproviders.SearchCenter;
import com.dust.exmusic.interfaces.OnSearchMainData;
import com.dust.exmusic.sharedpreferences.SharedPreferencesCenter;

import java.util.ArrayList;
import java.util.List;

public class MainSearchFragment extends Fragment {
    private ImageView backImg;
    private ImageView nothing_image;
    private EditText searchEditText;
    private LinearLayout nothing_Linear;
    private RecyclerView mainRecyclerView;
    private Handler handler;
    private Runnable runnable;
    private SearchCenter searchCenter;
    private List<MainDataClass> finList = new ArrayList<>();
    private AllMusicsRecyclerViewAdapter mAdapter;
    private ProgressBar searchPb;
    private final AnimationSet set = new AnimationSet(true);


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_artists, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpViews(view);
        setUpAlphaAnimation();
        setUpSearchCenter();
        setUpRecyclerView();
        setUpEditText();
        setUpBackImg();
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

    private void setUpBackImg() {
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                getActivity().getSupportFragmentManager().popBackStack("MainSearchFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }

    private void setUpRecyclerView() {
        mAdapter = new AllMusicsRecyclerViewAdapter(finList, getActivity(), getActivity().getSupportFragmentManager(), set, new SharedPreferencesCenter(getActivity()), "ALL|ALL");
        mainRecyclerView.setAdapter(mAdapter);
    }

    private void setUpSearchCenter() {
        searchCenter = new SearchCenter();
    }

    private void setUpEditText() {
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
                    finList.clear();
                    nothing_Linear.setVisibility(View.VISIBLE);
                    mAdapter.notifyDataSetChanged();
                    try {
                        handler.removeCallbacks(runnable);
                    } catch (Exception e) {
                    }
                    return;
                } else {
                    nothing_Linear.setVisibility(View.GONE);
                }

                if (handler != null)
                    handler.removeCallbacks(runnable);

                createSearchRequest(searchEditText.getText().toString());
            }
        });
    }

    private void createSearchRequest(String text) {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                searchPb.setVisibility(View.VISIBLE);
                nothing_Linear.setVisibility(View.GONE);
                searchCenter.searchMainData(text, new OnSearchMainData() {
                    @Override
                    public void onComplete(List<MainDataClass> results) {
                        finList.clear();
                        try {
                            finList.addAll(results.subList(0, 10));
                        } catch (Exception e) {
                            finList.clear();
                            finList.addAll(results);
                        }
                        searchPb.setVisibility(View.GONE);
                        if (finList.isEmpty())
                            nothing_Linear.setVisibility(View.VISIBLE);
                        else
                            nothing_Linear.setVisibility(View.GONE);
                        mAdapter.notifyDataSetChanged();
                    }
                });
                handler = null;
            }
        };
        handler.postDelayed(runnable, 750);
    }

    private void setUpViews(View view) {
        backImg = (ImageView) view.findViewById(R.id.backImg);
        nothing_image = (ImageView) view.findViewById(R.id.nothing_image);
        searchEditText = (EditText) view.findViewById(R.id.searchEditText);
        nothing_Linear = (LinearLayout) view.findViewById(R.id.nothing_Linear);
        mainRecyclerView = (RecyclerView) view.findViewById(R.id.mainRecyclerView);
        searchPb = (ProgressBar) view.findViewById(R.id.searchPb);
        nothing_image.setImageResource(R.drawable.albums);

        mainRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    }
}
