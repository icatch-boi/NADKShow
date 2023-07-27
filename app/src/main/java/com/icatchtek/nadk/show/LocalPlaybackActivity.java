package com.icatchtek.nadk.show;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.MimeTypes;
import com.icatch.smarthome.am.aws.AmazonAwsUtil;
import com.icatchtek.basecomponent.prompt.MyProgressDialog;
import com.icatchtek.basecomponent.prompt.MyToast;
import com.icatchtek.basecomponent.prompt.PercentageProgressDialog;
import com.icatchtek.basecomponent.utils.ClickUtils;
import com.icatchtek.baseutil.ThreadPoolUtils;
import com.icatchtek.baseutil.date.DateConverter;
import com.icatchtek.baseutil.date.DateUtil;
import com.icatchtek.baseutil.device.MyOrientationEventListener;
import com.icatchtek.baseutil.device.ScreenUtils;
import com.icatchtek.baseutil.file.FileOper;
import com.icatchtek.baseutil.file.FileUtil;
import com.icatchtek.baseutil.imageloader.ImageLoaderConfig;
import com.icatchtek.baseutil.info.SystemInfo;
import com.icatchtek.baseutil.log.AppLog;
import com.icatchtek.nadk.playback.NADKPlayback;
import com.icatchtek.nadk.playback.NADKPlaybackAssist;
import com.icatchtek.nadk.playback.NADKPlaybackClient;
import com.icatchtek.nadk.playback.NADKPlaybackClientListener;
import com.icatchtek.nadk.playback.file.NADKFileTransferStatusListener;
import com.icatchtek.nadk.reliant.NADKException;
import com.icatchtek.nadk.reliant.NADKNetAddress;
import com.icatchtek.nadk.reliant.NADKSignalingType;
import com.icatchtek.nadk.reliant.NADKWebrtcAuthentication;
import com.icatchtek.nadk.reliant.NADKWebrtcSetupInfo;
import com.icatchtek.nadk.reliant.parameter.NADKAudioParameter;
import com.icatchtek.nadk.reliant.parameter.NADKVideoParameter;
import com.icatchtek.nadk.reliant.parameter.NADKWebrtcStreamParameter;
import com.icatchtek.nadk.show.assist.WebrtcLogStatusListener;
import com.icatchtek.nadk.show.imageloader.CustomImageDownloader;
import com.icatchtek.nadk.show.kvsarchivedmedia.KVSArchivedMediaClient;
import com.icatchtek.nadk.show.sdk.DeviceLocalFileListInfo;
import com.icatchtek.nadk.show.sdk.FileDownloadStatusListener;
import com.icatchtek.nadk.show.sdk.NADKPlaybackClientService;
import com.icatchtek.nadk.show.utils.DatePickerHelper;
import com.icatchtek.nadk.show.utils.NADKConfig;
import com.icatchtek.nadk.show.utils.NetworkUtils;
import com.icatchtek.nadk.webrtc.NADKWebrtc;
import com.icatchtek.nadk.webrtc.NADKWebrtcClient;
import com.icatchtek.nadk.webrtc.NADKWebrtcClientStatusListener;
import com.icatchtek.nadk.webrtc.assist.NADKAuthorization;
import com.icatchtek.nadk.webrtc.assist.NADKWebrtcAppConfig;
import com.icatchtek.nadk.webrtc.assist.NADKWebrtcServiceRoutines;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tinyai.libmediacomponent.components.filelist.FileItemInfo;
import com.tinyai.libmediacomponent.components.filelist.FileListView;
import com.tinyai.libmediacomponent.components.filelist.FileListView2;
import com.tinyai.libmediacomponent.components.filelist.OperationMode;
import com.tinyai.libmediacomponent.components.filelist.PhotoWallLayoutType;
import com.tinyai.libmediacomponent.components.filelist.RefreshMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class LocalPlaybackActivity extends AppCompatActivity {
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

    private boolean masterRole = false;
    private NADKWebrtc webrtc;
    private NADKPlayback playback;
    private NADKWebrtcStreamParameter streamParameter;
    private NADKPlaybackClientService playbackClientService;
    private int signalingType;


    private FileListView2 fileListView;
    private List<FileItemInfo> fileItemInfoList;
    private DeviceLocalFileListInfo localFileListInfo = null;
    private NADKPlaybackClient playbackClient;

    private PercentageProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_playback);
        back_btn = findViewById(R.id.back_btn);
        top_bar_layout = findViewById(R.id.toolbar_layout);
        play_info_layout = findViewById(R.id.play_info_layout);


        video_view_layout = findViewById(R.id.video_view_layout);
        playerView = findViewById(R.id.player_view);
        fullscreen_switch = findViewById(R.id.exo_fullscreen);
        topbar = findViewById(R.id.exo_top_bar);
        topbar_back = findViewById(R.id.exo_top_bar_back);
        topbar_title = findViewById(R.id.exo_top_bar_title);

        Intent intent = getIntent();
        signalingType = intent.getIntExtra("signalingType", NADKSignalingType.NADK_SIGNALING_TYPE_BASE_TCP);

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

        initWebrtc();
        ThreadPoolUtils.getInstance().executorNetThread(new Runnable() {
            @Override
            public void run() {
                connect();
            }
        }, 200);

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
                ThreadPoolUtils.getInstance().executorNetThread(new Runnable() {
                    @Override
                    public void run() {
                        if (localFileListInfo != null) {
                            String path = localFileListInfo.downloadMediaFile(DeviceLocalFileListInfo.convertToNADKMediaFile(info), new FileDownloadStatusListener());
                            if (path != null) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        fileListView.markDownloaded(info);
                                    }
                                });
                            }
                        }
                    }
                }, 200);
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

    private void loadFile(){
        fileItemInfoList = new LinkedList<>();
        long time = System.currentTimeMillis();
        fileItemInfoList.add(new FileItemInfo(time,2968668,2000,1,1,"https://alifei04.cfp.cn/creative/vcg/800/version23/VCG21gic6371032.jpg","11.jpg"));
        fileItemInfoList.add(new FileItemInfo(time + 1*1000,29648668,5000,1,2,"https://alifei03.cfp.cn/creative/vcg/800/new/VCG21gic6370430.jpg","12.jpg"));
        fileItemInfoList.add(new FileItemInfo(time + 2*1000,29638668,11,1,3,"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg.duoziwang.com%2F2018%2F04%2F2411191727687.jpg&refer=http%3A%2F%2Fimg.duoziwang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1658303305&t=0dabba0a06955a0a56f492b63ac04742","13.jpg"));
        fileItemInfoList.add(new FileItemInfo(time + 3*1000,2968668,15,2,1,"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg.duoziwang.com%2F2017%2F03%2F26%2FB2154.jpg&refer=http%3A%2F%2Fimg.duoziwang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1658303333&t=a7d9d41808adb555a32dfd602969f988","14.mp4"));
        fileItemInfoList.add(new FileItemInfo(time + 4*1000,29628668,2000,2,1,"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg.duoziwang.com%2F2019%2F05%2F08211345608033.jpg&refer=http%3A%2F%2Fimg.duoziwang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1658303333&t=176832d3502f8ffd626e11f743c6efbd","15.mp4"));
        fileItemInfoList.add(new FileItemInfo(time + 5*1000,2968668,5000,2,2,"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg.duoziwang.com%2F2019%2F04%2F07090912704156.jpg&refer=http%3A%2F%2Fimg.duoziwang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1658303333&t=690beb6075135f76d272032e3ff342b5","16.mp4"));
        fileItemInfoList.add(new FileItemInfo(time + 6*1000,29684668,11,2,3,"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg.duoziwang.com%2F2019%2F07%2F12080849902767.jpg&refer=http%3A%2F%2Fimg.duoziwang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1658303333&t=aeedc8ede15711df3568fa0faae69d05","17.mp4"));
        fileItemInfoList.add(new FileItemInfo(time + 7*1000,2968668,2000,2,1,"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg.duoziwang.com%2F2019%2F08%2F01111742211966.jpg&refer=http%3A%2F%2Fimg.duoziwang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1658303333&t=66b0a66d50bc8917ff8b1f1438c3aaef","18.mp4"));
        fileItemInfoList.add(new FileItemInfo(time + 8*1000,2968668,5000,2,2,"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fpic1.win4000.com%2Fwallpaper%2F7%2F53c7489f16b65_130_170.jpg&refer=http%3A%2F%2Fpic1.win4000.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1658303333&t=89cf0cf60921c2acc5aa1a061c6fd1a6","19.mp4"));
        fileItemInfoList.add(new FileItemInfo(time + 9*1000,2968668,11,2,3,"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg.duoziwang.com%2F2017%2F03%2F26%2FB0130.jpg&refer=http%3A%2F%2Fimg.duoziwang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1658303333&t=823e867dd690a16e1257b55eb5999e14","20.mp4"));
        fileItemInfoList.add(new FileItemInfo(time + 10*1000,2968668,15,2,1,"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fi.qqkou.com%2Fi%2F0a3970196102x948153673b15.jpg&refer=http%3A%2F%2Fi.qqkou.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1658303333&t=06ab078403dc379b89f3e4b0a92b02c8","21.mp4"));
        fileItemInfoList.add(new FileItemInfo(time +11*1000,2968668,2000,1,1,"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg.duoziwang.com%2F2018%2F04%2F2411293529514.jpg&refer=http%3A%2F%2Fimg.duoziwang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1658303333&t=b9d7c01904aac1b4f8d7848ee5cc9983","22.jpg"));
        fileItemInfoList.add(new FileItemInfo(time + 12*1000,2968668,5000,1,2,"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg.duoziwang.com%2F2016%2F12%2F14%2F14190452742.jpg&refer=http%3A%2F%2Fimg.duoziwang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1658303333&t=b09a584708b08bd9a4f4ec037014b645","23.jpg"));
        fileItemInfoList.add(new FileItemInfo(time + 13*1000,2968668,11,1,3,"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg.duoziwang.com%2F2016%2F09%2F19%2F1458341903.jpg&refer=http%3A%2F%2Fimg.duoziwang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1658303333&t=3d70e7bdfa56d338559ff18b3fce0ed1","24.jpg"));
        fileItemInfoList.add(new FileItemInfo(time + 14*1000,2968668,15,1,1,"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fpic1.win4000.com%2Fwallpaper%2F7%2F53c748aced092_130_170.jpg&refer=http%3A%2F%2Fpic1.win4000.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1658303333&t=b7f808a46b222e43718e53470b1d231d","25.jpg"));
        fileItemInfoList.add(new FileItemInfo(time + 15*1000,2968668,2000,1,1,"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg.duoziwang.com%2F2019%2F07%2F10291453900812.jpg&refer=http%3A%2F%2Fimg.duoziwang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1658303333&t=9a3b806a83b787974a3f5124a8e06bfb","26.jpg"));
        fileItemInfoList.add(new FileItemInfo(time + 16*1000,2968668,5000,1,2,"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Flmg.jj20.com%2Fup%2Fallimg%2Ftx20%2F460419014828948.jpg&refer=http%3A%2F%2Flmg.jj20.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1658303333&t=ef2962123a829915e824ae9abc7ff4e3","27.jpg"));
        fileItemInfoList.add(new FileItemInfo(time + 17*1000,2968668,11,1,3,"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg.duoziwang.com%2F2019%2F07%2F12080849902157.jpg&refer=http%3A%2F%2Fimg.duoziwang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1658303333&t=b620217cf413ef8adbd49f503d14cbce","28.jpg"));
        fileItemInfoList.add(new FileItemInfo(time + 18*1000,2968668,15,1,1,"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fpic1.win4000.com%2Fwallpaper%2F8%2F580b0d571c7a3_120_80.jpg&refer=http%3A%2F%2Fpic1.win4000.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1658303333&t=7edcb0cf84d86002134e666001ee7848","29.jpg"));
        fileItemInfoList.add(new FileItemInfo(time + 19*1000,2968668,2000,1,1,"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fpic1.win4000.com%2Fwallpaper%2F8%2F580b0d4fa9029_120_80.jpg&refer=http%3A%2F%2Fpic1.win4000.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1658303333&t=1469336ae3e5a65a10ebcfe635aa3b95","30.jpg"));
        fileItemInfoList.add(new FileItemInfo(time + 20*1000,2968668,5000,1,2,"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg.duoziwang.com%2F2017%2F03%2F17%2FB3622.jpg&refer=http%3A%2F%2Fimg.duoziwang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1658303333&t=924d7e1c32808be017e358bd514c6f1a","31.jpg"));
        fileItemInfoList.add(new FileItemInfo(time + 21*1000,2968668,11,1,3,"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fwww.keaidian.com%2Fuploads%2Fallimg%2F191008%2F08155524_48.jpg&refer=http%3A%2F%2Fwww.keaidian.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1658303333&t=822d8b971750ef9676220d5c724d4f9c","32.jpg"));
        fileItemInfoList.add(new FileItemInfo(time + 22*1000,2968668,15,1,1,"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fpic1.win4000.com%2Fwallpaper%2F7%2F53c7488b110fa_120_80.jpg&refer=http%3A%2F%2Fpic1.win4000.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1658303333&t=7c9f2ecf776b33f102542e4a0a3c544b","33.jpg"));
        fileItemInfoList.add(new FileItemInfo(time + 23*1000,2968668,2000,1,1,"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg.duoziwang.com%2F2016%2F10%2F02%2F144712602.jpg&refer=http%3A%2F%2Fimg.duoziwang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1658303333&t=40cc5e1c740144425a497aa177159bf5","34.jpg"));
        fileItemInfoList.add(new FileItemInfo(time + 24*1000,2968668,5000,1,2,"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimages.liqucn.com%2Fimg%2Fh24%2Fh33%2Fimg_localize_c3a5d9ef54f70c335d9c4abf6bfa11d3_200x200.png&refer=http%3A%2F%2Fimages.liqucn.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1658303333&t=08a009545cc3935b42c3656ea1c77980","35.jpg"));
        fileItemInfoList.add(new FileItemInfo(time + 25*1000,2968668,11,1,3,"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg.duoziwang.com%2F2018%2F17%2F05200741509222.jpg&refer=http%3A%2F%2Fimg.duoziwang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1658303333&t=ff2133e67d2fb23d275c4bac40dc6a4b","36.jpg"));
        fileItemInfoList.add(new FileItemInfo(time + 26*1000,2968668,15,1,1,"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fi.qqkou.com%2Fi%2F0a1484765024x3693075515b15.jpg&refer=http%3A%2F%2Fi.qqkou.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1658303333&t=10f2efbf0d916c4994523067d563dabe","37.jpg"));

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
        String dstFile = CACHE_PATH + "/" + itemInfo.getFileName();
        File file = new File(dstFile);
        if (file.exists()) {
            return LOCAL_FILE_PREFIX + dstFile;
        }
        if (localFileListInfo != null) {
//            showDownloadDialog();
            String filePath = localFileListInfo.downloadMediaFile(DeviceLocalFileListInfo.convertToNADKMediaFile(itemInfo), new NADKFileTransferStatusListener() {
                @Override
                public void transferNotify(long transferedSize, long fileSize) {
                    updateDownloadDialog(transferedSize, fileSize);
                }
            });
            dismissDownloadDialog();
            if (filePath != null && !filePath.isEmpty()) {
                File srcFile = new File(filePath);
                srcFile.renameTo(new File(dstFile));
//                FileOper.createDirectory(STORAGE_PATH);
//                if (FileUtil.copy(filePath, dstFile)) {
                    return LOCAL_FILE_PREFIX + dstFile;
//                }
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

    private void updateDownloadDialog(long transferedSize, long fileSize) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (dialog != null && dialog.isShowing()) {
                    dialog.setMaxProgress(fileSize);
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



    private class PlaybackStateListener implements Player.EventListener{

        @Override
        public void onPlaybackStateChanged(int playbackState) {
            String stateString;
            switch (playbackState) {
                case ExoPlayer.STATE_IDLE:
                    stateString = "ExoPlayer.STATE_IDLE      -";
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    stateString = "ExoPlayer.STATE_BUFFERING -";
                    break;
                case ExoPlayer.STATE_READY:
                    stateString = "ExoPlayer.STATE_READY     -";

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

    private boolean initWebrtc() {
        List<NADKNetAddress> localAddresses = NetworkUtils.getNetworkAddress();
        this.streamParameter = new NADKWebrtcStreamParameter(
                new NADKAudioParameter(), new NADKVideoParameter(), localAddresses);

        try
        {
            /* create webrtc */
            this.webrtc = NADKWebrtc.create(masterRole);

            /* init logger */
//            this.initLogger(this.webrtc.getLogger(), masterRole);

            /* create playback based on webrtc */
            createDirectory(MEDIA_PATH);
            createDirectory(CACHE_PATH);

            this.playback = NADKPlaybackAssist.createWebrtcPlayback(
                    masterRole, MEDIA_PATH, CACHE_PATH, this.webrtc);


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
            this.webrtc.prepareWebrtc(setupInfo, authentication, this.streamParameter);
        }
        catch(NADKException ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean destroyWebrtc()
    {
        AppLog.i(TAG, "stop viewer");
        /* destroy playback */
        try {
            this.playback.destroy();
        } catch (NADKException e) {
            e.printStackTrace();
        }
        /* destroy webrtc */
        try {
            this.webrtc.destroyWebrtc();
        } catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }

        this.playbackClientService = null;
        this.localFileListInfo = null;
//        this.fileItemInfoList.clear();
        this.fileItemInfoList = null;
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
            localFileListInfo = new DeviceLocalFileListInfo(playbackClient);
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

            handler.post(new Runnable() {
                @Override
                public void run() {

                    if (fileItemInfoList != null) {
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

        }

        @Override
        public void destroyed(NADKWebrtcClient webrtcClient) {
            setConnectionStatus(R.string.text_disconnected);

        }

        @Override
        public void connected(NADKWebrtcClient webrtcClient) {

        }

        @Override
        public void disconnected(NADKWebrtcClient webrtcClient) {
            setConnectionStatus(R.string.text_disconnected);

        }

        @Override
        public void formatChanged(NADKWebrtcClient webrtcClient, NADKAudioParameter audioParameter, NADKVideoParameter videoParameter) {

        }
    }

}