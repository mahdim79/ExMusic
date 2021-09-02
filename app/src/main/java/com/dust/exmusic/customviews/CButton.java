package com.dust.exmusic.customviews;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dust.exmusic.application.MyApplication;

public class CButton extends androidx.appcompat.widget.AppCompatButton {
    public CButton(@NonNull Context context) {
        super(context);
        setUpTypeFace();
    }

    public CButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setUpTypeFace();
    }

    public CButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUpTypeFace();
    }

    private void setUpTypeFace() {
        MyApplication myApplication = (MyApplication) getContext().getApplicationContext();
        setTypeface(myApplication.setUpTypeFace());
    }

}
