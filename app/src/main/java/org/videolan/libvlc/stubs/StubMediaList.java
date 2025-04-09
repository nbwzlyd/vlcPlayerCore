package org.videolan.libvlc.stubs;

import android.os.Handler;
import org.videolan.libvlc.interfaces.IMedia;
import org.videolan.libvlc.interfaces.IMediaList;
import org.videolan.libvlc.stubs.StubVLCObject;

/* loaded from: classes.jar:org/videolan/libvlc/stubs/StubMediaList.class */
public class StubMediaList extends StubVLCObject<IMediaList.Event> implements IMediaList {
    @Override // org.videolan.libvlc.interfaces.IMediaList
    public void setEventListener(EventListener listener, Handler handler) {
    }

    @Override // org.videolan.libvlc.interfaces.IMediaList
    public int getCount() {
        return 0;
    }

    @Override // org.videolan.libvlc.interfaces.IMediaList
    public IMedia getMediaAt(int index) {
        return null;
    }

    @Override // org.videolan.libvlc.interfaces.IMediaList
    public boolean isLocked() {
        return false;
    }
}
