package com.icatchtek.nadk.show.kvsarchivedmedia;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.services.kinesisvideo.AWSKinesisVideoClient;
import com.amazonaws.services.kinesisvideo.model.APIName;
import com.amazonaws.services.kinesisvideo.model.GetDataEndpointRequest;
import com.amazonaws.services.kinesisvideo.model.GetDataEndpointResult;
import com.amazonaws.services.kinesisvideoarchivedmedia.model.GetHLSStreamingSessionURLRequest;
import com.amazonaws.services.kinesisvideoarchivedmedia.model.GetHLSStreamingSessionURLResult;
import com.amazonaws.services.kinesisvideoarchivedmedia.model.HLSFragmentSelector;
import com.amazonaws.services.kinesisvideoarchivedmedia.model.HLSFragmentSelectorType;
import com.amazonaws.services.kinesisvideoarchivedmedia.model.HLSTimestampRange;
import com.amazonaws.services.kinesisvideoarchivedmedia.model.PlaybackMode;
import com.icatchtek.baseutil.log.AppLog;
import com.icatchtek.nadk.show.kvsarchivedmedialibextend.AWSKinesisVideoArchivedMediaClientExtend;
import com.icatchtek.nadk.show.kvsarchivedmedialibextend.ClipFragmentSelector;
import com.icatchtek.nadk.show.kvsarchivedmedialibextend.ClipFragmentSelectorType;
import com.icatchtek.nadk.show.kvsarchivedmedialibextend.ClipTimestampRange;
import com.icatchtek.nadk.show.kvsarchivedmedialibextend.DASHFragmentSelector;
import com.icatchtek.nadk.show.kvsarchivedmedialibextend.DASHFragmentSelectorType;
import com.icatchtek.nadk.show.kvsarchivedmedialibextend.DASHTimestampRange;
import com.icatchtek.nadk.show.kvsarchivedmedialibextend.DownloadingProgressListen;
import com.icatchtek.nadk.show.kvsarchivedmedialibextend.GetClipRequest;
import com.icatchtek.nadk.show.kvsarchivedmedialibextend.GetClipResult;
import com.icatchtek.nadk.show.kvsarchivedmedialibextend.GetDASHStreamingSessionURLRequest;
import com.icatchtek.nadk.show.kvsarchivedmedialibextend.GetDASHStreamingSessionURLResult;
import com.icatchtek.nadk.webrtc.assist.NADKAuthorization;

import java.util.Date;

/**
 * Created by sha.liu on 2021/1/6.
 */
public class KVSArchivedMediaClient {
    private static final String TAG = KVSArchivedMediaClient.class.getSimpleName();
    private String channelName;
    private String region;
    private String accessKey;
    private String secretKey;
    private AWSKinesisVideoArchivedMediaClientExtend kvsArchivedMediaClient = null;
    private AWSCredentialsProvider provider;
    private String dataEndpoint;

    public KVSArchivedMediaClient(NADKAuthorization authorization) {
        channelName = authorization.getChannelName();
        region = authorization.getRegion();
        accessKey = authorization.getAccessKey();
        secretKey = authorization.getSecretKey();
        provider = new AWSCredentialsProvider() {
            @Override
            public AWSCredentials getCredentials() {
                return new AWSCredentials() {
                    @Override
                    public String getAWSAccessKeyId() {
                        return accessKey;
                    }

                    @Override
                    public String getAWSSecretKey() {
                        return secretKey;
                    }
                };
            }

            @Override
            public void refresh() {

            }
        };

    }

