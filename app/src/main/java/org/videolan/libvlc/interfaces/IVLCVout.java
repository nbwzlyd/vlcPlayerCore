package org.videolan.libvlc.interfaces;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import androidx.annotation.MainThread;

/* loaded from: classes.jar:org/videolan/libvlc/interfaces/IVLCVout.class */
public interface IVLCVout {

    /* loaded from: classes.jar:org/videolan/libvlc/interfaces/IVLCVout$Callback.class */
    public interface Callback {
        @MainThread
        void onSurfacesCreated(IVLCVout iVLCVout);

        @MainThread
        void onSurfacesDestroyed(IVLCVout iVLCVout);
    }

    /* loaded from: classes.jar:org/videolan/libvlc/interfaces/IVLCVout$OnNewVideoLayoutListener.class */
    public interface OnNewVideoLayoutListener {
        @MainThread
        void onNewVideoLayout(IVLCVout iVLCVout, int i, int i2, int i3, int i4, int i5, int i6);
    }

    @MainThread
    void setVideoView(SurfaceView surfaceView);

    @MainThread
    void setVideoView(TextureView textureView);

    @MainThread
    void setVideoSurface(Surface surface, SurfaceHolder surfaceHolder);

    @MainThread
    void setVideoSurface(SurfaceTexture surfaceTexture);

    @MainThread
    void setSubtitlesView(SurfaceView surfaceView);

    @MainThread
    void setSubtitlesView(TextureView textureView);

    @MainThread
    void setSubtitlesSurface(Surface surface, SurfaceHolder surfaceHolder);

    @MainThread
    void setSubtitlesSurface(SurfaceTexture surfaceTexture);

    @MainThread
    void attachViews(OnNewVideoLayoutListener onNewVideoLayoutListener);

    @MainThread
    void attachViews();

    @MainThread
    void detachViews();

    @MainThread
    boolean areViewsAttached();

    @MainThread
    void addCallback(Callback callback);

    @MainThread
    void removeCallback(Callback callback);

    @MainThread
    void sendMouseEvent(int i, int i2, int i3, int i4);

    @MainThread
    void setWindowSize(int i, int i2);
}
