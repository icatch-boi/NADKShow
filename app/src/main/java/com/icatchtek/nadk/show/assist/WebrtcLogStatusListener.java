package com.icatchtek.nadk.show.assist;

import java.util.Locale;

import com.icatchtek.nadk.reliant.NADKLogger;
import com.icatchtek.nadk.webrtc.NADKWebrtcActiveClientListener;

public class WebrtcLogStatusListener implements NADKWebrtcActiveClientListener
{
    private final NADKLogger    logger;
    private final String        deviceID;
    private final String        applicationID;

    public WebrtcLogStatusListener(NADKLogger logger, String deviceID, String applicationID)
    {
        this.logger = logger;
        this.deviceID = deviceID;
        this.applicationID = applicationID;
    }

    @Override
    public void noActiveClientExists()
    {
        logger.writeCommonLogI("statusListener", "NoActiveClientExists");

        /* update log first */
        String tid = logger.getUniqueID();
        String relFileName = logger.getRelativeFileName();
        String absFileName = logger.getAbsoluteFileName();

          /* switch a new file, upload the old file */
//        logger.setFileLog(false);
//        logger.setDeviceID(deviceID);
//        logger.setApplication(applicationID);
//        logger.setFileLog(true);

        /* TODO: uploading */
        //uploadLogToServer(tid, absFileName, relFileName);
        logger.writeCommonLogI("no_active_sessions", String.format(Locale.getDefault(),
            "BpSC| Please upload log, tid: %s, absFileName: %s, relFileName: %s\n", tid, absFileName, relFileName));

        logger.writeCommonLogI("no_active_sessions", String.format(Locale.getDefault(),
                "BpSC| no active sessions exist in NADK, deviceID: %s, applicationID: %s\n", deviceID, applicationID));
    }
}
