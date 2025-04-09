package org.videolan.libvlc;

import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import java.io.FileDescriptor;

import org.videolan.libvlc.Media;
import org.videolan.libvlc.interfaces.ILibVLC;
import org.videolan.libvlc.interfaces.IMedia;
import org.videolan.libvlc.interfaces.IMediaFactory;

/* loaded from: classes.jar:org/videolan/libvlc/MediaFactory.class */
public class MediaFactory implements IMediaFactory {
    @Override // org.videolan.libvlc.interfaces.IMediaFactory
    public IMedia getFromLocalPath(ILibVLC ILibVLC, String path) {
        return new org.videolan.libvlc.Media(ILibVLC, path);
    }

    @Override // org.videolan.libvlc.interfaces.IMediaFactory
    public IMedia getFromUri(ILibVLC ILibVLC, Uri uri) {
        return new org.videolan.libvlc.Media(ILibVLC, uri);
    }

    @Override // org.videolan.libvlc.interfaces.IMediaFactory
    public IMedia getFromFileDescriptor(ILibVLC ILibVLC, FileDescriptor fd) {
        return new org.videolan.libvlc.Media(ILibVLC, fd);
    }

    @Override // org.videolan.libvlc.interfaces.IMediaFactory
    public IMedia getFromAssetFileDescriptor(ILibVLC ILibVLC, AssetFileDescriptor assetFileDescriptor) {
        return new Media(ILibVLC, assetFileDescriptor);
    }
}
