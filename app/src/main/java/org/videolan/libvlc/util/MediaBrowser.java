package org.videolan.libvlc.util;

import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import androidx.annotation.MainThread;
import java.util.ArrayList;
import java.util.Iterator;
import org.videolan.libvlc.FactoryManager;
import org.videolan.libvlc.MediaDiscoverer;
import org.videolan.libvlc.MediaList;
import org.videolan.libvlc.interfaces.ILibVLC;
import org.videolan.libvlc.interfaces.IMedia;
import org.videolan.libvlc.interfaces.IMediaFactory;
import org.videolan.libvlc.interfaces.IMediaList;

/* loaded from: classes.jar:org/videolan/libvlc/util/MediaBrowser.class */
public class MediaBrowser {
    private static final String TAG = "MediaBrowser";
    private final ILibVLC mILibVlc;
    private final ArrayList<MediaDiscoverer> mMediaDiscoverers;
    private final ArrayList<IMedia> mDiscovererMediaArray;
    private IMediaList mBrowserMediaList;
    private IMedia mMedia;
    private EventListener mEventListener;
    private Handler mHandler;
    private boolean mAlive;
    private IMediaFactory mFactory;
    private static final String IGNORE_LIST_OPTION = ":ignore-filetypes=";
    private String mIgnoreList;
    private final IMediaList.EventListener mBrowserMediaListEventListener;
    private final IMediaList.EventListener mDiscovererMediaListEventListener;

    /* loaded from: classes.jar:org/videolan/libvlc/util/MediaBrowser$EventListener.class */
    public interface EventListener {
        void onMediaAdded(int i, IMedia iMedia);

        void onMediaRemoved(int i, IMedia iMedia);

        void onBrowseEnd();
    }

    /* loaded from: classes.jar:org/videolan/libvlc/util/MediaBrowser$Flag.class */
    public static class Flag {
        public static final int Interact = 1;
        public static final int NoSlavesAutodetect = 2;
        public static final int ShowHiddenFiles = 4;
    }

    public MediaBrowser(ILibVLC libvlc, EventListener listener) {
        this.mMediaDiscoverers = new ArrayList<>();
        this.mDiscovererMediaArray = new ArrayList<>();
        this.mIgnoreList = "db,nfo,ini,jpg,jpeg,ljpg,gif,png,pgm,pgmyuv,pbm,pam,tga,bmp,pnm,xpm,xcf,pcx,tif,tiff,lbm,sfv,txt,sub,idx,srt,ssa,ass,smi,utf,utf-8,rt,aqt,txt,usf,jss,cdg,psb,mpsub,mpl2,pjs,dks,stl,vtt,ttml";
        this.mBrowserMediaListEventListener = new IMediaList.EventListener() { // from class: org.videolan.libvlc.util.MediaBrowser.1
            public void onEvent(IMediaList.Event event) {
                if (MediaBrowser.this.mEventListener == null) {
                    return;
                }
                switch (event.type) {
                    case IMediaList.Event.ItemAdded /* 512 */:
                        MediaBrowser.this.mEventListener.onMediaAdded(event.index, event.media);
                        return;
                    case 513:
                    case 515:
                    default:
                        return;
                    case IMediaList.Event.ItemDeleted /* 514 */:
                        MediaBrowser.this.mEventListener.onMediaRemoved(event.index, event.media);
                        return;
                    case IMediaList.Event.EndReached /* 516 */:
                        MediaBrowser.this.mEventListener.onBrowseEnd();
                        return;
                }
            }
        };
        this.mDiscovererMediaListEventListener = new IMediaList.EventListener() { // from class: org.videolan.libvlc.util.MediaBrowser.2
            public void onEvent(IMediaList.Event event) {
                if (MediaBrowser.this.mEventListener == null) {
                    return;
                }
                switch (event.type) {
                    case IMediaList.Event.ItemAdded /* 512 */:
                        MediaBrowser.this.mDiscovererMediaArray.add(event.media);
                        MediaBrowser.this.mEventListener.onMediaAdded(-1, event.media);
                        return;
                    case 513:
                    case 515:
                    default:
                        return;
                    case IMediaList.Event.ItemDeleted /* 514 */:
                        int index = MediaBrowser.this.mDiscovererMediaArray.indexOf(event.media);
                        if (index != -1) {
                            MediaBrowser.this.mDiscovererMediaArray.remove(index);
                        }
                        if (index != -1) {
                            MediaBrowser.this.mEventListener.onMediaRemoved(index, event.media);
                            return;
                        }
                        return;
                    case IMediaList.Event.EndReached /* 516 */:
                        MediaBrowser.this.mEventListener.onBrowseEnd();
                        return;
                }
            }
        };
        this.mFactory = (IMediaFactory) FactoryManager.getFactory(IMediaFactory.factoryId);
        this.mILibVlc = libvlc;
        this.mILibVlc.retain();
        this.mEventListener = listener;
        this.mAlive = true;
    }

