package org.videolan.libvlc;

import android.content.Context;
import android.util.Log;
import androidx.annotation.Nullable;
import java.util.List;

import org.videolan.libvlc.interfaces.AbstractVLCEvent;
import org.videolan.libvlc.interfaces.ILibVLC;

/* loaded from: classes.jar:org/videolan/libvlc/LibVLC.class */
public class LibVLC extends VLCObject<ILibVLC.Event> implements ILibVLC {
    private static final String TAG = "VLC/LibVLC";
    final Context mAppContext;
    private static boolean sLoaded = false;

    public static native String version();

    public static native int majorVersion();

    public static native String compiler();

    public static native String changeset();

    private native void nativeNew(String[] strArr, String str);

    private native void nativeRelease();

    private native void nativeSetUserAgent(String str, String str2);

    @Override // org.videolan.libvlc.VLCObject
    public /* bridge */ /* synthetic */ long getInstance() {
        return super.getInstance();
    }

    @Override // org.videolan.libvlc.VLCObject, org.videolan.libvlc.interfaces.IVLCObject
    public /* bridge */ /* synthetic */ ILibVLC getLibVLC() {
        return super.getLibVLC();
    }

    @Override // org.videolan.libvlc.VLCObject, org.videolan.libvlc.interfaces.IVLCObject
    public /* bridge */ /* synthetic */ boolean isReleased() {
        return super.isReleased();
    }

    /* loaded from: classes.jar:org/videolan/libvlc/LibVLC$Event.class */
    public static class Event extends AbstractVLCEvent {
        protected Event(int type) {
            super(type);
        }
    }

    public LibVLC(Context context, List<String> options) {
        this.mAppContext = context.getApplicationContext();
        loadLibraries();
        nativeNew(options != null ? (String[]) options.toArray(new String[options.size()]) : null, context.getDir("vlc", 0).getAbsolutePath());
    }

    public LibVLC(Context context) {
        this(context, null);
    }

    @Override // org.videolan.libvlc.VLCObject
    public ILibVLC.Event onEventNative(int eventType, long arg1, long arg2, float argf1, @Nullable String args1) {
        return null;
    }

    @Override // org.videolan.libvlc.interfaces.ILibVLC
    public Context getAppContext() {
        return this.mAppContext;
    }

    @Override // org.videolan.libvlc.VLCObject
    protected void onReleaseNative() {
        nativeRelease();
    }

    public void setUserAgent(String name, String http) {
        nativeSetUserAgent(name, http);
    }

    public static synchronized void loadLibraries() {
        if (sLoaded) {
            return;
        }
        sLoaded = true;
        try {
            System.loadLibrary("c++_shared");
            System.loadLibrary("vlc");
            System.loadLibrary("vlcjni");
        } catch (SecurityException se) {
            Log.e(TAG, "Encountered a security issue when loading vlcjni library: " + se);
//            System.exit(1);
        } catch (UnsatisfiedLinkError ule) {
            Log.e(TAG, "Can't load vlcjni library: " + ule);
//            System.exit(1);
        }
    }
}
