package com.dust.exmusic.fragments.others;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dust.exmusic.R;
import com.dust.exmusic.customviews.CTextView;
import com.dust.exmusic.dataclasses.MainDataClass;
import com.dust.exmusic.dataproviders.MetaDataLoader;
import com.dust.exmusic.interfaces.OnLoadPicture;

import de.hdodenhof.circleimageview.CircleImageView;

public class PlayerPartFragment extends Fragment {

    private CTextView txtName;
    private CTextView txtArtistName;
    private CTextView txtOtherDetails;
    private CircleImageView musicImage;
    private MainDataClass dataClass;

    private AnimationSet animationSet = new AnimationSet(true);

    public PlayerPartFragment(MainDataClass dataClass) {
        this.dataClass = dataClass;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.player_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpAnimationSet();
        setUpViews(view);
    }

    private void setUpAnimationSet() {
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.5f, 1f, 1.5f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(400);
        scaleAnimation.setFillAfter(true);

        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setDuration(400);
        alphaAnimation.setFillAfter(true);

        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
    }

    private void setUpViews(View view) {
        txtName = (CTextView) view.findViewById(R.id.txtName);
        txtArtistName = (CTextView) view.findViewById(R.id.txtArtistName);
        txtOtherDetails = (CTextView) view.findViewById(R.id.txtOtherDetails);
        musicImage = (CircleImageView) view.findViewById(R.id.musicImage);
        txtName.setText(dataClass.getMusicName());
        txtArtistName.setText(dataClass.getArtistName());
        txtOtherDetails.setText(dataClass.getYear());

        new MetaDataLoader(getActivity()).getPicture(dataClass.getPath(), new OnLoadPicture() {
            @Override
            public void onGetPicture(Bitmap bitmap) {
                if (bitmap != null) {
                    musicImage.setImageBitmap(bitmap);
                    musicImage.startAnimation(animationSet);
                } else {
                    musicImage.setImageResource(R.drawable.empty_music_pic);
                    musicImage.startAnimation(animationSet);
                }
            }
        });
    }

}
