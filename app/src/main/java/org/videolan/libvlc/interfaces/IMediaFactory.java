package org.videolan.libvlc.interfaces;

import android.content.res.AssetFileDescriptor;
import android.net.Uri;

import java.io.FileDescriptor;

/* loaded from: classes.jar:org/videolan/libvlc/interfaces/IMediaFactory.class */
public interface IMediaFactory extends IComponentFactory {
    public static final String factoryId = IMediaFactory.class.getName();

    org.videolan.libvlc.interfaces.IMedia getFromLocalPath(org.videolan.libvlc.interfaces.ILibVLC iLibVLC, String str);

    org.videolan.libvlc.interfaces.IMedia getFromUri(org.videolan.libvlc.interfaces.ILibVLC iLibVLC, Uri uri);

    org.videolan.libvlc.interfaces.IMedia getFromFileDescriptor(org.videolan.libvlc.interfaces.ILibVLC iLibVLC, FileDescriptor fileDescriptor);

    IMedia getFromAssetFileDescriptor(ILibVLC iLibVLC, AssetFileDescriptor assetFileDescriptor);
}
