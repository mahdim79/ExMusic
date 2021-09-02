package com.dust.exmusic.fragments.others;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.dust.exmusic.R;
import com.dust.exmusic.customviews.CButton;
import com.dust.exmusic.customviews.CTextView;
import com.dust.exmusic.dataclasses.MainDataClass;
import com.dust.exmusic.fragments.navigationviewfragments.main.WholePlayListFragment;
import com.dust.exmusic.realm.RealmHandler;
import com.dust.exmusic.sharedpreferences.SharedPreferencesCenter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MusicDetailsBottomDialog extends BottomSheetDialogFragment {

    private CTextView addToPlayList;
    private CTextView shareSong;
    private CTextView details;
    private SharedPreferencesCenter sharedPreferencesCenter;

    private String path;

    private RealmHandler handler;

    public MusicDetailsBottomDialog(String path) {

        this.path = path;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.music_bottomsheet_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpSharedPreferences();
        setUpRealmHandler();
        setUpViews(view);
    }

    private void setUpRealmHandler() {
        handler = new RealmHandler();
    }

    private void setUpSharedPreferences() {
        sharedPreferencesCenter = new SharedPreferencesCenter(getActivity());
    }

    private void setUpViews(View view) {
        addToPlayList = (CTextView) view.findViewById(R.id.addToPlayList);
        shareSong = (CTextView) view.findViewById(R.id.shareSong);
        details = (CTextView) view.findViewById(R.id.details);

        shareSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("audio/*");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path)));
                getActivity().startActivity(Intent.createChooser(intent, getActivity().getResources().getString(R.string.sendVia)));
            }
        });

        addToPlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.dialog_add_to_playlist);
                CButton submitButton = (CButton) dialog.findViewById(R.id.submitButton);
                Spinner playListSpinner = (Spinner) dialog.findViewById(R.id.playListSpinner);
                String[] playLists = sharedPreferencesCenter.getPlayLists();
                if (playLists[0].equals("")) {
                    playListSpinner.setVisibility(View.GONE);
                    submitButton.setText(getActivity().getResources().getString(R.string.addNewPlayList));
                } else {
                    playListSpinner.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, playLists));
                }
                FragmentActivity fragmentActivity = getActivity();
                submitButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (playLists[0].equals("")) {
                            dialog.dismiss();
                            MusicDetailsBottomDialog.this.dismiss();

                            fragmentActivity.getSupportFragmentManager().beginTransaction().replace(R.id.drawerLayout, new WholePlayListFragment())
                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                    .addToBackStack("WholePlayListFragment")
                                    .commit();

                            return;
                        }

                        if (checkMusicAvailabilityInPlayList(path, sharedPreferencesCenter.getPlayLists()[playListSpinner.getSelectedItemPosition()])) {
                            Toast.makeText(fragmentActivity, fragmentActivity.getResources().getString(R.string.alredyAdded), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        handler.setIntoPlayList(path, sharedPreferencesCenter.getPlayLists()[playListSpinner.getSelectedItemPosition()]);
                        fragmentActivity.sendBroadcast(new Intent("com.dust.exmusic.OnPlayListChanged"));
                        dialog.dismiss();
                        MusicDetailsBottomDialog.this.dismiss();
                        Toast.makeText(fragmentActivity, fragmentActivity.getResources().getString(R.string.added), Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show();
                MusicDetailsBottomDialog.this.dismiss();
            }
        });

        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.details_dialog);
                dialog.setCancelable(true);
                TextView txtPath = (TextView) dialog.findViewById(R.id.txtPath);
                TextView txtSize = (TextView) dialog.findViewById(R.id.txtSize);
                TextView txtLastModification = (TextView) dialog.findViewById(R.id.txtLastModification);

                //set data
                File file = new File(path);
                if (file.exists()) {

                    // set path
                    txtPath.setText(path);

                    // set size
                    String size = String.valueOf(file.length() / 1000);
                    if (size.length() <= 3) {
                        txtSize.setText(getActivity().getResources().getString(R.string.kiloByteSize, size));
                    } else {
                        double length = Double.parseDouble(size) / 1000;
                        txtSize.setText(getActivity().getResources().getString(R.string.megaByteSize, String.format(Locale.ENGLISH, "%.1f", length)));
                    }

                    // set last modification
                    Date date = new Date(file.lastModified());
                    String year = String.valueOf(date.getYear());
                    String modificationData = date.getDate() + "/" + date.getMonth() + "/20" + year.substring(1) + "   ,   " + String.format(Locale.ENGLISH, "%02d", date.getHours()) + ":" + String.format(Locale.ENGLISH, "%02d", date.getMinutes()) + ":" + String.format(Locale.ENGLISH, "%02d", date.getSeconds());
                    txtLastModification.setText(modificationData);

                    // identify Locale

                    if (!sharedPreferencesCenter.getEnglishLanguage()) {
                        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/far_mitra.ttf");
                        txtSize.setTypeface(typeface);
                        txtLastModification.setTypeface(typeface);
                    }
                }
                dialog.show();
            }
        });
    }

    private boolean checkMusicAvailabilityInPlayList(String path, String playListName) {
        List<MainDataClass> list = handler.getPlayListData(playListName);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getPath().equals(path))
                return true;
        }
        return false;
    }
}
