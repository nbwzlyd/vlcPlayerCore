package org.videolan.libvlc;

import android.content.Context;
import java.util.List;

import org.videolan.libvlc.interfaces.ILibVLC;
import org.videolan.libvlc.interfaces.ILibVLCFactory;

/* loaded from: classes.jar:org/videolan/libvlc/LibVLCFactory.class */
public class LibVLCFactory implements ILibVLCFactory {
    static {
        FactoryManager.registerFactory(ILibVLCFactory.factoryId, new LibVLCFactory());
    }

    @Override // org.videolan.libvlc.interfaces.ILibVLCFactory
    public ILibVLC getFromOptions(Context context, List<String> options) {
        return new org.videolan.libvlc.LibVLC(context, options);
    }

    @Override // org.videolan.libvlc.interfaces.ILibVLCFactory
    public ILibVLC getFromContext(Context context) {
        return new LibVLC(context, null);
    }
}
