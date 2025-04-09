package org.videolan.libvlc.media;

import android.content.Context;
import android.graphics.Canvas;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.MediaController;
import java.io.InputStream;
import java.util.Map;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.interfaces.ILibVLC;

/* loaded from: classes.jar:org/videolan/libvlc/media/VideoView.class */
public class VideoView extends SurfaceView implements MediaController.MediaPlayerControl {
    private static ILibVLC sILibVLC;

    public VideoView(Context context) {
        super(context);
        sILibVLC = new LibVLC(context, null);
    }

    public VideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public VideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
    }

    public int resolveAdjustedSize(int desiredSize, int measureSpec) {
        return getDefaultSize(desiredSize, measureSpec);
    }

    public void setVideoPath(String path) {
        new Media(sILibVLC, path);
    }

    public void setVideoURI(Uri uri) {
        new Media(sILibVLC, uri);
    }

    public void setVideoURI(Uri uri, Map<String, String> headers) {
        setVideoURI(uri);
    }

    public void addSubtitleSource(InputStream is, MediaFormat format) {
    }

    public void setMediaController(MediaController controller) {
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener l) {
    }

    public void setOnErrorListener(MediaPlayer.OnErrorListener l) {
    }

    public void setOnInfoListener(MediaPlayer.OnInfoListener l) {
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }

    @Override // android.view.View
    public boolean onTrackballEvent(MotionEvent ev) {
        return super.onTrackballEvent(ev);
    }

    @Override // android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public void start() {
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public void pause() {
    }

    public void stopPlayback() {
    }

    public void suspend() {
    }

    public void resume() {
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public int getDuration() {
        return -1;
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public int getCurrentPosition() {
        return 0;
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public void seekTo(int msec) {
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public boolean isPlaying() {
        return false;
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public int getBufferPercentage() {
        return 0;
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public boolean canPause() {
        return false;
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public boolean canSeekBackward() {
        return false;
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public boolean canSeekForward() {
        return false;
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public int getAudioSessionId() {
        return 0;
    }

    @Override // android.view.SurfaceView, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override // android.view.SurfaceView, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override // android.view.SurfaceView, android.view.View
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    @Override // android.view.View
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override // android.view.SurfaceView, android.view.View
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
