package org.videolan.libvlc.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.player.vlcplayerplugin.R;


/* loaded from: classes.jar:org/videolan/libvlc/util/VLCVideoLayout.class */
public class VLCVideoLayout extends FrameLayout {
    public VLCVideoLayout(@NonNull Context context) {
        super(context);
        setupLayout(context);
    }

    public VLCVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupLayout(context);
    }

    public VLCVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupLayout(context);
    }

    public VLCVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setupLayout(context);
    }

    private void setupLayout(@NonNull Context context) {
        inflate(context, R.layout.vlc_video_layout, this);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setBackgroundResource(R.color.black);
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.height = -1;
        lp.width = -1;
        setLayoutParams(lp);
    }
}
