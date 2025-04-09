package org.videolan.libvlc.stubs;

import org.videolan.libvlc.interfaces.AbstractVLCEvent;
import org.videolan.libvlc.interfaces.ILibVLC;
import org.videolan.libvlc.interfaces.IVLCObject;

/* loaded from: classes.jar:org/videolan/libvlc/stubs/StubVLCObject.class */
public class StubVLCObject<T extends AbstractVLCEvent> implements IVLCObject<T> {
    @Override // org.videolan.libvlc.interfaces.IVLCObject
    public boolean retain() {
        return false;
    }

    @Override // org.videolan.libvlc.interfaces.IVLCObject
    public void release() {
    }

    @Override // org.videolan.libvlc.interfaces.IVLCObject
    public boolean isReleased() {
        return false;
    }

    @Override // org.videolan.libvlc.interfaces.IVLCObject
    public ILibVLC getLibVLC() {
        return null;
    }
}
