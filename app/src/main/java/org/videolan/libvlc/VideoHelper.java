package org.videolan.libvlc;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;


import com.player.vlcplayerplugin.R;

import org.videolan.libvlc.interfaces.IMedia;
import org.videolan.libvlc.interfaces.IVLCVout;
import org.videolan.libvlc.util.AndroidUtil;
import org.videolan.libvlc.util.DisplayManager;
import org.videolan.libvlc.util.VLCVideoLayout;

/* loaded from: classes.jar:org/videolan/libvlc/VideoHelper.class */
public class VideoHelper implements IVLCVout.OnNewVideoLayoutListener {
    private static final String TAG = "LibVLC/VideoHelper";
    private float mCustomScale;
    private FrameLayout mVideoSurfaceFrame;
    private DisplayManager mDisplayManager;
    private org.videolan.libvlc.MediaPlayer mMediaPlayer;
    private org.videolan.libvlc.MediaPlayer.ScaleType mCurrentScaleType = org.videolan.libvlc.MediaPlayer.ScaleType.SURFACE_BEST_FIT;
    private boolean mCurrentScaleCustom = false;
    private int mVideoHeight = 0;
    private int mVideoWidth = 0;
    private int mVideoVisibleHeight = 0;
    private int mVideoVisibleWidth = 0;
    private int mVideoSarNum = 0;
    private int mVideoSarDen = 0;
    private SurfaceView mVideoSurface = null;
    private SurfaceView mSubtitlesSurface = null;
    private TextureView mVideoTexture = null;
    private final Handler mHandler = new Handler();
    private View.OnLayoutChangeListener mOnLayoutChangeListener = null;

    public VideoHelper(org.videolan.libvlc.MediaPlayer player, VLCVideoLayout surfaceFrame, DisplayManager dm, boolean subtitles, boolean textureView) {
        init(player, surfaceFrame, dm, subtitles, !textureView);
    }

    private void init(org.videolan.libvlc.MediaPlayer player, VLCVideoLayout surfaceFrame, DisplayManager dm, boolean subtitles, boolean useSurfaceView) {
        this.mMediaPlayer = player;
        this.mDisplayManager = dm;
        boolean isPrimary = this.mDisplayManager == null || this.mDisplayManager.isPrimary();
        if (isPrimary) {
            this.mVideoSurfaceFrame = (FrameLayout) surfaceFrame.findViewById(R.id.player_surface_frame);
            if (useSurfaceView) {
                ViewStub stub = (ViewStub) this.mVideoSurfaceFrame.findViewById(R.id.surface_stub);
                this.mVideoSurface = stub != null ? (SurfaceView) stub.inflate() : (SurfaceView) this.mVideoSurfaceFrame.findViewById(R.id.surface_video);
                if (subtitles) {
                    ViewStub stub2 = (ViewStub) surfaceFrame.findViewById(R.id.subtitles_surface_stub);
                    this.mSubtitlesSurface = stub2 != null ? (SurfaceView) stub2.inflate() : (SurfaceView) surfaceFrame.findViewById(R.id.surface_subtitles);
                    this.mSubtitlesSurface.setZOrderMediaOverlay(true);
                    this.mSubtitlesSurface.getHolder().setFormat(-3);
                    return;
                }
                return;
            }
            ViewStub stub3 = (ViewStub) this.mVideoSurfaceFrame.findViewById(R.id.texture_stub);
            this.mVideoTexture = stub3 != null ? (TextureView) stub3.inflate() : (TextureView) this.mVideoSurfaceFrame.findViewById(R.id.texture_video);
        } else if (this.mDisplayManager.getPresentation() != null) {
            this.mVideoSurfaceFrame = this.mDisplayManager.getPresentation().getSurfaceFrame();
            this.mVideoSurface = this.mDisplayManager.getPresentation().getSurfaceView();
            this.mSubtitlesSurface = this.mDisplayManager.getPresentation().getSubtitlesSurfaceView();
        }
    }

    public void release() {
        if (this.mMediaPlayer.getVLCVout().areViewsAttached()) {
            detachViews();
        }
        this.mMediaPlayer = null;
        this.mVideoSurfaceFrame = null;
        this.mHandler.removeCallbacks(null);
        this.mVideoSurface = null;
        this.mSubtitlesSurface = null;
        this.mVideoTexture = null;
    }

