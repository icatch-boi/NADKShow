package com.icatchtek.nadk.show;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.MimeTypes;
import com.icatch.smarthome.am.utils.DateUtil;
import com.icatchtek.basecomponent.prompt.MyProgressDialog;
import com.icatchtek.basecomponent.prompt.MyToast;
import com.icatchtek.basecomponent.prompt.PercentageProgressDialog;
import com.icatchtek.basecomponent.utils.ClickUtils;
import com.icatchtek.baseutil.ThreadPoolUtils;
import com.icatchtek.baseutil.device.MyOrientationEventListener;
import com.icatchtek.baseutil.device.ScreenUtils;
import com.icatchtek.baseutil.imageloader.ImageLoaderConfig;
import com.icatchtek.baseutil.info.SystemInfo;
import com.icatchtek.baseutil.log.AppLog;
import com.icatchtek.nadk.playback.NADKPlayback;
import com.icatchtek.nadk.playback.NADKPlaybackAssist;
import com.icatchtek.nadk.playback.NADKPlaybackClient;
import com.icatchtek.nadk.playback.NADKPlaybackClientListener;
import com.icatchtek.nadk.playback.file.NADKFileStatusListener;
import com.icatchtek.nadk.playback.file.NADKFileTransferListener;
import com.icatchtek.nadk.playback.type.NADKDateTime;
import com.icatchtek.nadk.playback.type.NADKFileAvailableInfo;
import com.icatchtek.nadk.playback.type.NADKMediaFile;
import com.icatchtek.nadk.reliant.NADKException;
import com.icatchtek.nadk.reliant.NADKSignalingType;
import com.icatchtek.nadk.reliant.NADKWebrtcAuthentication;
import com.icatchtek.nadk.reliant.NADKWebrtcSetupInfo;
import com.icatchtek.nadk.reliant.parameter.NADKWebrtcStreamParameter;
import com.icatchtek.nadk.show.assist.WebrtcLogStatusListener;
import com.icatchtek.nadk.show.device.DeviceManager;
import com.icatchtek.nadk.show.device.NADKLocalDevice;
import com.icatchtek.nadk.show.imageloader.CustomImageDownloader;
import com.icatchtek.nadk.show.sdk.DeviceLocalFileListInfo;
import com.icatchtek.nadk.show.sdk.FileDownloadStatusListener;
import com.icatchtek.nadk.show.sdk.NADKPlaybackClientService;
import com.icatchtek.nadk.show.utils.NADKConfig;
import com.icatchtek.nadk.show.wakeup.WakeUpThread;
import com.icatchtek.nadk.webrtc.NADKWebrtc;
import com.icatchtek.nadk.webrtc.NADKWebrtcClient;
import com.icatchtek.nadk.webrtc.NADKWebrtcClientStatusListener;
import com.icatchtek.nadk.webrtc.NADKWebrtcControl;
import com.icatchtek.nadk.webrtc.assist.NADKAuthorization;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.tinyai.libmediacomponent.components.filelist.FileItemInfo;
import com.tinyai.libmediacomponent.components.filelist.FileListView2;
import com.tinyai.libmediacomponent.components.filelist.RefreshMode;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class LocalPlaybackActivity extends NADKShowBaseActivity {
    private static final String TAG = LocalPlaybackActivity.class.getSimpleName();
    private Handler handler = new Handler();


    private RelativeLayout video_view_layout;
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    private ImageButton back_btn;
    private ImageButton fullscreen_switch;
    private RelativeLayout topbar;
    private ImageView topbar_back;
    private TextView topbar_title;
    private boolean orientationIsVertical = true;
    private PlaybackStateListener playbackStateListener;
    private MyOrientationEventListener orientationEventListener;

    private RelativeLayout play_info_layout;
    private RelativeLayout top_bar_layout;
    private TextView top_bar_title;

    private RelativeLayout remote_connect_status_layout;
    private ProgressBar remote_connect_status_progress_bar;
    private ImageView remote_connect_status_imv;
    private TextView remote_connect_status_txv;
    private TextView remote_connect_reconnect_txv;
    private TextView remote_connect_status_disable;
    private LinearLayout remote_connect_status_info_layout;
    private int connectionStatusResId = R.string.text_connected;


//    private static final String MEDIA_PATH = "/storage/self/primary/NADKWebrtcResources/media";
//    private static final String CACHE_PATH = "/storage/self/primary/NADKWebrtcResources/media/cache";
    private static final String STORAGE_PATH = "/storage/self/primary/Download/NADKShow";
    private static final String MEDIA_PATH = STORAGE_PATH;
    private static final String CACHE_PATH = STORAGE_PATH;
    private static final String LOCAL_FILE_PREFIX = "file://";
    private String cachePath;

    private boolean masterRole = false;
    private NADKWebrtc webrtc;
    private NADKPlayback playback;
    private NADKWebrtcClient webrtcClient;
    private NADKWebrtcControl webrtcControl;
    private NADKWebrtcStreamParameter streamParameter;
    private NADKPlaybackClientService playbackClientService;
    private int signalingType;


    private FileListView2 fileListView;
    private List<FileItemInfo> fileItemInfoList = new LinkedList<>();
    private DeviceLocalFileListInfo localFileListInfo = null;
    private NADKPlaybackClient playbackClient;

    private PercentageProgressDialog dialog;

    private WakeUpThread wakeUpThread;
    private boolean isFromPV = false;
    private NADKLocalDevice nadkLocalDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_playback);
        back_btn = findViewById(R.id.back_btn);
        top_bar_layout = findViewById(R.id.toolbar_layout);
        play_info_layout = findViewById(R.id.play_info_layout);
        top_bar_title = findViewById(R.id.tool_bar_title);


        video_view_layout = findViewById(R.id.video_view_layout);
        playerView = findViewById(R.id.player_view);
        fullscreen_switch = findViewById(R.id.exo_fullscreen);
        topbar = findViewById(R.id.exo_top_bar);
        topbar_back = findViewById(R.id.exo_top_bar_back);
        topbar_title = findViewById(R.id.exo_top_bar_title);

        Intent intent = getIntent();
        signalingType = intent.getIntExtra("signalingType", NADKSignalingType.NADK_SIGNALING_TYPE_BASE_TCP);
        isFromPV = intent.getBooleanExtra("isFromPV", false);

        initConnectionStatusUI();
        initFileList();
        initActivityCfg();

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        fullscreen_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ClickUtils.isFastDoubleClick(v)){
                    AppLog.i(TAG, "isFastDoubleClick the v.id=" + v.getId());
                    return;
                }
                if (!orientationIsVertical) {
                    setPvLayout(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    setPvLayout(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
        });

        topbar_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ClickUtils.isFastDoubleClick(v)){
                    AppLog.i(TAG, "isFastDoubleClick the v.id=" + v.getId());
                    return;
                }
                if (!orientationIsVertical) {
                    setPvLayout(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        });

        orientationEventListener = new MyOrientationEventListener(this, new MyOrientationEventListener.OnOrientationChangedCallback() {
            @Override
            public void onOrientationChanged(int orientation) {
                setPvLayout(orientation);
            }
        });

        if (orientationEventListener != null) {
            orientationEventListener.enable();
        }


        setPvLayout(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initializePlayer();


        cachePath = getExternalCacheDir().toString() + "/NADK";
        createDirectory(cachePath);
        createDirectory(cachePath);

        if (!isFromPV) {
            initWebrtc();
            ThreadPoolUtils.getInstance().executorNetThread(new Runnable() {
                @Override
                public void run() {
                    connect();
                }
            }, 200);
        } else {

            nadkLocalDevice = DeviceManager.getInstance().getDevice(NADKConfig.getInstance().getLanModeAuthorization().getAccessKey());
            playbackClient = nadkLocalDevice.getPlaybackClient();

            ThreadPoolUtils.getInstance().executorNetThread(new Runnable() {
                @Override
                public void run() {
                    initPlayback();
                }
            }, 200);

        }

    }

    @Override
    public void onStop() {
        super.onStop();
        pauseVideo();
    }


    @Override
    public void onDestroy() {
        if (orientationEventListener != null) {
            orientationEventListener.disable();
        }
        disconnect();

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_HOME:
                AppLog.d(TAG, "home");
                pauseVideo();
                break;
            case KeyEvent.KEYCODE_BACK:
                AppLog.d(TAG, "back");
                if (!orientationIsVertical) {
                    setPvLayout(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    finish();
                }
                break;
        }
        return true;
    }

    private void initConnectionStatusUI() {
        remote_connect_status_layout = findViewById(R.id.remote_connect_status_layout);
        remote_connect_status_progress_bar = findViewById(R.id.remote_connect_status_progress_bar);
        remote_connect_status_imv = findViewById(R.id.remote_connect_status_imv);
        remote_connect_status_txv = findViewById(R.id.remote_connect_status_txv);
        remote_connect_reconnect_txv = findViewById(R.id.remote_connect_reconnect_txv);
        remote_connect_status_disable = findViewById(R.id.remote_connect_status_disable);
        remote_connect_status_info_layout = findViewById(R.id.remote_connect_status_info_layout);

        remote_connect_status_info_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ClickUtils.isFastDoubleClick(v)){
                    AppLog.i(TAG, "isFastDoubleClick the v.id=" + v.getId());
                    return;
                }
                destroyWebrtc();
                connect();

            }
        });

        remote_connect_status_disable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ClickUtils.isFastDoubleClick(v)){
                    AppLog.i(TAG, "isFastDoubleClick the v.id=" + v.getId());
                    return;
                }
                remote_connect_status_layout.setVisibility(View.GONE);
            }
        });
    }


    private void setConnectionStatus(int resId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (resId == R.string.text_connecting || resId == R.string.loading) {
                    remote_connect_status_layout.setVisibility(View.VISIBLE);
                    remote_connect_status_progress_bar.setVisibility(View.VISIBLE);
                    remote_connect_status_imv.setVisibility(View.GONE);
                    remote_connect_status_info_layout.setEnabled(false);
                    remote_connect_reconnect_txv.setVisibility(View.GONE);
                    remote_connect_status_txv.setText(resId);
                } else if (resId == R.string.text_disconnected){
                    remote_connect_status_layout.setVisibility(View.VISIBLE);
                    remote_connect_status_progress_bar.setVisibility(View.GONE);
                    remote_connect_status_imv.setVisibility(View.VISIBLE);
                    remote_connect_status_info_layout.setEnabled(true);
                    remote_connect_reconnect_txv.setVisibility(View.VISIBLE);
                    remote_connect_reconnect_txv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
                    remote_connect_reconnect_txv.getPaint().setAntiAlias(true);
                    remote_connect_status_txv.setText(getString(resId) + ",");
                    MyProgressDialog.closeProgressDialog();
                } else {
                    remote_connect_status_layout.setVisibility(View.GONE);
                    remote_connect_status_info_layout.setEnabled(false);
                    remote_connect_reconnect_txv.setVisibility(View.GONE);
                    remote_connect_status_txv.setText(resId);
                }

                connectionStatusResId = resId;

            }
        });

    }

    private void initActivityCfg() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        if (isLocked) {
//            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
//        } else {
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        }
    }

    private void initFileList() {
        fileListView = findViewById(R.id.file_list_view);
//        fileListView.changeLayoutType(PhotoWallLayoutType.PREVIEW_TYPE_LIST);


        fileListView.setRefreshListener(new FileListView2.RefreshCallback() {
            @Override
            public List<FileItemInfo> refreshData() {
                //模拟获取最新数据逻辑
                if (localFileListInfo != null) {
                    try {
                        return localFileListInfo.pullDownToRefresh();
                    } catch (NADKException e) {
                        e.printStackTrace();
                    }
                }
                return new LinkedList<>();
            }

            @Override
            public List<FileItemInfo> getModeData() {
                //模拟获取下一页数据逻辑
                if (localFileListInfo != null) {
                    try {
                        return localFileListInfo.pullUpToRefresh();
                    } catch (NADKException e) {
                        e.printStackTrace();
                    }
                }
                return new LinkedList<>();
            }

        });

        fileListView.setItemContentClickListener(new FileListView2.ItemContentClickListener() {

            @Override
            public void itemOnClick(FileItemInfo info) {
                play(info);
            }

            @Override
            public void itemOnLongClick(FileItemInfo info) {
//                fileListView.enterEditMode(position);
            }

            @Override
            public void downloadOnClick(FileItemInfo info) {
                //下载单个文件

                //下载成功后标记下载状态，fileListView自动更新UI
//                ThreadPoolUtils.getInstance().executorNetThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (localFileListInfo != null) {
//                            String path = localFileListInfo.downloadMediaFile(DeviceLocalFileListInfo.convertToNADKMediaFile(info), new FileDownloadStatusListener());
//                            if (path != null) {
//                                handler.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        fileListView.markDownloaded(info);
//                                    }
//                                });
//                            }
//                        }
//                    }
//                }, 200);
            }

            @Override
            public void deleteOnClick(FileItemInfo info) {
                //删除单个文件

                //删除成功后标记下载状态，fileListView自动更新UI


            }
        });

        fileListView.setEditBtnClickListener(new FileListView2.EditBtnClickListener() {
            @Override
            public void downloadOnClick(List<FileItemInfo> list) {
                //批量下载文件
                AppLog.d(TAG,"downloadOnClick list:" + list);
            }

            @Override
            public void deleteOnClick(List<FileItemInfo> list, FileListView2.DeleteResponse deleteResponse) {
                AppLog.d(TAG,"deleteOnClick list:" + list);
                if(list!=null){
                    for (FileItemInfo temp:list
                    ) {
                        AppLog.d(TAG,"deleteOnClick temp:" + temp.getFileName());
                    }
                }
//                fileListView.exitEditMode();
                if(deleteResponse != null){
                    deleteResponse.onComplete(true,list);
                }
            }

        });

        fileListView.setRefreshMode(RefreshMode.BOTH);
        fileListView.enableDownload(false);

    }


    private void initializePlayer() {
        playbackStateListener = new PlaybackStateListener();
        if (player == null) {

            DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
            trackSelector.setParameters(
                    trackSelector.buildUponParameters().setMaxVideoSizeSd());
            player = new SimpleExoPlayer.Builder(this)
                    .setTrackSelector(trackSelector)
                    .build();

            player.addAnalyticsListener(new EventLogger(trackSelector, "ExoPlayerDebug"));

            playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);
            playerView.setShowNextButton(false);
            playerView.setShowPreviousButton(false);
            playerView.setShowRewindButton(false);
            playerView.setShowFastForwardButton(false);

            player.addListener(playbackStateListener);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    player.setPlayWhenReady(playWhenReady);
                    playerView.setPlayer(player);
                }
            });
        }


    }

    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.removeListener(playbackStateListener);
            player.release();
            player = null;
        }
    }

    private void play(FileItemInfo itemInfo) {
        ThreadPoolUtils.getInstance().executorNetThread(new Runnable() {
            @Override
            public void run() {

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyProgressDialog.showProgressDialog(LocalPlaybackActivity.this);
                        if (player.isPlaying() || player.isLoading()) {
                            player.stop();
                        }
                        topbar_title.setText(itemInfo.getFileName());
                    }
                });

                String url = getUrl(itemInfo);

                if (url != null && !url.isEmpty()) {
                    player(url);
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            MyProgressDialog.closeProgressDialog();
                            MyToast.show(LocalPlaybackActivity.this, "play failed");
                            top_bar_title.setText("Playback Failed");
                        }
                    });
                }

            }
        }, 200);

    }

    private void pauseVideo() {
        player.pause();
    }

    private String getUrl(FileItemInfo itemInfo) {
        return downloadFile(itemInfo);
    }

    private String downloadFile(FileItemInfo itemInfo) {
        String dstFile = cachePath + "/" + DateUtil.timeFormatFileNameString(itemInfo.getTime()) + ".mp4";
        File file = new File(dstFile);
        if (file.exists()) {
            AppLog.d(TAG, dstFile + " is already exist");
            return LOCAL_FILE_PREFIX + dstFile;
        }
        if (localFileListInfo != null) {
//            showDownloadDialog();

            FileDownloadStatusListener fileDownloadStatusListener = new FileDownloadStatusListener(new NADKFileTransferListener() {
                private long fileSize;
                private String fileName;

                @Override
                public void transferStarted(String fileName, long fileSize) {
                    this.fileSize = fileSize;
                    this.fileName = fileName;
                    setMaxProgressForDownloadDialog(fileSize);

                }

                @Override
                public void transferFinished(long transferedSize) {
                    if (fileSize == transferedSize) {
                        if (fileName != null && !fileName.isEmpty()) {
//                            File srcFile = new File(fileName);
//                            createDirectory(STORAGE_PATH);
//                            FileUtil.copy(fileName, dstFile);
//                            srcFile.renameTo(new File(dstFile));
                        }
                    }

                }

                @Override
                public void transferInformation(long transferedSize) {
                    updateDownloadDialog(transferedSize);

                }


            });

//            ThreadPoolUtils.getInstance().executorNetThread(new Runnable() {
//                @Override
//                public void run() {
//
//
//                    String filePath = localFileListInfo.downloadMediaFile(DeviceLocalFileListInfo.convertToNADKMediaFile(itemInfo), (NADKFileTransferListener) fileDownloadStatusListener);
//                    dismissDownloadDialog();
//
//                }
//            }, 200);
//            return LOCAL_FILE_PREFIX + fileDownloadStatusListener.getFileName();

            String filePath = localFileListInfo.downloadMediaFile(DeviceLocalFileListInfo.convertToNADKMediaFile(itemInfo), (NADKFileTransferListener) fileDownloadStatusListener);
            dismissDownloadDialog();
            if (filePath != null && !filePath.isEmpty()) {
                File srcFile = new File(filePath);
                srcFile.renameTo(new File(dstFile));
                return LOCAL_FILE_PREFIX + dstFile;
            } else {
                return "";
            }


        }
        return "";
    }

    private void showDownloadDialog() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                dialog = new PercentageProgressDialog(LocalPlaybackActivity.this, R.style.base_common_dialog_style, getString(R.string.text_downloading));
                dialog.show();
            }
        });
    }

    private void dismissDownloadDialog() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
    }

    private void setMaxProgressForDownloadDialog(long fileSize) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (dialog != null && dialog.isShowing()) {
                    dialog.setMaxProgress(fileSize);
                }
            }
        });

    }

    private void updateDownloadDialog(long transferedSize) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (dialog != null && dialog.isShowing()) {
                    dialog.setCurrentProgress(transferedSize);
                }
            }
        });

    }

    private void player(String url) {
        String mimeType = MimeTypes.APPLICATION_MP4;
        if (url.endsWith(".mp4") || url.endsWith(".MP4")) {
            mimeType = MimeTypes.APPLICATION_MP4;
//        } else if (currentProtocol.equals("HLS")) {
//            mimeType = MimeTypes.APPLICATION_M3U8;
//        } else if (currentProtocol.equals("DASH")) {
//            mimeType = MimeTypes.APPLICATION_MPD;
        }

        player(url, mimeType);
    }

    private void player(String url, String type) {
        handler.post(new Runnable() {
            @Override
            public void run() {

                player.setPlayWhenReady(playWhenReady);

                MediaItem mediaItem = new MediaItem.Builder()
                        .setUri(url)
                        .setMimeType(type)
                        .build();
                player.setMediaItem(mediaItem);

                player.seekTo(currentWindow, playbackPosition);
                player.prepare();

                MyProgressDialog.closeProgressDialog();

            }
        });

    }


    public void setPvLayout(int requestedOrientation) {
        //竖屏
        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            fullscreen_switch.setImageResource(R.drawable.exo_icon_fullscreen_enter);
            ViewGroup.LayoutParams params = video_view_layout.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            int screenWidth;
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                screenWidth = SystemInfo.getScreenHeight(this);
            } else {
                screenWidth = SystemInfo.getScreenWidth(this);
            }

            /* width : height = 4 : 3 */
