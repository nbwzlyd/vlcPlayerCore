/*****************************************************************************
 * MediaDiscoverer.java
 *****************************************************************************
 * Copyright © 2015 VLC authors, VideoLAN and VideoLabs
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package org.videolan.libvlc;

import androidx.annotation.Nullable;

import org.videolan.libvlc.interfaces.AbstractVLCEvent;
import org.videolan.libvlc.interfaces.ILibVLC;

@SuppressWarnings("unused, JniMissingFunction")
public class MediaDiscoverer extends org.videolan.libvlc.VLCObject<MediaDiscoverer.Event> {
    private final static String TAG = "LibVLC/MediaDiscoverer";

    public static class Event extends AbstractVLCEvent {

        public static final int Started = 0x500;
        public static final int Ended   = 0x501;

        protected Event(int type) {
            super(type);
        }
    }

    public static class Description {
        public static class Category {
            /** devices, like portable music player */
            public static final int Devices = 0;
            /** LAN/WAN services, like Upnp, SMB, or SAP */
            public static final int Lan = 1;
            /** Podcasts */
            public static final int Podcasts = 2;
            /** Local directories, like Video, Music or Pictures directories */
            public static final int LocalDirs = 3;
        }
        public final String name;
        public final String longName;
        public final int category;

        private Description(String name, String longName, int category)
        {
            this.name = name;
            this.longName = longName;
            this.category = category;
        }
    }

    @SuppressWarnings("unused") /* Used from JNI */
    private static Description createDescriptionFromNative(String name, String longName, int category)
    {
        return new Description(name, longName, category);
    }

    public interface EventListener extends AbstractVLCEvent.Listener<MediaDiscoverer.Event> {}

    private org.videolan.libvlc.MediaList mMediaList = null;

    /**
     * Create a MediaDiscover.
     *
     * @param ILibVLC a valid LibVLC
     * @param name Name of the vlc service discovery ("dsm", "upnp", "bonjour"...).
     */
    public MediaDiscoverer(ILibVLC ILibVLC, String name) {
        super(ILibVLC);
        nativeNew(ILibVLC, name);
    }

    /**
     * Starts the discovery. This MediaDiscoverer should be alive (not released).
     *
     * @return true the service is started
     */
    public boolean start() {
        if (isReleased())
            throw new IllegalStateException("MediaDiscoverer is released");
        return nativeStart();
    }

    /**
     * Stops the discovery. This MediaDiscoverer should be alive (not released).
     * (You can also call {@link #release() to stop the discovery directly}.
     */
    public void stop() {
        if (isReleased())
            throw new IllegalStateException("MediaDiscoverer is released");
        nativeStop();
    }

    public void setEventListener(EventListener listener) {
        super.setEventListener(listener);
    }

    @Override
    protected Event onEventNative(int eventType, long arg1, long arg2, float argf1, @Nullable String args1) {
        switch (eventType) {
            case Event.Started:
            case Event.Ended:
                return new Event(eventType);
        }
        return null;
    }

    /**
     * Get the MediaList associated with the MediaDiscoverer.
     * This MediaDiscoverer should be alive (not released).
     *
     * @return MediaList. This MediaList should be released with {@link #release()}.
     */
    public org.videolan.libvlc.MediaList getMediaList() {
        synchronized (this) {
            if (mMediaList != null) {
                mMediaList.retain();
                return mMediaList;
            }
        }
        final org.videolan.libvlc.MediaList mediaList = new MediaList(this);
        synchronized (this) {
            mMediaList = mediaList;
            mMediaList.retain();
            return mMediaList;
        }
    }

    @Override
    protected void onReleaseNative() {
        if (mMediaList != null)
            mMediaList.release();
        nativeRelease();
    }

    /**
     * Get media discoverers by category
     * @param category see {@link Description.Category}
     */
    @Nullable
    public static Description[] list(ILibVLC ILibVLC, int category) {
        return nativeList(ILibVLC, category);
    }

    /* JNI */
    private native void nativeNew(ILibVLC ILibVLC, String name);
    private native void nativeRelease();
    private native boolean nativeStart();
    private native void nativeStop();
    private static native Description[] nativeList(ILibVLC ILibVLC, int category);
}