    synchronized private void init() {

        if (dataEndpoint == null || dataEndpoint.isEmpty()) {
            final AWSKinesisVideoClient kinesisVideoClient = new AWSKinesisVideoClient(provider);
            kinesisVideoClient.setRegion(Region.getRegion(region));
            kinesisVideoClient.setSignerRegionOverride(region);
            kinesisVideoClient.setServiceNameIntern("kinesisvideo");

            GetDataEndpointResult dataEndpointResult = kinesisVideoClient.getDataEndpoint(new GetDataEndpointRequest()
                    .withStreamName(channelName)
                    .withAPIName(APIName.GET_DASH_STREAMING_SESSION_URL));

            dataEndpoint = dataEndpointResult.getDataEndpoint();
            AppLog.d(TAG, "getDataEndpoint: " + dataEndpoint);
        } else {
            AppLog.d(TAG, "use cached dataEndpoint: " + dataEndpoint);
        }

        if (dataEndpoint != null) {
            kvsArchivedMediaClient =  new AWSKinesisVideoArchivedMediaClientExtend(provider);
            kvsArchivedMediaClient.setEndpoint(dataEndpoint);
            kvsArchivedMediaClient.setServiceNameIntern("kinesisvideo");
        }

    }



    public String getDashUrl(Date startTime, Date endTime) {
        try {
            if (kvsArchivedMediaClient == null) {
                init();
            }
            final GetDASHStreamingSessionURLResult dashStreamURL = kvsArchivedMediaClient.getDASHStreamingSessionURL(
                    new GetDASHStreamingSessionURLRequest()
                            .withStreamName(channelName)
                            .withPlaybackMode(PlaybackMode.ON_DEMAND)
                            .withDASHFragmentSelector(new DASHFragmentSelector()
                                    .withFragmentSelectorType(DASHFragmentSelectorType.PRODUCER_TIMESTAMP)
                                    .withTimestampRange(new DASHTimestampRange()
                                            .withStartTimestamp(startTime)
                                            .withEndTimestamp(endTime)))
            );

            AppLog.d(TAG, "getDASHStreamingSessionURL: " + dashStreamURL);

            if (dashStreamURL != null) {
                return dashStreamURL.getDASHStreamingSessionURL();
            } else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            AppLog.e(TAG, "getDASHStreamingSessionURL Exception = " + e.getClass().getSimpleName() + ", " + e.getMessage());
            return null;
        }

    }


    public String getHLSUrl(Date startTime, Date endTime) {
        try {
            if (kvsArchivedMediaClient == null) {
                init();
            }
            final GetHLSStreamingSessionURLResult hlsStreamURL = kvsArchivedMediaClient.getHLSStreamingSessionURL(
                    new GetHLSStreamingSessionURLRequest()
                            .withStreamName(channelName)
                            .withPlaybackMode(PlaybackMode.ON_DEMAND)
                            .withHLSFragmentSelector(new HLSFragmentSelector()
                                    .withFragmentSelectorType(HLSFragmentSelectorType.PRODUCER_TIMESTAMP)
                                    .withTimestampRange(new HLSTimestampRange()
                                            .withStartTimestamp(startTime)
                                            .withEndTimestamp(endTime)))
            );

            AppLog.d(TAG, "getHLSStreamingSessionURL: " + hlsStreamURL);

            if (hlsStreamURL != null) {
                return hlsStreamURL.getHLSStreamingSessionURL();
            } else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            AppLog.e(TAG, "getHLSStreamingSessionURL Exception = " + e.getClass().getSimpleName() + ", " + e.getMessage());
            return null;
        }

    }

    public void getClip(Date startTime, Date endTime, String path, DownloadingProgressListen downloadingProgressListen) {
        try {
            if (kvsArchivedMediaClient == null) {
                init();
            }
            final GetClipResult clipResult = kvsArchivedMediaClient.getClip(new GetClipRequest()
                    .withStreamName(channelName)
                    .withClipFragmentSelector(new ClipFragmentSelector()
                            .withFragmentSelectorType(ClipFragmentSelectorType.PRODUCER_TIMESTAMP)
                            .withTimestampRange(new ClipTimestampRange()
                                    .withStartTimestamp(startTime)
                                    .withEndTimestamp(endTime))), path, downloadingProgressListen);

            AppLog.d(TAG, "getClip: " + clipResult);
        } catch (Exception e) {
            e.printStackTrace();
            AppLog.e(TAG, "getClip Exception = " + e.getClass().getSimpleName() + ", " + e.getMessage());
            throw e;
        }
    }
}
