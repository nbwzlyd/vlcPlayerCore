/*****************************************************************************
 * MediaPlayer.java
 *****************************************************************************
 * Copyright © 2015 VLC authors and VideoLAN
 *
 * Authors  Jean-Baptiste Kempf <jb@videolan.org>
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package org.videolan.libvlc;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.AudioDeviceCallback;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.videolan.libvlc.interfaces.AbstractVLCEvent;
import org.videolan.libvlc.interfaces.ILibVLC;
import org.videolan.libvlc.interfaces.IMedia;
import org.videolan.libvlc.interfaces.IVLCVout;
import org.videolan.libvlc.util.AndroidUtil;
import org.videolan.libvlc.util.DisplayManager;
import org.videolan.libvlc.util.VLCUtil;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("unused, JniMissingFunction")
public class MediaPlayer extends VLCObject<MediaPlayer.Event> {

    /**
     * Useful when we want to resize the VideoLayout and avoid using the
     * context orientation to calculate the surface views bounds
     */
    private Boolean mUseOrientationFromBounds = false;

    public static class Event extends AbstractVLCEvent {
        public static final int MediaChanged        = 0x100;
        //public static final int NothingSpecial      = 0x101;
        public static final int Opening             = 0x102;
        public static final int Buffering           = 0x103;
        public static final int Playing             = 0x104;
        public static final int Paused              = 0x105;
        public static final int Stopped             = 0x106;
        //public static final int Forward             = 0x107;
        //public static final int Backward            = 0x108;
        public static final int EndReached          = 0x109;
        public static final int EncounteredError   = 0x10a;
        public static final int TimeChanged         = 0x10b;
        public static final int PositionChanged     = 0x10c;
        public static final int SeekableChanged     = 0x10d;
        public static final int PausableChanged     = 0x10e;
        //public static final int TitleChanged        = 0x10f;
        //public static final int SnapshotTaken       = 0x110;
        public static final int LengthChanged       = 0x111;
        public static final int Vout                = 0x112;
        //public static final int ScrambledChanged    = 0x113;
        public static final int ESAdded             = 0x114;
        public static final int ESDeleted           = 0x115;
        public static final int ESSelected          = 0x116;
        //        public static final int Corked              = 0x117;
//        public static final int Uncorked            = 0x118;
//        public static final int Muted               = 0x119;
//        public static final int Unmuted             = 0x11a;
//        public static final int AudioVolume         = 0x11b;
//        public static final int AudioDevice         = 0x11c;
//        public static final int ChapterChanged      = 0x11d;
        public static final int RecordChanged       = 0x11e;

        protected Event(int type) {
            super(type);
        }
        protected Event(int type, long arg1) {
            super(type, arg1);
        }

        protected Event(int type, long arg1, long arg2) {
            super(type, arg1, arg2);
        }

        protected Event(int type, float argf) {
            super(type, argf);
        }

        protected Event(int type, long arg1, @Nullable String args1) {
            super(type, arg1, args1);
        }

        public long getTimeChanged() {
            return arg1;
        }

        public long getLengthChanged() {
            return arg1;
        }

        public float getPositionChanged() {
            return argf1;
        }
        public int getVoutCount() {
            return (int) arg1;
        }
        public int getEsChangedType() {
            return (int) arg1;
        }
        public int getEsChangedID() {
            return (int) arg2;
        }
        public boolean getPausable() {
            return arg1 != 0;
        }
        public boolean getSeekable() {
            return arg1 != 0;
        }
        public float getBuffering() {
            return argf1;
        }
        public boolean getRecording() {
            return arg1 != 0;
        }
        @Nullable
        public String getRecordPath() {
            return args1;
        }
    }

    public interface EventListener extends AbstractVLCEvent.Listener<MediaPlayer.Event> {}

    public static class Position {
        public static final int Disable = -1;
        public static final int Center = 0;
        public static final int Left = 1;
        public static final int Right = 2;
        public static final int Top = 3;
        public static final int TopLeft = 4;
        public static final int TopRight = 5;
        public static final int Bottom = 6;
        public static final int BottomLeft = 7;
        public static final int BottomRight = 8;
    }

    public static class Navigate {
        public static final int Activate = 0;
        public static final int Up = 1;
        public static final int Down = 2;
        public static final int Left = 3;
        public static final int Right = 4;
    }

    public static class Title {
        private static class Flags {
            public static final int MENU = 0x01;
            public static final int INTERACTIVE = 0x02;
        };
        /**
         * duration in milliseconds
         */
        public final long duration;

        /**
         * title name
         */
        public final String name;

        /**
         * true if the title is a menu
         */
        private final int flags;

        public Title(long duration, String name, int flags) {
            this.duration = duration;
            this.name = name;
            this.flags = flags;
        }

        public boolean isMenu() {
            return (this.flags & Flags.MENU) != 0;
        }

        public boolean isInteractive() {
            return (this.flags & Flags.INTERACTIVE) != 0;
        }
    }

    @SuppressWarnings("unused") /* Used from JNI */
    private static Title createTitleFromNative(long duration, String name, int flags) {
        return new Title(duration, name, flags);
    }

    public static class Chapter {
        /**
         * time-offset of the chapter in milliseconds
         */
        public final long timeOffset;

        /**
         * duration of the chapter in milliseconds
         */
        public final long duration;

        /**
         * chapter name
         */
        public final String name;

        private Chapter(long timeOffset, long duration, String name) {
            this.timeOffset = timeOffset;
            this.duration = duration;
            this.name = name;
        }
    }

    @SuppressWarnings("unused") /* Used from JNI */
    private static Chapter createChapterFromNative(long timeOffset, long duration, String name) {
        return new Chapter(timeOffset, duration, name);
    }

    public static class Equalizer {
        @SuppressWarnings("unused") /* Used from JNI */
        private long mInstance;

        private Equalizer() {
            nativeNew();
        }

        private Equalizer(int index) {
            nativeNewFromPreset(index);
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                nativeRelease();
            } finally {
                super.finalize();
            }
        }

        /**
         * Create a new default equalizer, with all frequency values zeroed.
         * The new equalizer can subsequently be applied to a media player by invoking
         * {@link MediaPlayer#setEqualizer}.
         */
        public static Equalizer create() {
            return new Equalizer();
        }

        /**
         * Create a new equalizer, with initial frequency values copied from an existing
         * preset.
         * The new equalizer can subsequently be applied to a media player by invoking
         * {@link MediaPlayer#setEqualizer}.
         */
        public static Equalizer createFromPreset(int index) {
            return new Equalizer(index);
        }

        /**
         * Get the number of equalizer presets.
         */
        public static int getPresetCount() {
            return nativeGetPresetCount();
        }

        /**
         * Get the name of a particular equalizer preset.
         * This name can be used, for example, to prepare a preset label or menu in a user
         * interface.
         *
         * @param  index index of the preset, counting from zero.
         * @return preset name, or NULL if there is no such preset
         */

        public static String getPresetName(int index) {
            return nativeGetPresetName(index);
        }

        /**
         * Get the number of distinct frequency bands for an equalizer.
         */
        public static int getBandCount() {
            return nativeGetBandCount();
        }

        /**
         * Get a particular equalizer band frequency.
         * This value can be used, for example, to create a label for an equalizer band control
         * in a user interface.
         *
         * @param index index of the band, counting from zero.
         * @return equalizer band frequency (Hz), or -1 if there is no such band
         */
        public static float getBandFrequency(int index) {
            return nativeGetBandFrequency(index);
        }

        /**
         * Get the current pre-amplification value from an equalizer.
         *
         * @return preamp value (Hz)
         */
        public float getPreAmp() {
            return nativeGetPreAmp();
        }

        /**
         * Set a new pre-amplification value for an equalizer.
         * The new equalizer settings are subsequently applied to a media player by invoking
         * {@link MediaPlayer#setEqualizer}.
         * The supplied amplification value will be clamped to the -20.0 to +20.0 range.
         *
         * @param preamp value (-20.0 to 20.0 Hz)
         * @return true on success.
         */
        public boolean setPreAmp(float preamp) {
            return nativeSetPreAmp(preamp);
        }

        /**
         * Get the amplification value for a particular equalizer frequency band.
         *
         * @param index counting from zero, of the frequency band to get.
         * @return amplification value (Hz); NaN if there is no such frequency band.
         */
        public float getAmp(int index) {
            return nativeGetAmp(index);
        }

        /**
         * Set a new amplification value for a particular equalizer frequency band.
         * The new equalizer settings are subsequently applied to a media player by invoking
         * {@link MediaPlayer#setEqualizer}.
         * The supplied amplification value will be clamped to the -20.0 to +20.0 range.
         *
         * @param index counting from zero, of the frequency band to set.
         * @param amp amplification value (-20.0 to 20.0 Hz).
         * \return true on success.
         */
        public boolean setAmp(int index, float amp) {
            return nativeSetAmp(index, amp);
        }

        private static native int nativeGetPresetCount();
        private static native String nativeGetPresetName(int index);
        private static native int nativeGetBandCount();
        private static native float nativeGetBandFrequency(int index);
        private native void nativeNew();
        private native void nativeNewFromPreset(int index);
        private native void nativeRelease();
        private native float nativeGetPreAmp();
        private native boolean nativeSetPreAmp(float preamp);
        private native float nativeGetAmp(int index);
        private native boolean nativeSetAmp(int index, float amp);
    }

    //Video size constants
    public enum ScaleType {
        SURFACE_BEST_FIT(null),
        SURFACE_FIT_SCREEN(null),
        SURFACE_FILL(null),
        SURFACE_16_9(16F/9F),
        SURFACE_4_3(4F/3F),
        SURFACE_16_10(16F/10F),
        SURFACE_221_1(2.21F),
        SURFACE_235_1(2.35F),
        SURFACE_239_1(2.39F),
        SURFACE_5_4(5F/4F),
        SURFACE_ORIGINAL(null);


        private final Float ratio;

        ScaleType(Float ratio) {
            this.ratio = ratio;
        }

        public Float getRatio() {
            return ratio;
        }

        static public ScaleType[] getMainScaleTypes() {
            return new ScaleType[]{SURFACE_BEST_FIT, SURFACE_FIT_SCREEN, SURFACE_FILL, SURFACE_16_9, SURFACE_4_3, SURFACE_ORIGINAL};
        }
    }
    public static final int SURFACE_SCALES_COUNT = ScaleType.values().length;

    private IMedia mMedia = null;
    private org.videolan.libvlc.RendererItem mRenderer = null;
    private AssetFileDescriptor mAfd = null;
    private boolean mPlaying = false;
    private boolean isSurfaceDestroy=false;
    private boolean mPlayRequested = false;
    private boolean mListenAudioPlug = true;
    private int mVoutCount = 0;
    private String mAudioOutput = null;
    private String mAudioOutputDevice = null;

    private boolean mAudioPlugRegistered = false;
    private boolean mAudioDigitalOutputEnabled = false;
    private String mAudioPlugOutputDevice = "stereo";

    private boolean mCanDoPassthrough;

    // Video tools
    private org.videolan.libvlc.VideoHelper mVideoHelper = null;

    private final org.videolan.libvlc.AWindow mWindow = new org.videolan.libvlc.AWindow(new org.videolan.libvlc.AWindow.SurfaceCallback() {
        @Override
        public void onSurfacesCreated(org.videolan.libvlc.AWindow vout) {
            boolean play = false;
            boolean enableVideo = false;
            synchronized (MediaPlayer.this) {
                if (!mPlaying && mPlayRequested)
                    play = true;
                else if (mVoutCount == 0)
                    enableVideo = true;
            }
            if (play)
                play();
            else if (enableVideo)
                setVideoTrackEnabled(true);
        }

        @Override
        public void onSurfacesDestroyed(org.videolan.libvlc.AWindow vout) {
            boolean disableVideo = false;
            mPlaying = false;
            synchronized (MediaPlayer.this) {
                if (mVoutCount > 0)
                    disableVideo = true;
            }
            if (disableVideo)
                setVideoTrackEnabled(false);
        }
    });

    private synchronized void updateAudioOutputDevice(long encodingFlags, String defaultDevice) {
        mCanDoPassthrough = encodingFlags != 0;
        final String newDeviceId = mAudioDigitalOutputEnabled && mCanDoPassthrough ? "encoded:" + encodingFlags : defaultDevice;
        if (!newDeviceId.equals(mAudioPlugOutputDevice)) {
            mAudioPlugOutputDevice = newDeviceId;
            setAudioOutputDeviceInternal(mAudioPlugOutputDevice, false);
        }
    }

    private boolean isEncoded(int encoding) {
        switch (encoding) {
            case AudioFormat.ENCODING_AC3:
            case AudioFormat.ENCODING_E_AC3:
            case 14 /* AudioFormat.ENCODING_DOLBY_TRUEHD */:
            case AudioFormat.ENCODING_DTS:
            case AudioFormat.ENCODING_DTS_HD:
                return true;
            default:
                return false;
        }
    }

    private long getEncodingFlags(int encodings[]) {
        if (encodings == null)
            return 0;
        long encodingFlags = 0;
        for (int encoding : encodings) {
            if (isEncoded(encoding))
                encodingFlags |= 1 << encoding;
        }
        return encodingFlags;
    }

    private BroadcastReceiver createAudioPlugReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action == null)
                    return;
                if (action.equalsIgnoreCase(AudioManager.ACTION_HDMI_AUDIO_PLUG)) {
                    final boolean hasHdmi = intent.getIntExtra(AudioManager.EXTRA_AUDIO_PLUG_STATE, 0) == 1;
                    final long encodingFlags = !hasHdmi ? 0 :
                            getEncodingFlags(intent.getIntArrayExtra(AudioManager.EXTRA_ENCODINGS));
                    updateAudioOutputDevice(encodingFlags, "stereo");
                }
            }
        };
    }

    private final BroadcastReceiver mAudioPlugReceiver =
            !AndroidUtil.isMarshMallowOrLater ? createAudioPlugReceiver() : null;

    private void registerAudioPlugV21(boolean register) {
        if (register) {
            final IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_HDMI_AUDIO_PLUG);
            final Intent stickyIntent = mILibVLC.getAppContext().registerReceiver(mAudioPlugReceiver, intentFilter);
            if (stickyIntent != null)
                mAudioPlugReceiver.onReceive(mILibVLC.getAppContext(), stickyIntent);
        } else {
            mILibVLC.getAppContext().unregisterReceiver(mAudioPlugReceiver);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private AudioDeviceCallback createAudioDeviceCallback() {

        return new AudioDeviceCallback() {

            private SparseArray<Long> mEncodedDevices = new SparseArray<>();

            private void onAudioDevicesChanged() {
                long encodingFlags = 0;
                for (int i = 0; i < mEncodedDevices.size(); ++i)
                    encodingFlags |= mEncodedDevices.valueAt(i);

                /* Very simple assumption: force stereo PCM if the audio device doesn't support
                 * any encoded codecs. */
                final String defaultDevice = encodingFlags == 0 ? "stereo" : "pcm";
                updateAudioOutputDevice(encodingFlags, defaultDevice);
            }

            @RequiresApi(Build.VERSION_CODES.M)
            @Override
            public void onAudioDevicesAdded(AudioDeviceInfo[] addedDevices) {
                for (AudioDeviceInfo info : addedDevices) {
                    if (!info.isSink())
                        continue;
                    long encodingFlags = getEncodingFlags(info.getEncodings());
                    if (encodingFlags != 0)
                        mEncodedDevices.put(info.getId(), encodingFlags);
                }
                onAudioDevicesChanged();
            }

            @RequiresApi(Build.VERSION_CODES.M)
            @Override
            public void onAudioDevicesRemoved(AudioDeviceInfo[] removedDevices) {
                for (AudioDeviceInfo info : removedDevices) {
                    if (!info.isSink())
                        continue;
                    mEncodedDevices.remove(info.getId());
                }
                onAudioDevicesChanged();
            }
        };
    }

    private final AudioDeviceCallback mAudioDeviceCallback =
            AndroidUtil.isMarshMallowOrLater ? createAudioDeviceCallback() : null;

    @TargetApi(Build.VERSION_CODES.M)
    private void registerAudioPlugV23(boolean register) {
        AudioManager am = mILibVLC.getAppContext().getSystemService(AudioManager.class);
        if (register) {
            mAudioDeviceCallback.onAudioDevicesAdded(am.getDevices(AudioManager.GET_DEVICES_OUTPUTS));
            am.registerAudioDeviceCallback(mAudioDeviceCallback, null);
        } else {
            am.unregisterAudioDeviceCallback(mAudioDeviceCallback);
        }
    }

    private void registerAudioPlug(boolean register) {
        if (register == mAudioPlugRegistered)
            return;
        if (mAudioDeviceCallback != null)
            registerAudioPlugV23(register);
        else if (mAudioPlugReceiver != null)
            registerAudioPlugV21(register);
        mAudioPlugRegistered = register;
    }

    /**
     * HACK: handler to call updateVideoSurfaces() as soon as a video output
     * is created. It is currently mandatory to have the video being displayed
     * instead of a black screen. */
    Handler mHandlerMainThread = new Handler(Looper.getMainLooper());

    /**
     * Create an empty MediaPlayer
     *
     * @param ILibVLC a valid libVLC
     */
    public MediaPlayer(ILibVLC ILibVLC) {
        super(ILibVLC);
        nativeNewFromLibVlc(ILibVLC, mWindow);
    }

    /**
     * Create a MediaPlayer from a Media
     *
     * @param media a valid Media object
     */
    public MediaPlayer(@NonNull IMedia media) {
        super(media);
        if (media == null || media.isReleased())
            throw new IllegalArgumentException("Media is null or released");
        mMedia = media;
        mMedia.retain();
        nativeNewFromMedia(mMedia, mWindow);
    }

    /**
     * Get the IVLCVout helper.
     */
    @NonNull
    public IVLCVout getVLCVout() {
        return mWindow;
    }

    /**
     * Attach a video layout to the player
     *
     * @param surfaceFrame {@link VLCVideoLayout} in which the video will be displayed
     * @param dm Optional {@link DisplayManager} to help switch between renderers, primary and secondary displays
     * @param subtitles Whether you wish to show subtitles
     * @param textureView If true, {@link VLCVideoLayout} will use a {@link android.view.TextureView} instead of a {@link android.view.SurfaceView}
     */
    public void attachViews(@NonNull VLCVideoLayout surfaceFrame, @Nullable DisplayManager dm, boolean subtitles, boolean textureView) {
        mVideoHelper = new VideoHelper(this, surfaceFrame, dm, subtitles, textureView);
        mVideoHelper.attachViews();
    }

    /**
     * Detach the video layout
     */
    public void detachViews() {
        if (mVideoHelper != null) {
            mVideoHelper.release();
            mVideoHelper = null;
        }
    }

    /**
     * Update the video surfaces, either to switch from one to another or to resize it
     */
    public void updateVideoSurfaces() {
        if (mVideoHelper != null) mVideoHelper.updateVideoSurfaces();
    }

    /**
     * Set the video scale type, by default, scaletype is set to ScaleType.SURFACE_BEST_FIT
     * @param {@link ScaleType} to rule the video surface filling
     */
    public void setVideoScale(@NonNull ScaleType type) {
        if (mVideoHelper != null) mVideoHelper.setVideoScale(type);
    }

    /**
     * Get the current video scale type
     * @return the current {@link ScaleType} used by MediaPlayer
     */
    @NonNull
    public ScaleType getVideoScale() {
        return mVideoHelper != null ? mVideoHelper.getVideoScale() : ScaleType.SURFACE_BEST_FIT;
    }

    /**
     * Set a Media
     *
     * @param media a valid Media object
     */
    public void setMedia(@Nullable IMedia media) {
        if (media != null) {
            if (media.isReleased())
                throw new IllegalArgumentException("Media is released");
            media.setDefaultMediaPlayerOptions();
        }
        nativeSetMedia(media);
        synchronized (this) {
            if (mMedia != null) {
                mMedia.release();
            }
            if (media != null)
                media.retain();
            mMedia = media;
        }
    }

    /**
     * Set a renderer
     * @param item {@link org.videolan.libvlc.RendererItem}. if null VLC play on default output
     */
    public int setRenderer(@Nullable org.videolan.libvlc.RendererItem item) {
        if (mRenderer != null) mRenderer.release();
        if (item != null) item.retain();
        mRenderer = item;
        return nativeSetRenderer(item);
    }

    /**
     * Is a media in use by this MediaPlayer
     * @return true if a media is set
     */
    public synchronized boolean hasMedia() {
        return mMedia != null;
    }

    /**
     * Get the Media used by this MediaPlayer. This Media should be released with {@link #release()}.
     */
    @Nullable
    public synchronized IMedia getMedia() {
        if (mMedia != null)
            mMedia.retain();
        return mMedia;
    }

    /**
     * Play the media
     *
     */
    public void play() {
        synchronized (this) {
            if (!mPlaying) {
                if (mListenAudioPlug)
                    registerAudioPlug(true);
                mPlayRequested = true;
                if (mWindow.areSurfacesWaiting())
                    return;
            }
            mPlaying = true;
        }
        nativePlay();
    }

    /**
     * Load an asset and starts playback
     * @param context An application context, mandatory to access assets
     * @param assetFilename relative path of the asset in app assets folder
     * @throws IOException
     */
    public void playAsset(@NonNull Context context, @NonNull String assetFilename) throws IOException {
        mAfd = context.getAssets().openFd(assetFilename);
        play(mAfd);
    }

    /**
     * Load an asset and starts playback
     * @param afd The {@link AssetFileDescriptor} to play
     */
    public void play(@NonNull AssetFileDescriptor afd) {
        final IMedia media = new org.videolan.libvlc.Media(mILibVLC, afd);
        play(media);
    }

    /**
     * Play a media via its mrl
     * @param path Path of the media file to play
     */
    public void play(@NonNull String path) {
        final IMedia media = new org.videolan.libvlc.Media(mILibVLC, path);
        play(media);
    }

    /**
     * Play a media via its Uri
     * @param uri {@link Uri} of the media to play
     */
    public void play(@NonNull Uri uri) {
        final IMedia media = new org.videolan.libvlc.Media(mILibVLC, uri);
        play(media);
    }

    /**
     * Starts playback from an already prepared Media
     * @param media The {@link IMedia} to play
     */
    public void play(@NonNull IMedia media) {
        setMedia(media);
        media.release();
        play();
    }

    /**
     * Stops the playing media
     *
     */
    public void stop() {
        synchronized (this) {
            mPlayRequested = false;
            mPlaying = false;
        }
        nativeStop();
        if (mAfd != null) try {
            mAfd.close();
        } catch (IOException ignored) {}
    }

    /**
     * Set if, and how, the video title will be shown when media is played
     *
     * @param position see {@link Position}
     * @param timeout
     */
    public void setVideoTitleDisplay(int position, int timeout) {
        nativeSetVideoTitleDisplay(position, timeout);
    }

    /**
     * Get the current video scaling factor
     *
     * @return the currently configured zoom factor, or 0. if the video is set to fit to the
     * output window/drawable automatically.
     */
    public float getScale() {
        return nativeGetScale();
    }

    /**
     * Set the video scaling factor
     *
     * That is the ratio of the number of pixels on screen to the number of pixels in the original
     * decoded video in each dimension. Zero is a special value; it will adjust the video to the
     * output window/drawable (in windowed mode) or the entire screen.
     *
     * @param scale the scaling factor, or zero
     */
    public void setScale(float scale) {
        mVideoHelper.setCustomScale(scale);
    }

    protected void setNativeScale(float scale) {
        nativeSetScale(scale);
    }

    /**
     * Get current video aspect ratio
     *
     * @return the video aspect ratio or NULL if unspecified
     */
    public String getAspectRatio() {
        return nativeGetAspectRatio();
    }

    /**
     * Set new video aspect ratio.
     *
     * @param aspect new video aspect-ratio or NULL to reset to default
     */
    public void setAspectRatio(String aspect) {
        nativeSetAspectRatio(aspect);
    }

    private boolean isAudioDigitalOutputCapable() {
        return mAudioOutput == null || mAudioOutput.contains("audiotrack");
    }

    /**
     * Update the video viewpoint information
     *
     * @param yaw View point yaw in degrees
     * @param pitch View point pitch in degrees
     * @param roll  View point roll in degrees
     * @param fov Field of view in degrees (default 80.0f)
     * @param absolute if true replace the old viewpoint with the new one. If false,
     *                 increase/decrease it.
     * @return true on success.
     */
    public boolean updateViewpoint(float yaw, float pitch, float roll, float fov, boolean absolute) {
        return nativeUpdateViewpoint(yaw, pitch, roll, fov, absolute);
    }

    /**
     * Selects an audio output module.
     * Any change will take effect only after playback is stopped and
     * restarted. Audio output cannot be changed while playing.
     *
     * By default, the "android_audiotrack" is selected. Starting Android 21, passthrough is
     * enabled for encodings supported by the device/audio system.
     *
     * Calling this method will disable the encoding detection.
     *
     * @return true on success.
     */
    public synchronized boolean setAudioOutput(String aout) {
        mAudioOutput = aout;
        /* If The user forced an output different than AudioTrack, don't listen to audio
         * plug events and let the user decide */
        mListenAudioPlug = isAudioDigitalOutputCapable();
        if (!mListenAudioPlug)
            registerAudioPlug(false);

        final boolean ret = nativeSetAudioOutput(aout);

        if (!ret) {
            mAudioOutput = null;
            mListenAudioPlug = false;
        }

        if (mListenAudioPlug)
            registerAudioPlug(true);

        return ret;
    }

    /**
     * Enable or disable Digital Output
     *
     * Works only with AudioTrack AudioOutput.
     * If {@link #setAudioOutputDevice} was previously called, this method won't have any effects.
     *
     * @param enabled true to enable Digital Output
     * @return true on success
     */
    public synchronized boolean setAudioDigitalOutputEnabled(boolean enabled) {
        if (enabled == mAudioDigitalOutputEnabled)
            return true;
        if (!mListenAudioPlug || !isAudioDigitalOutputCapable())
            return false;

        registerAudioPlug(false);
        mAudioDigitalOutputEnabled = enabled;
        registerAudioPlug(true);
        return true;
    }

    /** Convenient method for {@link #setAudioOutputDevice}
     *
     * @param encodings list of encodings to play via passthrough (see AudioFormat.ENCODING_*),
     *                  null to don't force any.
     * @return true on success
     */
    public synchronized boolean forceAudioDigitalEncodings(int []encodings) {
        if (!isAudioDigitalOutputCapable())
            return false;

        if (encodings.length == 0)
            setAudioOutputDeviceInternal(null, true);
        else {
            final String newDeviceId = "encoded:" + getEncodingFlags(encodings);
            if (!newDeviceId.equals(mAudioPlugOutputDevice)) {
                mAudioPlugOutputDevice = newDeviceId;
                setAudioOutputDeviceInternal(mAudioPlugOutputDevice, true);
            }
        }
        return true;
    }

    private synchronized boolean setAudioOutputDeviceInternal(String id, boolean fromUser) {
        mAudioOutputDevice = id;
        if (fromUser) {
            /* The user forced a device, don't listen to audio plug events and let the user decide */
            mListenAudioPlug = mAudioOutputDevice == null && isAudioDigitalOutputCapable();
            if (!mListenAudioPlug)
                registerAudioPlug(false);
        }

        final boolean ret = nativeSetAudioOutputDevice(id);

        if (!ret) {
            mAudioOutputDevice = null;
            mListenAudioPlug = false;
        }

        if (mListenAudioPlug)
            registerAudioPlug(true);

        return ret;
    }

    public void setUseOrientationFromBounds(Boolean mUseOrientationFromBounds) {
        this.mUseOrientationFromBounds = mUseOrientationFromBounds;
    }

    public Boolean useOrientationFromBounds() {
        return mUseOrientationFromBounds;
    }

    /**
     * Configures an explicit audio output device.
     * Audio output will be moved to the device specified by the device identifier string.
     *
     * Available devices for the "android_audiotrack" module (the default) are
     * "stereo": Up to 2 channels (compat mode).
     * "pcm": Up to 8 channels.
     * "encoded": Up to 8 channels, passthrough for every encodings if available.
     * "encoded:ENCODING_FLAGS_MASK": passthrough for every encodings specified by
     * ENCODING_FLAGS_MASK. This extra value is a long that contains binary-shifted
     * AudioFormat.ENCODING_* values.
     *
     * Calling this method will disable the encoding detection (see {@link #setAudioOutput} and {@link #setAudioDigitalOutputEnabled(boolean)}).
     *
     * @return true on success.
     */
    public boolean setAudioOutputDevice(String id) {
        return setAudioOutputDeviceInternal(id, true);
    }

    /**
     * Get the full description of available titles.
     *
     * @return the list of titles
     */
    public Title[] getTitles() {
        return nativeGetTitles();
    }

    /**
     * Get the full description of available chapters.
     *
     * @param title index of the title (if -1, use the current title)
     * @return the list of Chapters for the title
     */
    public Chapter[] getChapters(int title) {
        return nativeGetChapters(title);
    }

    /**
     * Get the list of available tracks for a given type
     *
     * More than one tracks can be selected for one type. In that case,
     * {@link getSelectedTracks} should be used.
     *
     * @param type type defined by {@link org.videolan.libvlc.Media.Track.Type}
     * @return a track array or null. Each tracks can be casted to {@link
     * org.videolan.libvlc.Media.VideoTrack}, {@link org.videolan.libvlc.Media.AudioTrack}, {@link
     * org.videolan.libvlc.Media.SubtitleTrack}, or {@link org.videolan.libvlc.Media.UnknownTrack} depending on {@link
     * org.videolan.libvlc.Media.type}
     */
    public org.videolan.libvlc.Media.Track[] getTracks(int type) {
        return nativeGetTracks(type, false);
    }

    /**
     * Get the first selected track for a given type
     *
     * @param type type defined by {@link org.videolan.libvlc.Media.Track.Type}
     * @return a track or null. Can be casted to {@link org.videolan.libvlc.Media.VideoTrack},
     * {@link org.videolan.libvlc.Media.AudioTrack}, {@link org.videolan.libvlc.Media.SubtitleTrack}, or {@link
     * org.videolan.libvlc.Media.UnknownTrack} depending on {@link org.videolan.libvlc.Media.type}
     */
    public org.videolan.libvlc.Media.Track getSelectedTrack(int type) {
        return nativeGetSelectedTrack(type);
    }

    /**
     * Get the list of selected tracks for a given type
     *
     * @param type type defined by {@link org.videolan.libvlc.Media.Track.Type}
     * @return a track array or null. Each tracks can be casted to {@link
     * org.videolan.libvlc.Media.VideoTrack}, {@link org.videolan.libvlc.Media.AudioTrack}, {@link
     * org.videolan.libvlc.Media.SubtitleTrack}, or {@link org.videolan.libvlc.Media.UnknownTrack} depending on {@link
     * org.videolan.libvlc.Media.type}
     */
    public org.videolan.libvlc.Media.Track[] getSelectedTracks(int type) {
        return nativeGetTracks(type, true);
    }

    /**
     * Get a track from its id
     *
     * This function can be used to get the last updated information of a track.
     *
     * @param id id from {@link org.videolan.libvlc.Media.Track.id}
     */
    public org.videolan.libvlc.Media.Track getTrackFromID(String id) {
        return nativeGetTrackFromID(id);
    }

    /**
     * Select a track by its id
     *
     * This will unselect the current track of the sane type
     *
     * @param id id from {@link org.videolan.libvlc.Media.Track.id}
     * @return true if the track was found and selected
     */
    public boolean selectTrack(String id) {
        return nativeSelectTrack(id);
    }

    /**
     * Select multiple tracks for one type
     *
     * Selecting multiple audio tracks is currently not supported.
     *
     * This function can be used pre-select a list of tracks before starting
     * the player. It has only effect for the current media. It can also be
     * used when the player is already started.

     * 'ids' can contain more than one track id, delimited with ','. "" or any
     * invalid track id will cause the player to unselect all tracks of that
     * category. NULL will disable the preference for newer tracks without
     * unselecting any current tracks.

     * Example:
     * - (Media.Track.Type.Video, "video/1,video/2") will select these 2 video
     *   tracks.  If there is only one video track with the id "video/0", no
     *   tracks will be selected.
     * - (Media.Track.Type.Text, "${slave_url_md5sum}/spu/0) will select one
     *   spu added by an input slave with the corresponding url.
     */
    public void selectTracks(int type, String ids)
    {
        nativeSelectTracks(type, ids);
    }

    /**
     * Unselect all tracks for a given type
     *
     * @param type type defined by {@link org.videolan.libvlc.Media.Track.Type}
     */
    public void unselectTrackType(int type) {
        nativeUnselectTrackType(type);
    }

    /**
     * Set the enabled state of the video track
     *
     * @param enabled
     */
    public void setVideoTrackEnabled(boolean enabled) {
        if (!enabled) {
            unselectTrackType(org.videolan.libvlc.Media.Track.Type.Video);
        } else if (!isReleased() && hasMedia() && getSelectedTrack(org.videolan.libvlc.Media.Track.Type.Video) == null) {
            final org.videolan.libvlc.Media.Track[] tracks = getTracks(org.videolan.libvlc.Media.Track.Type.Video);
            if (tracks != null)
                selectTrack(tracks[0].id);
        }
    }

    /**
     * Get the current audio delay.
     *
     * @return delay in microseconds.
     */
    public long getAudioDelay() {
        return nativeGetAudioDelay();
    }

    /**
     * Set current audio delay. The audio delay will be reset to zero each time the media changes.
     *
     * @param delay in microseconds.
     * @return true on success.
     */
    public boolean setAudioDelay(long delay) {
        return nativeSetAudioDelay(delay);
    }

    /**
     * Get the current spu (subtitle) delay.
     *
     * @return delay in microseconds.
     */
    public long getSpuDelay() {
        return nativeGetSpuDelay();
    }

    /**
     * Set current spu (subtitle) delay. The spu delay will be reset to zero each time the media changes.
     *
     * @param delay in microseconds.
     * @return true on success.
     */
    public boolean setSpuDelay(long delay) {
        return nativeSetSpuDelay(delay);
    }

    /**
     * Apply new equalizer settings to a media player.
     *
     * The equalizer is first created by invoking {@link Equalizer#create()} or
     * {@link Equalizer#createFromPreset(int)}}.
     *
     * It is possible to apply new equalizer settings to a media player whether the media
     * player is currently playing media or not.
     *
     * Invoking this method will immediately apply the new equalizer settings to the audio
     * output of the currently playing media if there is any.
     *
     * If there is no currently playing media, the new equalizer settings will be applied
     * later if and when new media is played.
     *
     * Equalizer settings will automatically be applied to subsequently played media.
     *
     * To disable the equalizer for a media player invoke this method passing null.
     *
     * @return true on success.
     */
    public boolean setEqualizer(Equalizer equalizer) {
        return nativeSetEqualizer(equalizer);
    }

    /**
     * Add a slave (or subtitle) to the current media player.
     *
     * @param type see {@link IMedia.Slave.Type}
     * @param uri a valid RFC 2396 Uri
     * @return true on success.
     */
    public boolean addSlave(int type, Uri uri, boolean select) {
        return nativeAddSlave(type, VLCUtil.encodeVLCUri(uri), select);
    }

    /**
     * Start/stop recording
     *
     * @param directory path of the recording directory or null to stop
     * recording
     * @param enable true to start recording, false to stop
     * @return true on success.
     */
    public boolean record(String directory, boolean enable) {
        return nativeRecord(directory, enable);
    }

    /**
     * Add a slave (or subtitle) to the current media player.
     *
     * @param type see {@link IMedia.Slave.Type}
     * @param path a local path
     * @return true on success.
     */
    public boolean addSlave(int type, String path, boolean select) {
        return addSlave(type, Uri.fromFile(new File(path)), select);
    }

    /**
     * Sets the speed of playback (1 being normal speed, 2 being twice as fast)
     *
     * @param rate
     */
    public native void setRate(float rate);

    /**
     * Get the current playback speed
     */
    public native float getRate();

    /**
     * Returns true if any media is playing
     */
    public native boolean isPlaying();

    /**
     * Returns true if any media is seekable
     */
    public native boolean isSeekable();

    /**
     * Pauses any playing media
     */
    public native void pause();

    /**
     * Get player state.
     */
    public native int getPlayerState();

    /**
     * Gets volume as integer
     */
    public native int getVolume();

    /**
     * Sets volume as integer
     * @param volume: Volume level passed as integer
     */
    public native int setVolume(int volume);

    /**
     * Gets the current movie time (in ms).
     * @return the movie time (in ms), or -1 if there is no media.
     */
    public native long getTime();

    /**
     * Sets the movie time (in ms), if any media is being played.
     * @param time: Time in ms.
     * @param fast: Prefer fast seeking or precise seeking
     * @return the movie time (in ms), or -1 if there is no media.
     */
    public long setTime(long time, boolean fast) {
        return nativeSetTime(time, fast);
    }

    public long setTime(long time) {
        return nativeSetTime(time, false);
    }

    /**
     * Gets the movie position.
     * @return the movie position, or -1 for any error.
     */
    public native float getPosition();

    /**
     * Sets the movie position.
     * @param pos: movie position.
     * @param fast: Prefer fast seeking or precise seeking
     */
    public void setPosition(float pos, boolean fast) {
        nativeSetPosition(pos, fast);
    }
    public void setPosition(float pos) {
        nativeSetPosition(pos, false);
    }

    /**
     * Gets currently selected teletext page.
     * @return the currently selected teletext page.
     */
    public int getTeletext() {
        return nativeGetTeletext();
    };

    /**
     * Select a teletext page.
     * If telexext was not active, activate teletext.
     * @param page: page to change to
     */
    public void setTeletext(int page) {
        nativeSetTeletext(page);
    };

    /**
     * Get current teletext background transparency.
     * @return true if teletext is currently active and transparent, false if teletext is opaque or not active.
     */
    public boolean getTeletextTransparency() {
        return nativeGetTeletextTransparency();
    };

    /**
     * Set teletext background transparency.
     * @param transparent: true for transparent, false for opaque
     */
    public void setTeletextTransparency(boolean transparent) {
        nativeSetTeletextTransparency(transparent);
    };

    /**
     * Gets current movie's length in ms.
     * @return the movie length (in ms), or -1 if there is no media.
     */
    public native long getLength();

    public native int getTitle();
    public native void setTitle(int title);
    public native int getChapter();
    public native int previousChapter();
    public native int nextChapter();
    public native void setChapter(int chapter);
    public native void navigate(int navigate);

    public synchronized void setEventListener(EventListener listener) {
        super.setEventListener(listener);
    }

    @Override
    protected synchronized Event onEventNative(int eventType, long arg1, long arg2, float argf1, @Nullable String args1) {
        switch (eventType) {
            case Event.MediaChanged:
            case Event.Stopped:
            case Event.EndReached:
            case Event.EncounteredError:
                mVoutCount = 0;
                notify();
            case Event.Opening:
            case Event.Buffering:
                return new Event(eventType, argf1);
            case Event.Playing:
            case Event.Paused:
                return new Event(eventType);
            case Event.TimeChanged:
                return new Event(eventType, arg1);
            case Event.LengthChanged:
                return new Event(eventType, arg1);
            case Event.PositionChanged:
                return new Event(eventType, argf1);
            case Event.Vout:
                mVoutCount = (int) arg1;
                notify();

                /* Post on the main thread so that surfaces gets updated
                 * after the event has been processed by the application.
                 * FIXME: This is a hack to ensure the video surface is
                 * correctly setup and doesn't appear black when using
                 * libvlcjni. */
                mHandlerMainThread.post(new Runnable() {
                    @Override
                    public void run() { updateVideoSurfaces(); }
                });

                return new Event(eventType, arg1);
            case Event.ESAdded:
            case Event.ESDeleted:
            case Event.ESSelected:
                return new Event(eventType, arg1, arg2);
            case Event.SeekableChanged:
            case Event.PausableChanged:
                return new Event(eventType, arg1);
            case Event.RecordChanged:
                return new Event(eventType, arg1, args1);
        }
        return null;
    }

    @Override
    protected void onReleaseNative() {
        detachViews();
        mWindow.detachViews();
        registerAudioPlug(false);

        if (mMedia != null)
            mMedia.release();
        if (mRenderer != null)
            mRenderer.release();
        mVoutCount = 0;
        nativeRelease();
    }

    public boolean canDoPassthrough() {
        return mCanDoPassthrough;
    }

    /* JNI */
    public native long nativeSetTime(long time, boolean fast);
    public native void nativeSetPosition(float pos, boolean fast);
    private native void nativeNewFromLibVlc(ILibVLC ILibVLC, org.videolan.libvlc.AWindow window);
    private native void nativeNewFromMedia(IMedia media, AWindow window);
    private native void nativeRelease();
    private native void nativeSetMedia(IMedia media);
    private native void nativePlay();
    private native void nativeStop();
    private native int nativeSetRenderer(RendererItem item);
    private native void nativeSetVideoTitleDisplay(int position, int timeout);
    private native float nativeGetScale();
    private native void nativeSetScale(float scale);
    private native String nativeGetAspectRatio();
    private native void nativeSetAspectRatio(String aspect);
    private native boolean nativeUpdateViewpoint(float yaw, float pitch, float roll, float fov, boolean absolute);
    private native boolean nativeSetAudioOutput(String aout);
    private native boolean nativeSetAudioOutputDevice(String id);
    private native Title[] nativeGetTitles();
    private native Chapter[] nativeGetChapters(int title);
    private native org.videolan.libvlc.Media.Track[] nativeGetTracks(int type, boolean selected);
    private native org.videolan.libvlc.Media.Track nativeGetSelectedTrack(int type);
    private native Media.Track nativeGetTrackFromID(String id);
    private native boolean nativeSelectTrack(String id);
    private native void nativeSelectTracks(int type, String ids);
    private native void nativeUnselectTrackType(int type);
    private native long nativeGetAudioDelay();
    private native boolean nativeSetAudioDelay(long delay);
    private native long nativeGetSpuDelay();
    private native boolean nativeSetSpuDelay(long delay);
    private native boolean nativeAddSlave(int type, String location, boolean select);
    private native boolean nativeRecord(String directory, boolean enable);
    private native boolean nativeSetEqualizer(Equalizer equalizer);
    private native int nativeGetTeletext();
    private native void nativeSetTeletext(int page);
    private native boolean nativeGetTeletextTransparency();
    private native void nativeSetTeletextTransparency(boolean transparent);
}
