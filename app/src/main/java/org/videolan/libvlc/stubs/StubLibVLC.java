package org.videolan.libvlc.stubs;

import android.content.Context;
import java.util.List;
import org.videolan.libvlc.interfaces.ILibVLC;
import org.videolan.libvlc.stubs.StubVLCObject;

/* loaded from: classes.jar:org/videolan/libvlc/stubs/StubLibVLC.class */
public class StubLibVLC extends StubVLCObject<ILibVLC.Event> implements ILibVLC {
    private final Context mContext;

    public StubLibVLC(Context context, List<String> options) {
        this.mContext = context;
    }

    public StubLibVLC(Context context) {
        this(context, null);
    }

    @Override // org.videolan.libvlc.interfaces.ILibVLC
    public Context getAppContext() {
        return this.mContext;
    }
}
