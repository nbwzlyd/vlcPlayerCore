package org.videolan.libvlc.util;

import java.util.Arrays;
import java.util.HashSet;

/* loaded from: classes.jar:org/videolan/libvlc/util/Extensions.class */
public class Extensions {
    public static final HashSet<String> VIDEO = new HashSet<>();
    public static final HashSet<String> AUDIO = new HashSet<>();
    public static final HashSet<String> SUBTITLES = new HashSet<>();
    public static final HashSet<String> PLAYLIST = new HashSet<>();

    static {
        String[] videoExtensions = {".3g2", ".3gp", ".3gp2", ".3gpp", ".amv", ".asf", ".avi", ".bik", ".divx", ".drc", ".dv", ".f4v", ".flv", ".gvi", ".gxf", ".h264", ".ismv", ".iso", ".m1v", ".m2v", ".m2t", ".m2ts", ".m4v", ".mkv", ".mov", ".mp2", ".mp2v", ".mp4", ".mp4v", ".mpe", ".mpeg", ".mpeg1", ".mpeg2", ".mpeg4", ".mpg", ".mpv2", ".mts", ".mtv", ".mxf", ".mxg", ".nsv", ".nut", ".nuv", ".ogm", ".ogv", ".ogx", ".ps", ".rec", ".rm", ".rmvb", ".rpl", ".thp", ".tod", ".ts", ".tts", ".vob", ".vro", ".webm", ".wm", ".wmv", ".wtv", ".xesc"};
        String[] audioExtensions = {".3ga", ".669", ".a52", ".aac", ".ac3", ".adt", ".adts", ".aif", ".aifc", ".aiff", ".alac", ".amr", ".aob", ".ape", ".au", ".awb", ".caf", ".dts", ".flac", ".it", ".m4a", ".m4b", ".m4p", ".mid", ".mka", ".mlp", ".mod", ".mpa", ".mp1", ".mp2", ".mp3", ".mpc", ".mpga", ".oga", ".ogg", ".oma", ".opus", ".qcp", ".ra", ".ram", ".rmi", ".s3m", ".snd", ".spx", ".tta", ".voc", ".vqf", ".w64", ".wav", ".wma", ".wv", ".xa", ".xm"};
        String[] subtitlesExtensions = {".idx", ".sub", ".srt", ".ssa", ".ass", ".smi", ".utf", ".utf8", ".utf-8", ".rt", ".aqt", ".txt", ".usf", ".jss", ".cdg", ".psb", ".mpsub", ".mpl2", ".pjs", ".dks", ".stl", ".vtt", ".ttml", ".mks"};
        String[] playlistExtensions = {".m3u", ".asx", ".b4s", ".pls", ".xspf", ".wpl"};
        VIDEO.addAll(Arrays.asList(videoExtensions));
        AUDIO.addAll(Arrays.asList(audioExtensions));
        SUBTITLES.addAll(Arrays.asList(subtitlesExtensions));
        PLAYLIST.addAll(Arrays.asList(playlistExtensions));
    }
}
