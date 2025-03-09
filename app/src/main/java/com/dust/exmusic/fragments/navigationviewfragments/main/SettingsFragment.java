package com.dust.exmusic.fragments.navigationviewfragments.main;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.dust.exmusic.BuildConfig;
import com.dust.exmusic.R;
import com.dust.exmusic.customviews.CTextView;
import com.dust.exmusic.sharedpreferences.SharedPreferencesCenter;

public class SettingsFragment extends Fragment implements View.OnClickListener {
    private ImageView backImg;
    private LinearLayout soundSettings;
    private LinearLayout languageSettings;
    private LinearLayout themeSettings;
    private LinearLayout sourceSettings;
    private LinearLayout versionSettings;
    private CTextView currentLanguage;
    private CTextView version;

    private SharedPreferencesCenter sharedPreferencesCenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpSharedPreferencesCenter();
        setUpViews(view);
    }

    private void setUpSharedPreferencesCenter() {
        sharedPreferencesCenter = new SharedPreferencesCenter(getActivity());
    }

    private void setUpViews(View view) {
        backImg = (ImageView) view.findViewById(R.id.backImg);
        soundSettings = (LinearLayout) view.findViewById(R.id.soundSettings);
        languageSettings = (LinearLayout) view.findViewById(R.id.languageSettings);
        themeSettings = (LinearLayout) view.findViewById(R.id.themeSettings);
        sourceSettings = (LinearLayout) view.findViewById(R.id.sourceSettings);
        versionSettings = (LinearLayout) view.findViewById(R.id.versionSettings);
        currentLanguage = (CTextView) view.findViewById(R.id.currentLanguage);
        version = (CTextView) view.findViewById(R.id.version);

        if (sharedPreferencesCenter.getEnglishLanguage())
            currentLanguage.setText(getActivity().getResources().getString(R.string.english));
        else
            currentLanguage.setText(getActivity().getResources().getString(R.string.persian));

        soundSettings.setOnClickListener(this);
        languageSettings.setOnClickListener(this);
        themeSettings.setOnClickListener(this);
        sourceSettings.setOnClickListener(this);
        versionSettings.setOnClickListener(this);

        if (sharedPreferencesCenter.getEnglishLanguage())
            version.setTypeface(null);
        version.setText(getResources().getString(R.string.version, BuildConfig.VERSION_NAME));

        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack("SettingsFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.soundSettings:
                getActivity().startActivity(new Intent(Settings.ACTION_SOUND_SETTINGS));
                break;
            case R.id.languageSettings:
                Dialog dialog1 = new Dialog(getActivity());
                dialog1.setContentView(R.layout.choose_theme_dialog);
                RadioButton persianLanguage = dialog1.findViewById(R.id.darkRadioButton);
                persianLanguage.setText(getActivity().getResources().getString(R.string.persian));
                RadioButton englishLanguage = dialog1.findViewById(R.id.lightRadioButton);
                CTextView dialogTitle = dialog1.findViewById(R.id.dialogTitle);
                dialogTitle.setText(getActivity().getResources().getString(R.string.chooseLanguage));
                englishLanguage.setText(getActivity().getResources().getString(R.string.english));
                dialog1.findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean lastData = sharedPreferencesCenter.getEnglishLanguage();
                        sharedPreferencesCenter.setEnglishLanguage(englishLanguage.isChecked());

                        if (englishLanguage.isChecked() && lastData) {
                            dialog1.dismiss();
                        } else if (persianLanguage.isChecked() && !lastData) {
                            dialog1.dismiss();
                        } else {
                            dialog1.dismiss();
                            requireActivity().recreate();
                        }
                    }
                });
                if (sharedPreferencesCenter.getEnglishLanguage())
                    englishLanguage.setChecked(true);
                else
                    persianLanguage.setChecked(true);
                dialog1.show();

                break;
            case R.id.themeSettings:
                Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.choose_theme_dialog);
                RadioButton darkRadioButton = dialog.findViewById(R.id.darkRadioButton);
                RadioButton lightRadioButton = dialog.findViewById(R.id.lightRadioButton);
                dialog.findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean lastData = sharedPreferencesCenter.getDarkTheme();
                        sharedPreferencesCenter.setDarkTheme(darkRadioButton.isChecked());
                        if (darkRadioButton.isChecked() && lastData) {
                            dialog.dismiss();
                        } else if (lightRadioButton.isChecked() && !lastData) {
                            dialog.dismiss();
                        } else {
                            dialog.dismiss();
                            getActivity().recreate();
                        }
                    }
                });
                if (sharedPreferencesCenter.getDarkTheme())
                    darkRadioButton.setChecked(true);
                else
                    lightRadioButton.setChecked(true);
                dialog.show();
                break;
            case R.id.sourceSettings:
                String sourceUrl = "https://google.com";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl));
                getActivity().startActivity(Intent.createChooser(intent, getActivity().getResources().getString(R.string.openVia)));
                break;
            case R.id.versionSettings:
                Toast.makeText(getActivity(), BuildConfig.VERSION_NAME, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
