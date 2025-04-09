package org.videolan.libvlc.interfaces;

import org.videolan.libvlc.interfaces.AbstractVLCEvent;
import org.videolan.libvlc.interfaces.ILibVLC;

/* loaded from: classes.jar:org/videolan/libvlc/interfaces/IVLCObject.class */
public interface IVLCObject<T extends AbstractVLCEvent> {
    boolean retain();

    void release();

    boolean isReleased();

    ILibVLC getLibVLC();
}
