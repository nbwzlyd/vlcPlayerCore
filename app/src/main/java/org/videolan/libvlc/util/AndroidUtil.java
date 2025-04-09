package org.videolan.libvlc.util;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;
import java.io.File;

/* loaded from: classes.jar:org/videolan/libvlc/util/AndroidUtil.class */
public class AndroidUtil {
    public static final boolean isROrLater;
    public static final boolean isPOrLater;
    public static final boolean isOOrLater;
    public static final boolean isNougatMR1OrLater;
    public static final boolean isNougatOrLater;
    public static final boolean isMarshMallowOrLater;

    static {
        isROrLater = Build.VERSION.SDK_INT >= 30;
        isPOrLater = Build.VERSION.SDK_INT >= 28;
        isOOrLater = isPOrLater || Build.VERSION.SDK_INT >= 26;
        isNougatMR1OrLater = isOOrLater || Build.VERSION.SDK_INT >= 25;
        isNougatOrLater = isNougatMR1OrLater || Build.VERSION.SDK_INT >= 24;
        isMarshMallowOrLater = isNougatOrLater || Build.VERSION.SDK_INT >= 23;
    }

    public static File UriToFile(Uri uri) {
        return new File(uri.getPath().replaceFirst("file://", ""));
    }

    public static Uri PathToUri(String path) {
        return Uri.fromFile(new File(path));
    }

    public static Uri LocationToUri(String location) {
        Uri uri = Uri.parse(location);
        if (uri.getScheme() == null) {
            throw new IllegalArgumentException("location has no scheme");
        }
        return uri;
    }

    public static Uri FileToUri(File file) {
        return Uri.fromFile(file);
    }

    public static Activity resolveActivity(Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        }
        if (!(context instanceof ContextWrapper)) {
            return null;
        }
        return resolveActivity(((ContextWrapper) context).getBaseContext());
    }
}
