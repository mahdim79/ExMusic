package com.dust.exmusic.fragments.homepagefragments;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dust.exmusic.R;
import com.dust.exmusic.adapters.recyclerviews.AllMusicsRecyclerViewAdapter;
import com.dust.exmusic.dataclasses.MainDataClass;
import com.dust.exmusic.dataproviders.MainDataProvider;
import com.dust.exmusic.interfaces.OnLoadData;
import com.dust.exmusic.interfaces.OnMainDataAdded;
import com.dust.exmusic.realm.RealmHandler;
import com.dust.exmusic.services.PlayerService;
import com.dust.exmusic.sharedpreferences.SharedPreferencesCenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.content.Context.ACTIVITY_SERVICE;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView title;
    private List<MainDataClass> totalList = new ArrayList<>();
    private RealmHandler dbHandler;
    private NestedScrollView nestedMain;
    private LinearLayout nothing_Linear;
    private int pagination = 1;
    private final AnimationSet set = new AnimationSet(true);
    private SharedPreferencesCenter sharedPreferencesCenter;
    private AllMusicsRecyclerViewAdapter mAdapter;
    private Spinner sortSpinner;

    private final int SORT_BY_NAME = 1;
    private final int SORT_BY_LAST_MODIFICATION_DATE = 0;
    private final int SORT_BY_Year = 2;

    private int SortType = SORT_BY_LAST_MODIFICATION_DATE;

    private ActivityResultLauncher<String[]> externalStorageLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @Override
        public void onActivityResult(Map<String, Boolean> result) {
            checkPermissions();
        }
    });

    private ActivityResultLauncher<String> externalRecordAudio = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            checkPermissions();
        }
    });

    private ActivityResultLauncher<String> postNotificationLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {}
    });

    private ActivityResultLauncher<Intent> externalStorageLauncherS = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            checkPermissions();
        }
    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpViews(view);
        setUpSharedPreferences();
        setUpAlphaAnimation();
        setUpDataBase();
        checkPermissions();
    }

    @Override
    public void onResume() {
        Log.i("HomeFragment","onResume");
        super.onResume();
    }

    private void setUpSortSpinner() {
        sortSpinner.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, new String[]{getActivity().getResources().getString(R.string.freshes), getActivity().getResources().getString(R.string.name), getActivity().getResources().getString(R.string.newest)}));
        sortSpinner.setSelection(sharedPreferencesCenter.getSortType());
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        if (SortType != SORT_BY_LAST_MODIFICATION_DATE) {
                            startSorting(SORT_BY_LAST_MODIFICATION_DATE);
                        }
                        break;
                    case 1:
                        if (SortType != SORT_BY_NAME) {
                            startSorting(SORT_BY_NAME);
                        }
                        break;
                    case 2:
                        if (SortType != SORT_BY_Year) {
                            startSorting(SORT_BY_Year);
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setUpSharedPreferences() {
        sharedPreferencesCenter = new SharedPreferencesCenter(getActivity());
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

    private void sendPath() {
        if (checkServiceRunning()) {
            Intent intent = new Intent(getActivity(), PlayerService.class);
            intent.setAction("com.dust.exmusic.ACTION_SEND_PATH");
            getActivity().startService(intent);
        }
    }

    private boolean checkServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (runningServiceInfo.service.getClassName().equals(PlayerService.class.getName()))
                return true;
        }
        return false;
    }

    private void setUpPagination() {
        nestedMain.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    pagination++;
                    List<MainDataClass> dataClasses = dbHandler.getMainData(pagination, SortType);
                    if (!dataClasses.isEmpty()) {
                        int startCount = totalList.size();
                        totalList.addAll(dataClasses);
                        mAdapter.notifyItemRangeInserted(startCount, totalList.size() - startCount);
                        sendPath();
                    }
                }
            }
        });
    }

    private void setUpDataBase() {
        dbHandler = new RealmHandler();
    }

    private void startSorting(int SortType) {
        if (mAdapter == null)
            return;
        this.SortType = SortType;
        sharedPreferencesCenter.setSortType(SortType);
        pagination = 1;
        totalList.clear();
        List<MainDataClass> currentList = dbHandler.getMainData(pagination, SortType);
        if (!currentList.isEmpty()) {
            totalList.addAll(currentList);
            mAdapter.notifyDataSetChanged();
            sendPath();
        }
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            if (!Environment.isExternalStorageManager()){
                Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                permissionIntent.setData(Uri.fromParts("package", requireContext().getPackageName(), null));
                externalStorageLauncherS.launch(permissionIntent);
                return;
            }
        }else {
            if (requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || requireContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                externalStorageLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE});
                return;
            }
        }

        initMusicListOptions();

        if (requireContext().checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            externalRecordAudio.launch(Manifest.permission.RECORD_AUDIO);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (requireContext().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                postNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

    }

    private void initMusicListOptions() {
        setUpRecyclerView();
        setUpPagination();
        setUpSortSpinner();
    }

    private void setUpRecyclerView() {
        List<MainDataClass> currentList = dbHandler.getMainData(pagination, sharedPreferencesCenter.getSortType());
        if (!currentList.isEmpty()) {
            totalList.addAll(currentList);
            mAdapter = new AllMusicsRecyclerViewAdapter(totalList, requireContext(), getActivity().getSupportFragmentManager(), set, sharedPreferencesCenter, "ALL|ALL");
            recyclerView.setAdapter(mAdapter);
            progressBar.setVisibility(View.GONE);
            sendPath();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new MainDataProvider().getMainData(new OnLoadData() {
                    @Override
                    public void onLoadData(List<MainDataClass> list) {
                        dbHandler.insertMainData(list, new OnMainDataAdded() {
                            @Override
                            public void onMainDataAdded() {
                                try {
                                    List<MainDataClass> dataClasses = dbHandler.getMainData(pagination, SortType);
                                    if (dataClasses.isEmpty()) {
                                        nothing_Linear.setVisibility(View.VISIBLE);
                                        title.setVisibility(View.INVISIBLE);
                                    } else {
                                        nothing_Linear.setVisibility(View.GONE);
                                        title.setVisibility(View.VISIBLE);
                                    }

                                    totalList.clear();
                                    totalList.addAll(dataClasses);

                                    if (mAdapter == null) {
                                        mAdapter = new AllMusicsRecyclerViewAdapter(totalList, getActivity(), getActivity().getSupportFragmentManager(), set, sharedPreferencesCenter, "ALL|ALL");
                                        recyclerView.setAdapter(mAdapter);
                                    } else {
                                        mAdapter.notifyDataSetChanged();
                                    }
                                    progressBar.setVisibility(View.GONE);

                                    getActivity().sendBroadcast(new Intent("com.dust.exmusic.OnPlayListChanged"));
                                    getActivity().sendBroadcast(new Intent("com.dust.exmusic.OnFavoriteListChanged"));
                                    getActivity().sendBroadcast(new Intent("com.dust.exmusic.OnFolderListChanged"));

                                    sendPath();

                                    startSorting(SORT_BY_NAME);
                                } catch (Exception e) {
                                }
                            }
                        });
                    }
                });
            }
        }, 10);
    }

    private void setUpViews(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.mainRecyclerView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        nestedMain = (NestedScrollView) view.findViewById(R.id.nestedMain);
        title = (TextView) view.findViewById(R.id.title);
        nothing_Linear = (LinearLayout) view.findViewById(R.id.nothing_Linear);
        sortSpinner = (Spinner) view.findViewById(R.id.sortSpinner);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    }

}
