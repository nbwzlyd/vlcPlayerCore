
package player;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.view.Surface;
import android.view.SurfaceHolder;


import com.player.pluginlibrary.AbstractPlayer;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.MediaPlayer.EventListener;
import org.videolan.libvlc.interfaces.IMedia;
import org.videolan.libvlc.interfaces.IVLCVout;

import java.util.ArrayList;
import java.util.Map;

public class VlcPlayer extends AbstractPlayer {

    private LibVLC mLibVLC;
    private MediaPlayer mMediaPlayer;
    private Context mContext;
    private String mCurrentPath;
    private float mCurrentSpeed = 1.0f;
    private boolean mIsVideoEnable = true;

    public VlcPlayer(Context context) {
        this.mContext = context.getApplicationContext();
        initPlayer();
    }

    @Override
    public void initPlayer() {
        final ArrayList<String> args = new ArrayList<>();
        args.add("--avcodec-codec=h264");
        args.add("--avcodec-fast");
        args.add("--network-caching=1500");
        args.add("--clock-jitter=0");
        args.add("--clock-synchro=0");

        mLibVLC = new LibVLC(mContext, args);
        mMediaPlayer = new MediaPlayer(mLibVLC);
        mMediaPlayer.setEventListener(mVlcEventListener);
    }

    private final EventListener mVlcEventListener = new EventListener() {
        @Override
        public void onEvent(MediaPlayer.Event event) {
            switch (event.type) {
                case MediaPlayer.Event.EndReached:
                    if (mPlayerEventListener != null) {
                        mPlayerEventListener.onCompletion();
                    }
                    break;
                case MediaPlayer.Event.Playing:
                    break;
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                    // Handle player state changes if needed
                    break;
                case MediaPlayer.Event.Buffering:
                    if (mPlayerEventListener != null) {
                        float percentage = event.getBuffering();
                        if (percentage == 0f) {
                            mPlayerEventListener.onInfo(MEDIA_INFO_BUFFERING_START, 0);
                        } else if (percentage == 100f) {
                            mPlayerEventListener.onInfo(MEDIA_INFO_BUFFERING_END, 0);
                        }
                    }
                    break;
                case MediaPlayer.Event.EncounteredError:
                    if (mPlayerEventListener != null) {
                        mPlayerEventListener.onError();
                    }
                    break;
                case MediaPlayer.Event.MediaChanged:
                    mMediaPlayer.play();
                    break;
                case MediaPlayer.Event.Opening:
                    // Media is opening
                    break;
                case MediaPlayer.Event.PausableChanged:
                    // Pausable state changed
                    break;
                case MediaPlayer.Event.LengthChanged:
                    // Duration changed
                    break;
                case MediaPlayer.Event.Vout:
                    // Video output changed
                    if (mPlayerEventListener != null) {
                        mPlayerEventListener.onInfo(MEDIA_INFO_RENDERING_START, 0);
                    }
                    if (event.getVoutCount() > 0 && mPlayerEventListener != null) {
                        IMedia.Track[] track = mMediaPlayer.getMedia().getTracks(IMedia.Track.Type.Video);
                        IMedia.VideoTrack videoTrack = (IMedia.VideoTrack) track[0];
                        mPlayerEventListener.onVideoSizeChanged(videoTrack.width, videoTrack.height);
                    }
                    break;
                case MediaPlayer.Event.ESAdded:
                case MediaPlayer.Event.ESDeleted:
                case MediaPlayer.Event.ESSelected:
                    // ES (Elementary Stream) events
                    break;
                case MediaPlayer.Event.PositionChanged:
                    // Position changed
                    break;
                case MediaPlayer.Event.SeekableChanged:
                    // Seekable state changed
                    break;
                case MediaPlayer.Event.RecordChanged:
                    // Record state changed
                    break;
//                case MediaPlayer.Event.Corked:
//                case MediaPlayer.Event.Uncorked:
//                case MediaPlayer.Event.Muted:
//                case MediaPlayer.Event.Unmuted:
                // Audio state changes
//                    break;
            }
        }
    };

