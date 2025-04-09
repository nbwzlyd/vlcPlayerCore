package org.videolan.libvlc.interfaces;

import android.content.Context;

/* loaded from: classes.jar:org/videolan/libvlc/interfaces/ILibVLC.class */
public interface ILibVLC extends IVLCObject<ILibVLC.Event> {
    Context getAppContext();

    public static class Event extends AbstractVLCEvent {
        protected Event(int type) {
            super(type);
        }
    }
}