//            params.height = screenWidth * 3 / 4;

            /* width : height = 16 : 9 */
            params.height = screenWidth * 9 / 16;


            AppLog.d(TAG, " screenWidth =" + screenWidth);
            AppLog.d(TAG, " params.width =" + params.width);
            AppLog.d(TAG, " params.height =" + params.height);
            video_view_layout.setLayoutParams(params);


            top_bar_layout.setVisibility(View.VISIBLE);
            play_info_layout.setVisibility(View.VISIBLE);
            topbar.setVisibility(View.GONE);
            orientationIsVertical = true;
            video_view_layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            ScreenUtils.setPortrait(this);
            setConnectionStatus(connectionStatusResId);
        } else if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            fullscreen_switch.setImageResource(R.drawable.exo_icon_fullscreen_exit);
            //横屏
            ViewGroup.LayoutParams params = video_view_layout.getLayoutParams();

            /* width : height = 4 : 3 */
//            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
//            int screenWidth = SystemInfo.getScreenWidth(this);
//            params.width = screenWidth * 4 / 3;

            /* width : height = 16 : 9 */
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            int screenWidth = SystemInfo.getScreenHeight(this);
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
//            params.height = screenWidth * 9 / 16;



            AppLog.d(TAG, " screenWidth =" + screenWidth);
            AppLog.d(TAG, " params.width =" + params.width);
            AppLog.d(TAG, " params.height =" + params.height);


            video_view_layout.setLayoutParams(params);