    public MediaBrowser(ILibVLC libvlc, EventListener listener, Handler handler) {
        this(libvlc, listener);
        this.mHandler = handler;
    }

    private void reset() {
        Iterator<MediaDiscoverer> it = this.mMediaDiscoverers.iterator();
        while (it.hasNext()) {
            MediaDiscoverer md = it.next();
            md.release();
        }
        this.mMediaDiscoverers.clear();
        this.mDiscovererMediaArray.clear();
        if (this.mMedia != null) {
            this.mMedia.release();
            this.mMedia = null;
        }
        if (this.mBrowserMediaList != null) {
            this.mBrowserMediaList.release();
            this.mBrowserMediaList = null;
        }
    }

    @MainThread
    public void release() {
        reset();
        if (!this.mAlive) {
            throw new IllegalStateException("MediaBrowser released more than one time");
        }
        this.mILibVlc.release();
        this.mAlive = false;
    }

    @MainThread
    public void changeEventListener(EventListener eventListener) {
        reset();
        this.mEventListener = eventListener;
    }

    private void startMediaDiscoverer(String discovererName) {
        MediaDiscoverer md = new MediaDiscoverer(this.mILibVlc, discovererName);
        this.mMediaDiscoverers.add(md);
        MediaList ml = md.getMediaList();
        ml.setEventListener(this.mDiscovererMediaListEventListener, this.mHandler);
        ml.release();
        if (!md.isReleased()) {
            md.start();
        }
    }

    @MainThread
    public void discoverNetworkShares() {
        reset();
        MediaDiscoverer.Description[] descriptions = MediaDiscoverer.list(this.mILibVlc, 1);
        if (descriptions == null) {
            return;
        }
        for (MediaDiscoverer.Description description : descriptions) {
            Log.i(TAG, "starting " + description.name + " discover (" + description.longName + ")");
            startMediaDiscoverer(description.name);
        }
    }

    @MainThread
    public void discoverNetworkShares(String serviceName) {
        reset();
        startMediaDiscoverer(serviceName);
    }

    @MainThread
    public void browse(String path, int flags) {
        IMedia media = this.mFactory.getFromLocalPath(this.mILibVlc, path);
        browse(media, flags);
        media.release();
    }

    @MainThread
    public void browse(Uri uri, int flags) {
        IMedia media = this.mFactory.getFromUri(this.mILibVlc, uri);
        browse(media, flags);
        media.release();
    }

    @MainThread
    public void browse(IMedia media, int flags) {
        media.retain();
        media.addOption(IGNORE_LIST_OPTION + this.mIgnoreList);
        if ((flags & 2) != 0) {
            media.addOption(":no-sub-autodetect-file");
        }
        if ((flags & 4) != 0) {
            media.addOption(":show-hiddenfiles");
        }
        int mediaFlags = 2;
        if ((flags & 1) != 0) {
            mediaFlags = 2 | 32;
        }
        reset();
        this.mBrowserMediaList = media.subItems();
        this.mBrowserMediaList.setEventListener(this.mBrowserMediaListEventListener, this.mHandler);
        media.parseAsync(mediaFlags, 0);
        this.mMedia = media;
    }

    @MainThread
    public int getMediaCount() {
        return this.mBrowserMediaList != null ? this.mBrowserMediaList.getCount() : this.mDiscovererMediaArray.size();
    }

    @MainThread
    public IMedia getMediaAt(int index) {
        if (index < 0 || index >= getMediaCount()) {
            throw new IndexOutOfBoundsException();
        }
        IMedia media = this.mBrowserMediaList != null ? this.mBrowserMediaList.getMediaAt(index) : this.mDiscovererMediaArray.get(index);
        media.retain();
        return media;
    }

    @MainThread
    public void setIgnoreFileTypes(String list) {
        this.mIgnoreList = list;
    }
}
