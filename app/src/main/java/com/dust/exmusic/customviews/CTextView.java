package com.dust.exmusic.customviews;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dust.exmusic.application.MyApplication;

public class CTextView extends androidx.appcompat.widget.AppCompatTextView {
    public CTextView(@NonNull Context context) {
        super(context);
        setTypeFace(context);
    }

    public CTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setTypeFace(context);
    }

    public CTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeFace(context);
    }

    private void setTypeFace(Context context) {
        MyApplication myApplication = (MyApplication) context.getApplicationContext();
        setTypeface(myApplication.setUpTypeFace());
    }
}