//            remoteView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT));
            ScreenUtils.setLandscape(this, requestedOrientation);
            topbar.setVisibility(View.VISIBLE);
            top_bar_layout.setVisibility(View.GONE);
            play_info_layout.setVisibility(View.GONE);
            video_view_layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            orientationIsVertical = false;
            RelativeLayout.LayoutParams remoteViewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);

        }
    }



    private class PlaybackStateListener implements Player.Listener{

        @Override
        public void onPlaybackStateChanged(int playbackState) {
            String stateString;
            switch (playbackState) {
                case ExoPlayer.STATE_IDLE:
                    stateString = "ExoPlayer.STATE_IDLE      -";
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            top_bar_title.setText("Playback Failed");
                        }
                    });
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    stateString = "ExoPlayer.STATE_BUFFERING -";
                    break;
                case ExoPlayer.STATE_READY:
                    stateString = "ExoPlayer.STATE_READY     -";

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            top_bar_title.setText("Playback Succeed");
                        }
                    });

                    break;
                case ExoPlayer.STATE_ENDED:
                    stateString = "ExoPlayer.STATE_ENDED     -";
                    break;
                default:
                    stateString = "UNKNOWN_STATE             -";
                    break;
            }
            AppLog.d(TAG, "changed state to " + stateString);
        }
    }

    private void startWakeup() {
        NADKAuthorization authorization = NADKConfig.getInstance().getLanModeAuthorization();

        try {
            wakeUpThread = new WakeUpThread(authorization.getAccessKey(), authorization.getSecretKey());
            wakeUpThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopWakeup() {
        if (wakeUpThread != null) {
            wakeUpThread.setStopFlag();
            wakeUpThread = null;
        }
    }

    private boolean initWebrtc() {

        try
        {
            /* create webrtc */
            this.webrtc = NADKWebrtc.create(masterRole);

            /* init logger */
//            this.initLogger(this.webrtc.getLogger(), masterRole);

            /* create playback based on webrtc */

//            cachePath = getExternalCacheDir().toString() + "/NADK";
//            createDirectory(cachePath);
//            createDirectory(cachePath);


            this.playback = NADKPlaybackAssist.createWebrtcPlayback(
                    masterRole, cachePath, cachePath, this.webrtc);


            webrtc.addClientStatusListener(new WebrtcStatusListener());

            /* add a status listener, we want to upload file log after all
             * webrtc session disconnected. */
            webrtc.addActiveClientListener(
                    new WebrtcLogStatusListener(webrtc.getLogger(),
                            "android", masterRole ? "master" : "viewer"));
        }
        catch(Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean prepareWebrtc()
    {
        startWakeup();
        try
        {
            /* create a playback client listener,
             * the playback client will be used to send/receive media frames */
            playbackClientService = new NADKPlaybackClientService(this.masterRole, new LocalPlaybackClientListener());
            this.playback.prepare(playbackClientService);

            /* prepare webrtc client*/

            NADKWebrtcSetupInfo setupInfo = NADKConfig.getInstance().createNADKWebrtcSetupInfo(masterRole, signalingType);

            /* create webrtc authentication */
            NADKWebrtcAuthentication authentication = NADKConfig.getInstance().createNADKWebrtcAuthentication(masterRole, signalingType);
            if (authentication == null) {
                return false;
            }


            /* prepare the webrtc client, connect to the signaling */
            this.webrtc.prepareWebrtc(setupInfo, authentication);
        }
        catch(NADKException ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean destroyWebrtc()
    {
        stopWakeup();
        AppLog.i(TAG, "stop viewer");
        /* destroy playback */
        try {
            if (playback != null) {
                this.playback.destroy();
            }

        } catch (NADKException e) {
            e.printStackTrace();
        }
        /* destroy webrtc */
        try {
            if (webrtc != null) {
                this.webrtc.destroyWebrtc();
            }

        } catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }

        this.playbackClientService = null;
        this.localFileListInfo = null;
//        this.fileItemInfoList.clear();
        this.fileItemInfoList = null;
        this.webrtcClient = null;
        this.webrtcControl = null;
        AppLog.reInitLog();
        return true;
    }

    private boolean connect() {
        setConnectionStatus(R.string.text_connecting);
        boolean ret = prepareWebrtc();
        if (!ret) {
            setConnectionStatus(R.string.text_disconnected);
        }
        return true;
    }

    private boolean disconnect() {
        releasePlayer();
        destroyWebrtc();
        return true;
    }

    private void initPlayback() {
        if (localFileListInfo == null) {
//            this.playbackClient = playbackClientService.getPlaybackClient(100);

            try {
                List<NADKFileAvailableInfo> fileAvailableInfos = playbackClient.getFileAvailableInfosByDate(new NADKDateTime(2023, 9, 1, 0, 0, 0));
                AppLog.d(TAG, "fileAvailableInfos: " +fileAvailableInfos);
            } catch (NADKException e) {
                e.printStackTrace();
            }

            localFileListInfo = new DeviceLocalFileListInfo(playbackClient);
            try {
                if (playbackClient == null) {
                    return;
                }
                playbackClient.setFileStatusListener(new NADKFileStatusListener() {
                    @Override
                    public void fileAdded(NADKMediaFile mediaFile) {
                        AppLog.d(TAG, "NADKFileStatusListener fileAdded: " + mediaFile.toString());
                        try {
                            if (localFileListInfo == null) {
                                return;
                            }
                            localFileListInfo.pullDownToRefresh();
                            fileItemInfoList = localFileListInfo.getDeviceFileInfoList();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {

                                    if (fileItemInfoList != null) {
                                        fileListView.renderList(fileItemInfoList);
                                    } else {
                                        fileItemInfoList = new LinkedList<>();
                                        fileListView.renderList(fileItemInfoList);
                                    }

                                }
                            });
                        } catch (NADKException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void fileRemoved(NADKMediaFile mediaFile) {
                        AppLog.d(TAG, "NADKFileStatusListener fileRemoved: " + mediaFile.toString());

                    }
                });
            } catch (NADKException e) {
                e.printStackTrace();
            }
            try {
                localFileListInfo.pullDownToRefresh();
                fileItemInfoList = localFileListInfo.getDeviceFileInfoList();
            } catch (NADKException e) {
                e.printStackTrace();
            }

            CustomImageDownloader downloader = new CustomImageDownloader(localFileListInfo,LocalPlaybackActivity.this);
            ImageLoaderConfig.initImageLoader(getApplicationContext(), downloader);
            ImageLoader.getInstance().destroy();
            ImageLoaderConfig.initImageLoader(getApplicationContext(), downloader);
            fileListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), false, false));

            handler.post(new Runnable() {
                @Override
                public void run() {

                    if (fileItemInfoList != null) {
                        fileListView.renderList(fileItemInfoList);
                    } else {
                        fileItemInfoList = new LinkedList<>();
                        fileListView.renderList(fileItemInfoList);
                    }

                }
            });
        }
    }


    private void createDirectory(String directoryPath) {
        if (directoryPath != null) {
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                boolean ret = directory.mkdirs();
                AppLog.d(TAG, "createDirectory: " + directoryPath + ", ret = " + ret);
            } else {
                AppLog.d(TAG, "createDirectory: " + directoryPath + ", directory exists");
            }
        }
    }

    private class LocalPlaybackClientListener implements NADKPlaybackClientListener {

        @Override
        public void connected(NADKPlaybackClient mplaybackClient) {
            setConnectionStatus(R.string.text_connected);
            playbackClient = mplaybackClient;
            ThreadPoolUtils.getInstance().executorNetThread(new Runnable() {
                @Override
                public void run() {
                    initPlayback();
                }
            }, 200);


        }

        @Override
        public void disconnected(NADKPlaybackClient playbackClient) {
            setConnectionStatus(R.string.text_disconnected);
        }
    }

    private class WebrtcStatusListener implements NADKWebrtcClientStatusListener {

        @Override
        public void created(NADKWebrtcClient webrtcClient) {
            initWebrtcControl(webrtcClient);

        }

        @Override
        public void destroyed(NADKWebrtcClient webrtcClient) {
            setConnectionStatus(R.string.text_disconnected);

        }

        @Override
        public void connected(NADKWebrtcClient webrtcClient) {
            stopWakeup();

//            initWebrtcControl(webrtcClient);
        }

        @Override
        public void disconnected(NADKWebrtcClient webrtcClient) {
            setConnectionStatus(R.string.text_disconnected);

        }

    }

    private void initWebrtcControl(NADKWebrtcClient webrtcClient) {
        this.webrtcClient = webrtcClient;
        try {
            webrtcControl= NADKWebrtcControl.createControl(webrtcClient);
            AppLog.d(TAG, "createControl succeed");
//            webrtcControl.addDataChannelListener(new NADKDataChannelListener() {
//                @Override
//                public void onDataChannelCreated(NADKDataChannel dataChannel) {
//                    try {
//                        AppLog.d(TAG, "onDataChannelCreated: " + dataChannel.getChannelName());
//                    } catch (NADKException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            });
            AppLog.d(TAG, "addDataChannelListener succeed");
        } catch (NADKException e) {
            e.printStackTrace();
            AppLog.d(TAG, "initWebrtcControl: " + e.getMessage());
        }

    }

}