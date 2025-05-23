package org.videolan.libvlc;

import androidx.annotation.Nullable;

import org.videolan.libvlc.interfaces.AbstractVLCEvent;

@SuppressWarnings("unused, JniMissingFunction")
public class RendererItem extends org.videolan.libvlc.VLCObject<RendererItem.Event> {

    /** The renderer can render audio */
    public static final int LIBVLC_RENDERER_CAN_AUDIO = 0x0001;
    /** The renderer can render video */
    public static final int LIBVLC_RENDERER_CAN_VIDEO = 0x0002;

    final public String name;
    final public String displayName;
    final public String type;
    final String iconUrl;
    final int flags;
    private final long ref;

    RendererItem(String name, String type, String iconUrl, int flags, long ref) {
        final int index = name.lastIndexOf('-');
        this.name = name;
        this.displayName = index == -1 ? name : name.replace('-', ' ');
        this.type = type;
        this.iconUrl = iconUrl;
        this.flags = flags;
        this.ref = ref;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RendererItem && ref == ((RendererItem)obj).ref;
    }

    @Override
    protected Event onEventNative(int eventType, long arg1, long arg2, float argf1, @Nullable String args1) {
        return new Event(eventType);
    }

    @Override
    protected void onReleaseNative() {
        nativeReleaseItem();
    }

    public static class Event extends AbstractVLCEvent {
        protected Event(int type) {
            super(type);
        }
    }

    private native void nativeReleaseItem();
}
