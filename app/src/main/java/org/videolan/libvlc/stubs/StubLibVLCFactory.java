package org.videolan.libvlc.stubs;

import android.content.Context;
import java.util.List;
import org.videolan.libvlc.interfaces.ILibVLC;
import org.videolan.libvlc.interfaces.ILibVLCFactory;
import org.videolan.libvlc.stubs.StubLibVLC;

/* loaded from: classes.jar:org/videolan/libvlc/stubs/StubLibVLCFactory.class */
public class StubLibVLCFactory implements ILibVLCFactory {
    @Override // org.videolan.libvlc.interfaces.ILibVLCFactory
    public ILibVLC getFromOptions(Context context, List<String> options) {
        return new org.videolan.libvlc.stubs.StubLibVLC(context, options);
    }

    @Override // org.videolan.libvlc.interfaces.ILibVLCFactory
    public ILibVLC getFromContext(Context context) {
        return new StubLibVLC(context);
    }
}
