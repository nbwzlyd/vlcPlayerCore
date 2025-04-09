package org.videolan.libvlc.stubs;

import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import java.io.FileDescriptor;
import org.videolan.libvlc.interfaces.ILibVLC;
import org.videolan.libvlc.interfaces.IMedia;
import org.videolan.libvlc.interfaces.IMediaFactory;
import org.videolan.libvlc.stubs.StubMedia;

/* loaded from: classes.jar:org/videolan/libvlc/stubs/StubMediaFactory.class */
public class StubMediaFactory implements IMediaFactory {
    @Override // org.videolan.libvlc.interfaces.IMediaFactory
    public IMedia getFromLocalPath(ILibVLC ILibVLC, String path) {
        return new org.videolan.libvlc.stubs.StubMedia(ILibVLC, path);
    }

    @Override // org.videolan.libvlc.interfaces.IMediaFactory
    public IMedia getFromUri(ILibVLC ILibVLC, Uri uri) {
        return new org.videolan.libvlc.stubs.StubMedia(ILibVLC, uri);
    }

    @Override // org.videolan.libvlc.interfaces.IMediaFactory
    public IMedia getFromFileDescriptor(ILibVLC ILibVLC, FileDescriptor fd) {
        return new org.videolan.libvlc.stubs.StubMedia(ILibVLC, fd);
    }

    @Override // org.videolan.libvlc.interfaces.IMediaFactory
    public IMedia getFromAssetFileDescriptor(ILibVLC ILibVLC, AssetFileDescriptor assetFileDescriptor) {
        return new StubMedia(ILibVLC, assetFileDescriptor);
    }
}
