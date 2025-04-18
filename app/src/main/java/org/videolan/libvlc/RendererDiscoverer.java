/*****************************************************************************
 * RendererDiscoverer.java
 *****************************************************************************
 * Copyright © 2017 VLC authors and VideoLAN
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package org.videolan.libvlc;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;

import org.videolan.libvlc.interfaces.AbstractVLCEvent;
import org.videolan.libvlc.interfaces.ILibVLC;

public class RendererDiscoverer extends org.videolan.libvlc.VLCObject<RendererDiscoverer.Event> {
    private final static String TAG = "LibVLC/RendererDiscoverer";

    final List<org.videolan.libvlc.RendererItem> mRenderers = new ArrayList<>();

    public static class Event extends AbstractVLCEvent {

        public static final int ItemAdded   = 0x502;
        public static final int ItemDeleted = 0x503;

        private final org.videolan.libvlc.RendererItem item;

        protected Event(int type, long nativeHolder, org.videolan.libvlc.RendererItem item) {
            super(type, nativeHolder);
            this.item = item;
            item.retain();
        }

        public org.videolan.libvlc.RendererItem getItem() {
            return item;
        }

        @Override
        public void release() {
            item.release();
            super.release();
        }
    }

    @SuppressWarnings("unused") /* Used from JNI */
    private static org.videolan.libvlc.RendererItem createItemFromNative(String name, String type, String iconUrl, int flags, long ref) {
        return new org.videolan.libvlc.RendererItem(name, type, iconUrl, flags, ref);
    }

    public interface EventListener extends AbstractVLCEvent.Listener<RendererDiscoverer.Event> {}

    /**
     * Create a MediaDiscover.
     *
     * @param ILibVLC a valid LibVLC
     * @param name Name of the vlc service discovery.
     */
    public RendererDiscoverer(ILibVLC ILibVLC, String name) {
        super(ILibVLC);
        nativeNew(ILibVLC, name);
    }

    /**
     * Starts the discovery. This RendererDiscoverer should be alive (not released).
     *
     * @return true the service is started
     */
    public boolean start() {
        if (isReleased()) throw new IllegalStateException("MediaDiscoverer is released");
        return nativeStart();
    }

    /**
     * Stops the discovery. This RendererDiscoverer should be alive (not released).
     * (You can also call {@link #release() to stop the discovery directly}.
     */
    public void stop() {
        if (isReleased()) throw new IllegalStateException("MediaDiscoverer is released");
        setEventListener(null);
        nativeStop();
        release();
    }

    public void setEventListener(EventListener listener) {
        super.setEventListener(listener);
    }

    public static Description[] list(ILibVLC ILibVlc) {
        return nativeList(ILibVlc);
    }

    public static class Description {
        public final String name;
        final String longName;
        private Description(String name, String longName) {
            this.name = name;
            this.longName = longName;
        }
    }

    @Override
    protected Event onEventNative(int eventType, long arg1, long arg2, float argf1, @Nullable String args1) {
        switch (eventType) {
            case Event.ItemAdded:
                return new Event(eventType, arg1, insertItemFromEvent(arg1));
            case Event.ItemDeleted:
                return new Event(eventType, arg1, removeItemFromEvent(arg1));
            default:
                return null;
        }
    }

    private final LongSparseArray<org.videolan.libvlc.RendererItem> index = new LongSparseArray<>();
    private synchronized org.videolan.libvlc.RendererItem insertItemFromEvent(long arg1) {
        final org.videolan.libvlc.RendererItem item = nativeNewItem(arg1);
        index.put(arg1, item);
        mRenderers.add(item);
        return item;
    }

    private synchronized org.videolan.libvlc.RendererItem removeItemFromEvent(long arg1) {
        final org.videolan.libvlc.RendererItem item = index.get(arg1);
        if (item != null) {
            index.remove(arg1);
            mRenderers.remove(item);
            item.release();
        }
        return item;
    }

    @Override
    protected void onReleaseNative() {
        for (org.videolan.libvlc.RendererItem item : mRenderers) item.release();
        mRenderers.clear();
        nativeRelease();
    }

    @SuppressWarnings("unused") /* Used from JNI */
    private static Description createDescriptionFromNative(String name, String longName) {
        return new Description(name, longName);
    }

    /* JNI */
    private native void nativeNew(ILibVLC ILibVLC, String name);
    private native void nativeRelease();
    private native boolean nativeStart();
    private native void nativeStop();
    private static native Description[] nativeList(ILibVLC ILibVLC);
    private native RendererItem nativeNewItem(long ref);
}
