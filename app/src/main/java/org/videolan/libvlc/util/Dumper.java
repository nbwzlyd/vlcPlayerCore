package org.videolan.libvlc.util;

import android.net.Uri;
import androidx.annotation.MainThread;
import java.util.ArrayList;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.ILibVLC;
import org.videolan.libvlc.interfaces.IMedia;

/* loaded from: classes.jar:org/videolan/libvlc/util/Dumper.class */
public class Dumper {
    private final ILibVLC mILibVLC;
    private final MediaPlayer mMediaPlayer;
    private final Listener mListener;

    /* loaded from: classes.jar:org/videolan/libvlc/util/Dumper$Listener.class */
    public interface Listener {
        void onFinish(boolean z);

        void onProgress(float f);
    }

    @MainThread
    public Dumper(Uri uri, String filepath, Listener listener) {
        if (uri == null || filepath == null || listener == null) {
            throw new IllegalArgumentException("arguments shouldn't be null");
        }
        this.mListener = listener;
        ArrayList<String> options = new ArrayList<>(8);
        options.add("--demux");
        options.add("dump2,none");
        options.add("--demuxdump-file");
        options.add(filepath);
        options.add("--no-video");
        options.add("--no-audio");
        options.add("--no-spu");
        options.add("-vv");
        this.mILibVLC = new LibVLC(null, options);
        IMedia media = new Media(this.mILibVLC, uri);
        this.mMediaPlayer = new MediaPlayer(media);
        this.mMediaPlayer.setEventListener(new MediaPlayer.EventListener() { // from class: org.videolan.libvlc.util.Dumper.1
            public void onEvent(MediaPlayer.Event event) {
                switch (event.type) {
                    case MediaPlayer.Event.Buffering /* 259 */:
                        Dumper.this.mListener.onProgress(event.getBuffering());
                        return;
                    case MediaPlayer.Event.EndReached /* 265 */:
                    case MediaPlayer.Event.EncounteredError /* 266 */:
                        Dumper.this.mListener.onFinish(event.type == 265);
                        Dumper.this.cancel();
                        return;
                    default:
                        return;
                }
            }
        });
        media.release();
    }

    @MainThread
    public void start() {
        this.mMediaPlayer.play();
    }

    @MainThread
    public void cancel() {
        this.mMediaPlayer.stop();
        this.mMediaPlayer.release();
        this.mILibVLC.release();
    }
}
