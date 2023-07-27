package com.icatchtek.nadk.show.imageloader;

import com.icatchtek.baseutil.entity.PushMessage;

import java.util.Locale;

/**
 * Created by sha.liu on 2018/4/12.
 */

public enum ImageUri {
    HTTP("http"), HTTPS("https"), FILE("file"), CONTENT("content"), ASSETS("assets"), DRAWABLE("drawable"),
    DATABASE("database"), TUTK("tutk"), MSGFILE("msgfile"), DEVICEMSGFILE("devicemsgfile"), UNKNOWN("");


    private String scheme;
    private String uriPrefix;

    ImageUri(String scheme) {
        this.scheme = scheme;
        uriPrefix = scheme + "://";
    }

    /**
     * Defines ImageUri of incoming URI
     *
     * @param uri URI for scheme detection
     * @return ImageUri of incoming URI
     */
    public static ImageUri ofUri(String uri) {
        if (uri != null) {
            for (ImageUri s : values()) {
                if (s.belongsTo(uri)) {
                    return s;
                }
            }
        }
        return UNKNOWN;
    }

    /** Appends scheme to incoming path */
    public String getUri(String path) {
        return uriPrefix + path;
    }

    public String getUri(String uid, int fileHandle, int thumbSize) {
        if (this == TUTK || this == DATABASE) {
            return getUri(converFileInfo(uid, fileHandle, thumbSize));
        }
        throw new IllegalArgumentException(String.format("this method doesn't support ImageUri [%1$s], only support ImageUri [%2$s] & [%3$s]", this.toString(), TUTK.toString(), DATABASE.toString()));

    }

    public String getUri(String uid, int msgid) {
        if (this == MSGFILE) {
            return getUri(converMsgInfo(uid, msgid));
        }
        throw new IllegalArgumentException(String.format("this method doesn't support ImageUri [%1$s], only support ImageUri [%2$s]", this.toString(), MSGFILE.toString()));

    }

    public String getUri(String uid, long timeInSecs) {
        if (this == DEVICEMSGFILE) {
            return getUri(converDeviceMsgInfo(uid, timeInSecs));
        }
        throw new IllegalArgumentException(String.format("this method doesn't support ImageUri [%1$s], only support ImageUri [%2$s]", this.toString(), DEVICEMSGFILE.toString()));

    }

    public String getScheme() {
        return scheme;
    }

    public String getUriPrefix() {
        return uriPrefix;
    }

    /** Removed scheme part ("scheme://") from incoming URI */
    public String crop(String uri) {
        if (!belongsTo(uri)) {
            throw new IllegalArgumentException(String.format("URI [%1$s] doesn't have expected scheme [%2$s]", uri, scheme));
        }
        return uri.substring(uriPrefix.length());
    }





    public PushMessage getDeviceMsgOfUri(String uri) {
        if (this == DEVICEMSGFILE) {
            String msgInfoStr = crop(uri);
            return parseDeviceMsgInfoStr(msgInfoStr);
        }

        return null;
    }

    private String converFileInfo(String uid, int fileHandle, int thumbSize) {
        String fileInfoStr = "/uid=" + uid + "/fileHandle=" + fileHandle + "/thumbSize=" + thumbSize + "/thumb_" + fileHandle + ".jpg";
        return fileInfoStr;
    }

    private String converMsgInfo(String uid, int msgid) {
        String msgInfoStr = "/uid=" + uid + "/msgid=" + msgid + "/thumb_" + msgid + ".jpg";
        return msgInfoStr;
    }

    private String converDeviceMsgInfo(String uid, long timeInSecs) {
        String msgInfoStr = "/uid=" + uid + "/timeInSecs=" + timeInSecs +"/thumb_" + timeInSecs + ".jpg";
        return msgInfoStr;
    }



    private int getMsgid(String msgInfoStr) {
        int msgid = -1;

        String fileAttrs[] = msgInfoStr.split("/");
        for (String fileAttr : fileAttrs) {
            String keyVal[] = fileAttr.split("=");
            if (keyVal.length != 2) {
                continue;
            }

            if (keyVal[0].equals("msgid")) {
                msgid = Integer.parseInt(keyVal[1]);
                break;
            }
        }

        return msgid;

    }

    private String getMsgUid(String msgInfoStr) {
        String uid = "";

        String fileAttrs[] = msgInfoStr.split("/");
        for (String fileAttr : fileAttrs) {
            String keyVal[] = fileAttr.split("=");
            if (keyVal.length != 2) {
                continue;
            }

            if (keyVal[0].equals("uid")) {
                uid = keyVal[1];
                break;
            }
        }

        return uid;

    }

    private PushMessage parseDeviceMsgInfoStr(String deviceMsgInfoStr) {
        String uid = "";
        long timeInSecs = 0;

        String fileAttrs[] = deviceMsgInfoStr.split("/");
        for (String fileAttr : fileAttrs) {
            String keyVal[] = fileAttr.split("=");
            if (keyVal.length != 2) {
                continue;
            }

            if (keyVal[0].equals("uid")) {
                uid = keyVal[1];
            } else if (keyVal[0].equals("timeInSecs")){
                timeInSecs = Long.valueOf(keyVal[1]);
            }
        }
        PushMessage msg = new PushMessage();
        msg.timeInSecs = timeInSecs;
        msg.devID = uid;
        return msg;

    }




    private boolean belongsTo(String uri) {
        return uri.toLowerCase(Locale.US).startsWith(uriPrefix);
    }
}
