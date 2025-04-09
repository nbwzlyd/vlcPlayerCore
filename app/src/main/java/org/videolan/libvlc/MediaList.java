package org.videolan.libvlc;

import android.os.Handler;
import android.util.SparseArray;
import androidx.annotation.Nullable;

import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaDiscoverer;
import org.videolan.libvlc.VLCObject;
import org.videolan.libvlc.interfaces.AbstractVLCEvent;
import org.videolan.libvlc.interfaces.ILibVLC;
import org.videolan.libvlc.interfaces.IMedia;
import org.videolan.libvlc.interfaces.IMediaList;

/* loaded from: classes.jar:org/videolan/libvlc/MediaList.class */
public class MediaList extends org.videolan.libvlc.VLCObject<IMediaList.Event> implements IMediaList {
    private static final String TAG = "LibVLC/MediaList";
    private int mCount = 0;
    private final SparseArray<IMedia> mMediaArray = new SparseArray<>();
    private boolean mLocked = false;

    private native void nativeNewFromLibVlc(ILibVLC iLibVLC);

    private native void nativeNewFromMediaDiscoverer(org.videolan.libvlc.MediaDiscoverer mediaDiscoverer);

    private native void nativeNewFromMedia(IMedia iMedia);

    private native void nativeRelease();

    private native int nativeGetCount();

    private native void nativeLock();

    private native void nativeUnlock();

    @Override // org.videolan.libvlc.VLCObject
    public /* bridge */ /* synthetic */ long getInstance() {
        return super.getInstance();
    }

    @Override // org.videolan.libvlc.VLCObject, org.videolan.libvlc.interfaces.IVLCObject
    public /* bridge */ /* synthetic */ ILibVLC getLibVLC() {
        return super.getLibVLC();
    }

    @Override // org.videolan.libvlc.VLCObject, org.videolan.libvlc.interfaces.IVLCObject
    public /* bridge */ /* synthetic */ boolean isReleased() {
        return super.isReleased();
    }

    private void init() {
        lock();
        this.mCount = nativeGetCount();
        for (int i = 0; i < this.mCount; i++) {
            this.mMediaArray.put(i, new org.videolan.libvlc.Media(this, i));
        }
        unlock();
    }

    public MediaList(ILibVLC ILibVLC) {
        super(ILibVLC);
        nativeNewFromLibVlc(ILibVLC);
        init();
    }

    public MediaList(MediaDiscoverer md) {
        super(md);
        nativeNewFromMediaDiscoverer(md);
        init();
    }

    public MediaList(IMedia m) {
        super(m);
        nativeNewFromMedia(m);
        init();
    }

    private synchronized IMedia insertMediaFromEvent(int index) {
        for (int i = this.mCount - 1; i >= index; i--) {
            this.mMediaArray.put(i + 1, this.mMediaArray.valueAt(i));
        }
        this.mCount++;
        IMedia media = new Media(this, index);
        this.mMediaArray.put(index, media);
        return media;
    }

    private synchronized IMedia removeMediaFromEvent(int index) {
        this.mCount--;
        IMedia media = this.mMediaArray.get(index);
        if (media != null) {
            media.release();
        }
        for (int i = index; i < this.mCount; i++) {
            this.mMediaArray.put(i, this.mMediaArray.valueAt(i + 1));
        }
        return media;
    }

    @Override // org.videolan.libvlc.interfaces.IMediaList
    public void setEventListener(EventListener listener, Handler handler) {
        super.setEventListener((AbstractVLCEvent.Listener) listener, handler);
    }

    @Override // org.videolan.libvlc.VLCObject
    public synchronized Event onEventNative(int eventType, long arg1, long arg2, float argf1, @Nullable String args1) {
        if (this.mLocked) {
            throw new IllegalStateException("already locked from event callback");
        }
        this.mLocked = true;
        Event event = null;
        switch (eventType) {
            case Event.ItemAdded /* 512 */:
                int index = (int) arg1;
                if (index != -1) {
                    IMedia media = insertMediaFromEvent(index);
                    event = new Event(eventType, media, true, index);
                    break;
                }
                break;
            case Event.ItemDeleted /* 514 */:
                int index2 = (int) arg1;
                if (index2 != -1) {
                    IMedia media2 = removeMediaFromEvent(index2);
                    event = new Event(eventType, media2, false, index2);
                    break;
                }
                break;
            case Event.EndReached /* 516 */:
                event = new Event(eventType, null, false, -1);
                break;
        }
        this.mLocked = false;
        return event;
    }

    @Override // org.videolan.libvlc.interfaces.IMediaList
    public synchronized int getCount() {
        return this.mCount;
    }

    @Override // org.videolan.libvlc.interfaces.IMediaList
    public synchronized IMedia getMediaAt(int index) {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException();
        }
        IMedia media = this.mMediaArray.get(index);
        media.retain();
        return media;
    }

    @Override // org.videolan.libvlc.VLCObject
    public void onReleaseNative() {
        for (int i = 0; i < this.mMediaArray.size(); i++) {
            IMedia media = this.mMediaArray.get(i);
            if (media != null) {
                media.release();
            }
        }
        nativeRelease();
    }

    private synchronized void lock() {
        if (this.mLocked) {
            throw new IllegalStateException("already locked");
        }
        this.mLocked = true;
        nativeLock();
    }

    private synchronized void unlock() {
        if (!this.mLocked) {
            throw new IllegalStateException("not locked");
        }
        this.mLocked = false;
        nativeUnlock();
    }

    @Override // org.videolan.libvlc.interfaces.IMediaList
    public synchronized boolean isLocked() {
        return this.mLocked;
    }
}
