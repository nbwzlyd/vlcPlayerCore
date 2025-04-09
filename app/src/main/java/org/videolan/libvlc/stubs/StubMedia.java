package org.videolan.libvlc.stubs;

import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import java.io.FileDescriptor;
import org.videolan.libvlc.interfaces.ILibVLC;
import org.videolan.libvlc.interfaces.IMedia;
import org.videolan.libvlc.interfaces.IMediaList;
import org.videolan.libvlc.stubs.StubMediaList;
import org.videolan.libvlc.stubs.StubVLCObject;

/* loaded from: classes.jar:org/videolan/libvlc/stubs/StubMedia.class */
public class StubMedia extends StubVLCObject<IMedia.Event> implements IMedia {
    private Uri mUri;
    private ILibVLC mILibVLC;
    private int mType;

    public StubMedia(ILibVLC ILibVLC, String path) {
        this(ILibVLC, Uri.parse(path));
    }

    public StubMedia(ILibVLC ILibVLC, Uri uri) {
        this.mType = 0;
        this.mUri = uri;
        this.mILibVLC = ILibVLC;
    }

    public StubMedia(ILibVLC ILibVLC, FileDescriptor fd) {
        this.mType = 0;
        this.mILibVLC = ILibVLC;
    }

    public StubMedia(ILibVLC ILibVLC, AssetFileDescriptor assetFileDescriptor) {
        this.mType = 0;
        this.mILibVLC = ILibVLC;
    }

    @Override // org.videolan.libvlc.interfaces.IMedia
    public long getDuration() {
        return 0L;
    }

    @Override // org.videolan.libvlc.interfaces.IMedia
    public IMediaList subItems() {
        return new StubMediaList();
    }

    @Override // org.videolan.libvlc.interfaces.IMedia
    public boolean parse(int flags) {
        return false;
    }

    @Override // org.videolan.libvlc.interfaces.IMedia
    public boolean parse() {
        return false;
    }

    @Override // org.videolan.libvlc.interfaces.IMedia
    public boolean parseAsync(int flags, int timeout) {
        return false;
    }

    @Override // org.videolan.libvlc.interfaces.IMedia
    public boolean parseAsync(int flags) {
        return false;
    }

    @Override // org.videolan.libvlc.interfaces.IMedia
    public boolean parseAsync() {
        return false;
    }

    @Override // org.videolan.libvlc.interfaces.IMedia
    public int getType() {
        return this.mType;
    }

    @Override // org.videolan.libvlc.interfaces.IMedia
    public Track[] getTracks(int type) {
        return null;
    }

    @Override // org.videolan.libvlc.interfaces.IMedia
    public Track[] getTracks() {
        return null;
    }

    @Override // org.videolan.libvlc.interfaces.IMedia
    public String getMeta(int id) {
        if (this.mUri == null) {
            return null;
        }
        switch (id) {
            case 0:
                return getTitle();
            case Meta.URL /* 10 */:
                return this.mUri.getPath();
            default:
                return null;
        }
    }

    @Override // org.videolan.libvlc.interfaces.IMedia
    public String getMeta(int id, boolean force) {
        return getMeta(id);
    }

    private String getTitle() {
        if ("file".equals(this.mUri.getScheme())) {
            return this.mUri.getLastPathSegment();
        }
        return this.mUri.getPath();
    }

    @Override // org.videolan.libvlc.interfaces.IMedia
    public void setHWDecoderEnabled(boolean enabled, boolean force) {
    }

    @Override // org.videolan.libvlc.interfaces.IMedia
    public void setEventListener(EventListener listener) {
    }

    @Override // org.videolan.libvlc.interfaces.IMedia
    public void addOption(String option) {
    }

    @Override // org.videolan.libvlc.interfaces.IMedia
    public void addSlave(Slave slave) {
    }

    @Override // org.videolan.libvlc.interfaces.IMedia
    public void clearSlaves() {
    }

    @Override // org.videolan.libvlc.interfaces.IMedia
    public Slave[] getSlaves() {
        return new Slave[0];
    }

    @Override // org.videolan.libvlc.interfaces.IMedia
    public Uri getUri() {
        return this.mUri;
    }

    @Override // org.videolan.libvlc.interfaces.IMedia
    public boolean isParsed() {
        return false;
    }

    @Override // org.videolan.libvlc.interfaces.IMedia
    public Stats getStats() {
        return null;
    }

    @Override // org.videolan.libvlc.interfaces.IMedia
    public void setDefaultMediaPlayerOptions() {
    }

    @Override // org.videolan.libvlc.stubs.StubVLCObject, org.videolan.libvlc.interfaces.IVLCObject
    public boolean retain() {
        return false;
    }

    @Override // org.videolan.libvlc.stubs.StubVLCObject, org.videolan.libvlc.interfaces.IVLCObject
    public void release() {
    }

    @Override // org.videolan.libvlc.stubs.StubVLCObject, org.videolan.libvlc.interfaces.IVLCObject
    public boolean isReleased() {
        return false;
    }

    @Override // org.videolan.libvlc.stubs.StubVLCObject, org.videolan.libvlc.interfaces.IVLCObject
    public ILibVLC getLibVLC() {
        return this.mILibVLC;
    }

    public void setType(int type) {
        this.mType = type;
    }
}