    @Override
    public void setDataSource(String path, Map<String, String> headers) {
        if (path == null) return;
        mCurrentPath = path;

        Media media;
        if (path.startsWith("http") || path.startsWith("https") || path.startsWith("rtsp")) {
            media = new Media(mLibVLC, Uri.parse(path));
            // Set HTTP headers if provided
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    media.addOption(":http-" + entry.getKey() + "=" + entry.getValue());
                }
            }
        } else {
            media = new Media(mLibVLC, path);
        }

        mMediaPlayer.setMedia(media);
        media.release();
    }

    @Override
    public void setDataSource(AssetFileDescriptor fd) {
        // VLC doesn't directly support AssetFileDescriptor, so we need to extract the file descriptor
//        try {
//            setDataSource(fd.getFileDescriptor());
//        } catch (Exception e) {
//            if (mPlayerEventListener != null) {
//                mPlayerEventListener.onError();
//            }
//        }
    }

    @Override
    public String getPlayUrl() {
        return mCurrentPath;
    }

    @Override
    public void start() {
        if (mMediaPlayer != null) {
            mMediaPlayer.play();
        }
    }

    @Override
    public void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    @Override
    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    @Override
    public void prepareAsync() {
//        if (mMediaPlayer != null) {
//            mMediaPlayer.play();
//        }
    }

    @Override
    public void reset() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.setMedia(null);
        }
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    @Override
    public void seekTo(long time) {
        if (mMediaPlayer != null && mMediaPlayer.isSeekable()) {
            float pos = (float) time / getDuration();
            mMediaPlayer.setPosition(pos);
        }
    }

    @Override
    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.setEventListener(null);
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mLibVLC != null) {
            mLibVLC.release();
            mLibVLC = null;
        }
        removeListeners();
    }

    @Override
    public long getCurrentPosition() {
        if (mMediaPlayer != null) {
            return (long) (mMediaPlayer.getPosition() * getDuration());
        }
        return 0;
    }

    @Override
    public long getDuration() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getLength();
        }
        return 0;
    }

    @Override
    public int getBufferedPercentage() {
        // VLC doesn't provide direct buffered percentage, so we estimate it
//        if (mMediaPlayer != null) {
//            return (int) (mMediaPlayer.getBuffering() * 100);
//        }
        return 0;
    }

    @Override
    public void setSurface(Surface surface) {
        if (mMediaPlayer != null) {
            mMediaPlayer.getVLCVout().setVideoSurface(surface,null);
            mMediaPlayer.getVLCVout().attachViews();
        }
    }

    @Override
    public void setDisplay(SurfaceHolder holder) {
        if (mMediaPlayer != null && holder != null) {
            mMediaPlayer.getVLCVout().setVideoSurface(holder.getSurface(), holder);
            mMediaPlayer.getVLCVout().setWindowSize(holder.getSurfaceFrame().width(), holder.getSurfaceFrame().height());
            mMediaPlayer.getVLCVout().attachViews();
        }
    }

    @Override
    public void releaseSurface(SurfaceHolder holder) {
        if (mMediaPlayer != null) {
            mMediaPlayer.getVLCVout().detachViews();
        }
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        if (mMediaPlayer != null) {
            // VLC uses a single volume value (0-100)
            float volume = Math.max(leftVolume, rightVolume) * 100;
            mMediaPlayer.setVolume((int) volume);
        }
    }

    @Override
    public void setLooping(boolean isLooping) {
//        if (mMediaPlayer != null) {
//            mMediaPlayer.setRepeat(isLooping ? MediaPlayer.Repeat.All : MediaPlayer.Repeat.None);
//        }
    }

    @Override
    public void setOptions() {
        // Additional VLC options can be set here
    }

    @Override
    public void setSpeed(float speed) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setRate(speed);
            mCurrentSpeed = speed;
        }
    }

    @Override
    public float getSpeed() {
        return mCurrentSpeed;
    }

    @Override
    public long getTcpSpeed() {
        // VLC doesn't provide direct access to TCP speed
        return 0;
    }

    @Override
    public void setVideoEnable(boolean enable) {
        mIsVideoEnable = enable;
        if (mMediaPlayer != null) {
            mMediaPlayer.setVideoTrackEnabled(enable);
        }
    }

    @Override
    public void selectLocalSubtitle(String path) {
        if (mMediaPlayer != null && path != null) {
            mMediaPlayer.addSlave(Media.Slave.Type.Subtitle, path, true);
        }
    }

    public MediaPlayer getInternalPlayer(){
        return mMediaPlayer;
    }

    public void setWindowSize(int width, int height) {

        getInternalPlayer().getVLCVout().setWindowSize(width,height);
    }
}