    public void attachViews() {
        if (this.mVideoSurface == null && this.mVideoTexture == null) {
            return;
        }
        IVLCVout vlcVout = this.mMediaPlayer.getVLCVout();
        if (this.mVideoSurface != null) {
            vlcVout.setVideoView(this.mVideoSurface);
            if (this.mSubtitlesSurface != null) {
                vlcVout.setSubtitlesView(this.mSubtitlesSurface);
            }
        } else if (this.mVideoTexture != null) {
            vlcVout.setVideoView(this.mVideoTexture);
        } else {
            return;
        }
        vlcVout.attachViews(this);
        if (this.mOnLayoutChangeListener == null) {
            this.mOnLayoutChangeListener = new View.OnLayoutChangeListener() { // from class: org.videolan.libvlc.VideoHelper.1
                private final Runnable runnable = new Runnable() { // from class: org.videolan.libvlc.VideoHelper.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (VideoHelper.this.mVideoSurfaceFrame == null || VideoHelper.this.mOnLayoutChangeListener == null) {
                            return;
                        }
                        VideoHelper.this.updateVideoSurfaces();
                    }
                };

                @Override // android.view.View.OnLayoutChangeListener
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                        VideoHelper.this.mHandler.removeCallbacks(this.runnable);
                        VideoHelper.this.mHandler.post(this.runnable);
                    }
                }
            };
        }
        this.mVideoSurfaceFrame.addOnLayoutChangeListener(this.mOnLayoutChangeListener);
        this.mMediaPlayer.setVideoTrackEnabled(true);
    }

    void detachViews() {
        if (this.mOnLayoutChangeListener != null && this.mVideoSurfaceFrame != null) {
            this.mVideoSurfaceFrame.removeOnLayoutChangeListener(this.mOnLayoutChangeListener);
            this.mOnLayoutChangeListener = null;
        }
        this.mMediaPlayer.setVideoTrackEnabled(false);
        this.mMediaPlayer.getVLCVout().detachViews();
    }

    private void changeMediaPlayerLayout(int displayW, int displayH) {
        float scale;
        if (this.mMediaPlayer.isReleased()) {
            return;
        }
        if (this.mCurrentScaleCustom) {
            this.mMediaPlayer.setAspectRatio(null);
            this.mMediaPlayer.setNativeScale(this.mCustomScale);
            return;
        }
        switch (AnonymousClass2.$SwitchMap$org$videolan$libvlc$MediaPlayer$ScaleType[this.mCurrentScaleType.ordinal()]) {
            case 1:
                this.mMediaPlayer.setAspectRatio(null);
                this.mMediaPlayer.setNativeScale(0.0f);
                return;
            case 2:
            case 3:
                IMedia.VideoTrack vtrack = (IMedia.VideoTrack) this.mMediaPlayer.getSelectedTrack(1);
                if (vtrack == null) {
                    return;
                }
                boolean videoSwapped = vtrack.orientation == 5 || vtrack.orientation == 6;
                if (this.mCurrentScaleType == org.videolan.libvlc.MediaPlayer.ScaleType.SURFACE_FIT_SCREEN) {
                    int videoW = vtrack.width;
                    int videoH = vtrack.height;
                    if (videoSwapped) {
                        videoW = videoH;
                        videoH = videoW;
                    }
                    if (vtrack.sarNum != vtrack.sarDen) {
                        videoW = (videoW * vtrack.sarNum) / vtrack.sarDen;
                    }
                    float ar = videoW / videoH;
                    float dar = displayW / displayH;
                    if (dar >= ar) {
                        scale = displayW / videoW;
                    } else {
                        scale = displayH / videoH;
                    }
                    this.mMediaPlayer.setNativeScale(scale);
                    this.mMediaPlayer.setAspectRatio(null);
                    return;
                }
                this.mMediaPlayer.setNativeScale(0.0f);
                this.mMediaPlayer.setAspectRatio(!videoSwapped ? "" + displayW + ":" + displayH : "" + displayH + ":" + displayW);
                return;
            case 4:
                this.mMediaPlayer.setAspectRatio("16:9");
                this.mMediaPlayer.setNativeScale(0.0f);
                return;
            case 5:
                this.mMediaPlayer.setAspectRatio("16:10");
                this.mMediaPlayer.setNativeScale(0.0f);
                return;
            case 6:
                this.mMediaPlayer.setAspectRatio("221:100");
                this.mMediaPlayer.setNativeScale(0.0f);
                return;
            case 7:
                this.mMediaPlayer.setAspectRatio("235:100");
                this.mMediaPlayer.setNativeScale(0.0f);
                return;
            case 8:
                this.mMediaPlayer.setAspectRatio("239:100");
                this.mMediaPlayer.setNativeScale(0.0f);
                return;
            case IMedia.Meta.Setting /* 9 */:
                this.mMediaPlayer.setAspectRatio("5:4");
                this.mMediaPlayer.setNativeScale(0.0f);
                return;
            case IMedia.Meta.URL /* 10 */:
                this.mMediaPlayer.setAspectRatio("4:3");
                this.mMediaPlayer.setNativeScale(0.0f);
                return;
            case IMedia.Meta.Language /* 11 */:
                this.mMediaPlayer.setAspectRatio(null);
                this.mMediaPlayer.setNativeScale(1.0f);
                return;
            default:
                return;
        }
    }

    /* renamed from: org.videolan.libvlc.VideoHelper$2 */
    /* loaded from: classes.jar:org/videolan/libvlc/VideoHelper$2.class */
    public static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$org$videolan$libvlc$MediaPlayer$ScaleType = new int[org.videolan.libvlc.MediaPlayer.ScaleType.values().length];

        static {
            try {
                $SwitchMap$org$videolan$libvlc$MediaPlayer$ScaleType[org.videolan.libvlc.MediaPlayer.ScaleType.SURFACE_BEST_FIT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$org$videolan$libvlc$MediaPlayer$ScaleType[org.videolan.libvlc.MediaPlayer.ScaleType.SURFACE_FIT_SCREEN.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$org$videolan$libvlc$MediaPlayer$ScaleType[org.videolan.libvlc.MediaPlayer.ScaleType.SURFACE_FILL.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$org$videolan$libvlc$MediaPlayer$ScaleType[org.videolan.libvlc.MediaPlayer.ScaleType.SURFACE_16_9.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$org$videolan$libvlc$MediaPlayer$ScaleType[org.videolan.libvlc.MediaPlayer.ScaleType.SURFACE_16_10.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$org$videolan$libvlc$MediaPlayer$ScaleType[org.videolan.libvlc.MediaPlayer.ScaleType.SURFACE_221_1.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$org$videolan$libvlc$MediaPlayer$ScaleType[org.videolan.libvlc.MediaPlayer.ScaleType.SURFACE_235_1.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$org$videolan$libvlc$MediaPlayer$ScaleType[org.videolan.libvlc.MediaPlayer.ScaleType.SURFACE_239_1.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$org$videolan$libvlc$MediaPlayer$ScaleType[org.videolan.libvlc.MediaPlayer.ScaleType.SURFACE_5_4.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$org$videolan$libvlc$MediaPlayer$ScaleType[org.videolan.libvlc.MediaPlayer.ScaleType.SURFACE_4_3.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$org$videolan$libvlc$MediaPlayer$ScaleType[org.videolan.libvlc.MediaPlayer.ScaleType.SURFACE_ORIGINAL.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
        }
    }

    public void updateVideoSurfaces() {
        int sh;
        int sw;
        double vw;
        double ar;
        if (this.mMediaPlayer == null || this.mMediaPlayer.isReleased() || !this.mMediaPlayer.getVLCVout().areViewsAttached()) {
            return;
        }
        boolean isPrimary = this.mDisplayManager == null || this.mDisplayManager.isPrimary();
        Activity activity = !isPrimary ? null : AndroidUtil.resolveActivity(this.mVideoSurfaceFrame.getContext());
        if (activity != null) {
            sw = this.mVideoSurfaceFrame.getWidth();
            sh = this.mVideoSurfaceFrame.getHeight();
        } else if (this.mDisplayManager != null && this.mDisplayManager.getPresentation() != null && this.mDisplayManager.getPresentation().getWindow() != null) {
            sw = this.mDisplayManager.getPresentation().getWindow().getDecorView().getWidth();
            sh = this.mDisplayManager.getPresentation().getWindow().getDecorView().getHeight();
        } else {
            return;
        }
        if (sw * sh == 0) {
            Log.e(TAG, "Invalid surface size");
            return;
        }
        this.mMediaPlayer.getVLCVout().setWindowSize(sw, sh);
        View videoView = this.mVideoSurface;
        if (videoView == null) {
            videoView = this.mVideoTexture;
        }
        ViewGroup.LayoutParams lp = videoView.getLayoutParams();
        if (this.mVideoWidth * this.mVideoHeight == 0 || (AndroidUtil.isNougatOrLater && activity != null && activity.isInPictureInPictureMode())) {
            changeMediaPlayerLayout(sw, sh);
            lp.width = -1;
            lp.height = -1;
            videoView.setLayoutParams(lp);
            ViewGroup.LayoutParams lp2 = this.mVideoSurfaceFrame.getLayoutParams();
            lp2.width = -1;
            lp2.height = -1;
            this.mVideoSurfaceFrame.setLayoutParams(lp2);
            return;
        }
        if (lp.width == lp.height && lp.width == -1) {
            this.mMediaPlayer.setAspectRatio(null);
            this.mMediaPlayer.setNativeScale(0.0f);
        }
        double dw = sw;
        double dh = sh;
        boolean consideredPortrait = this.mVideoSurfaceFrame.getResources().getConfiguration().orientation == 1;
        if (this.mMediaPlayer.useOrientationFromBounds().booleanValue()) {
            consideredPortrait = sh > sw;
        }
        boolean isPortrait = isPrimary && consideredPortrait;
        if ((sw > sh && isPortrait) || (sw < sh && !isPortrait)) {
            dw = sh;
            dh = sw;
        }
        if (this.mVideoSarDen == this.mVideoSarNum) {
            vw = this.mVideoVisibleWidth;
            ar = this.mVideoVisibleWidth / this.mVideoVisibleHeight;
        } else {
            vw = (this.mVideoVisibleWidth * this.mVideoSarNum) / this.mVideoSarDen;
            ar = vw / this.mVideoVisibleHeight;
        }
        double dar = dw / dh;
        org.videolan.libvlc.MediaPlayer.ScaleType scaleType = this.mCurrentScaleType;
        if (this.mCurrentScaleCustom) {
            dh *= this.mCustomScale;
            dw *= this.mCustomScale;
            scaleType = org.videolan.libvlc.MediaPlayer.ScaleType.SURFACE_BEST_FIT;
        }
        switch (AnonymousClass2.$SwitchMap$org$videolan$libvlc$MediaPlayer$ScaleType[scaleType.ordinal()]) {
            case 1:
                if (dar < ar) {
                    dh = dw / ar;
                    break;
                } else {
                    dw = dh * ar;
                    break;
                }
            case 2:
                if (dar >= ar) {
                    dh = dw / ar;
                    break;
                } else {
                    dw = dh * ar;
                    break;
                }
            case 3:
                break;
            case IMedia.Meta.Language /* 11 */:
                dh = this.mVideoVisibleHeight;
                dw = vw;
                break;
            default:
                double ar2 = this.mCurrentScaleType.getRatio().floatValue();
                if (dar < ar2) {
                    dh = dw / ar2;
                    break;
                } else {
                    dw = dh * ar2;
                    break;
                }
        }
        lp.width = (int) Math.ceil((dw * this.mVideoWidth) / this.mVideoVisibleWidth);
        lp.height = (int) Math.ceil((dh * this.mVideoHeight) / this.mVideoVisibleHeight);
        videoView.setLayoutParams(lp);
        videoView.invalidate();
    }

    @Override // org.videolan.libvlc.interfaces.IVLCVout.OnNewVideoLayoutListener
    public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        if (width == 0 && height == 0 && visibleWidth == 0 && visibleHeight == 0 && sarNum == 0 && sarDen == 0) {
            this.mVideoVisibleHeight = 0;
            this.mVideoVisibleWidth = 0;
            this.mVideoHeight = 0;
            this.mVideoWidth = 0;
            this.mVideoSarDen = 0;
            this.mVideoSarNum = 0;
        } else {
            if (width != 0 && height != 0) {
                this.mVideoWidth = width;
                this.mVideoHeight = height;
            }
            if (visibleWidth != 0 && visibleHeight != 0) {
                this.mVideoVisibleWidth = visibleWidth;
                this.mVideoVisibleHeight = visibleHeight;
            }
            if (sarNum != 0 && sarDen != 0) {
                this.mVideoSarNum = sarNum;
                this.mVideoSarDen = sarDen;
            }
        }
        updateVideoSurfaces();
    }

    public void setVideoScale(org.videolan.libvlc.MediaPlayer.ScaleType type) {
        this.mCurrentScaleType = type;
        this.mCurrentScaleCustom = false;
        updateVideoSurfaces();
    }

    public void setCustomScale(float scale) {
        this.mCustomScale = scale;
        this.mCurrentScaleCustom = true;
        updateVideoSurfaces();
    }

    public MediaPlayer.ScaleType getVideoScale() {
        return this.mCurrentScaleType;
    }
}
