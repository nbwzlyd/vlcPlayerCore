package org.videolan.libvlc.interfaces;
import androidx.annotation.Nullable;

/* loaded from: classes.jar:org/videolan/libvlc/interfaces/AbstractVLCEvent.class */
public abstract class AbstractVLCEvent {
    public final int type;
    protected final long arg1;
    protected final long arg2;
    protected final float argf1;
    protected final String args1;

    /* loaded from: classes.jar:org/videolan/libvlc/interfaces/AbstractVLCEvent$Listener.class */
    public interface Listener<T extends AbstractVLCEvent> {
        void onEvent(T t);
    }

    public AbstractVLCEvent(int type) {
        this.type = type;
        this.arg2 = 0L;
        this.arg1 = 0L;
        this.argf1 = 0.0f;
        this.args1 = null;
    }

    public AbstractVLCEvent(int type, long arg1) {
        this.type = type;
        this.arg1 = arg1;
        this.arg2 = 0L;
        this.argf1 = 0.0f;
        this.args1 = null;
    }

    public AbstractVLCEvent(int type, long arg1, long arg2) {
        this.type = type;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.argf1 = 0.0f;
        this.args1 = null;
    }

    public AbstractVLCEvent(int type, float argf) {
        this.type = type;
        this.arg2 = 0L;
        this.arg1 = 0L;
        this.argf1 = argf;
        this.args1 = null;
    }

    public AbstractVLCEvent(int type, long arg1, @Nullable String args1) {
        this.type = type;
        this.arg1 = arg1;
        this.arg2 = 0L;
        this.argf1 = 0.0f;
        this.args1 = args1;
    }

    public void release() {
    }
}
