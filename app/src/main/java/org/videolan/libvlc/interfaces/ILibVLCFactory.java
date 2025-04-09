package org.videolan.libvlc.interfaces;

import android.content.Context;

import java.util.List;

/* loaded from: classes.jar:org/videolan/libvlc/interfaces/ILibVLCFactory.class */
public interface ILibVLCFactory extends IComponentFactory {
    public static final String factoryId = ILibVLCFactory.class.getName();

    org.videolan.libvlc.interfaces.ILibVLC getFromOptions(Context context, List<String> list);

    ILibVLC getFromContext(Context context);
}
