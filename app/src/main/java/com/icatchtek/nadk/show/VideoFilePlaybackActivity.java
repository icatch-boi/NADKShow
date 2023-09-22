package com.icatchtek.nadk.show;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.MimeTypes;
import com.icatchtek.basecomponent.prompt.MyProgressDialog;
import com.icatchtek.basecomponent.prompt.MyToast;
import com.icatchtek.basecomponent.utils.ClickUtils;
import com.icatchtek.baseutil.ThreadPoolUtils;
import com.icatchtek.baseutil.date.DateConverter;
import com.icatchtek.baseutil.date.DateUtil;
import com.icatchtek.baseutil.device.MyOrientationEventListener;
import com.icatchtek.baseutil.device.ScreenUtils;
import com.icatchtek.baseutil.info.SystemInfo;
import com.icatchtek.baseutil.log.AppLog;
import com.icatchtek.nadk.show.kvsarchivedmedia.KVSArchivedMediaClient;
import com.icatchtek.nadk.show.utils.DatePickerHelper;
import com.icatchtek.nadk.show.utils.NADKConfig;
import com.icatchtek.nadk.webrtc.assist.NADKAuthorization;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class VideoFilePlaybackActivity extends NADKShowBaseActivity {
    private static final String TAG = VideoFilePlaybackActivity.class.getSimpleName();
    private static final String[] supportProtocol = {"HLS", "DASH"};
    private List<String> supportProtocolList;
    private Handler handler = new Handler();
    private Spinner option_spinner;

    private RelativeLayout video_view_layout;
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    private ImageButton fullscreen_switch;
    private RelativeLayout topbar;
    private ImageView topbar_back;
    private TextView topbar_title;
    private boolean orientationIsVertical = true;
    private PlaybackStateListener playbackStateListener;
    private String currentProtocol = supportProtocol[0];
    private MyOrientationEventListener orientationEventListener;
    private Button play_btn;
    private RelativeLayout play_info_layout;
    private RelativeLayout top_bar_layout;
    private ImageButton back_btn;
    private TextView start_time_txt;
    private TextView end_time_txt;

    private TextView debug_info_txt;
    private String debugInfo;

    private KVSArchivedMediaClient kvsArchivedMediaClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_playback);
        top_bar_layout = findViewById(R.id.toolbar_layout);
        play_info_layout = findViewById(R.id.play_info_layout);
        option_spinner = findViewById(R.id.protocol_spinner);
        back_btn = findViewById(R.id.back_btn);
        start_time_txt = findViewById(R.id.start_time_txt);
        end_time_txt = findViewById(R.id.end_time_txt);

        video_view_layout = findViewById(R.id.video_view_layout);
        playerView = findViewById(R.id.player_view);
        fullscreen_switch = findViewById(R.id.exo_fullscreen);
        topbar = findViewById(R.id.exo_top_bar);
        topbar_back = findViewById(R.id.exo_top_bar_back);
        topbar_title = findViewById(R.id.exo_top_bar_title);
        play_btn = findViewById(R.id.play_btn);
        initActivityCfg();

        LinearLayout play_protocol_layout = findViewById(R.id.play_protocol_layout);
        play_protocol_layout.setVisibility(View.GONE);
        ScrollView debug_info_layout = findViewById(R.id.debug_info_layout);
        debug_info_layout.setVisibility(View.VISIBLE);

        debug_info_txt = findViewById(R.id.debug_info_txt);

        play_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
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

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        Date currentDate = new Date();
        String startTime = DateConverter.toLocalTimeStr(new Date(currentDate.getTime() - 60 * 1000));
        String endTime = DateConverter.toLocalTimeStr(currentDate);
        start_time_txt.setText(startTime);
        end_time_txt.setText(endTime);
        start_time_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*年月日时间选择器*/
                Calendar calendar = Calendar.getInstance();
                Date date = DateConverter.timeStr2Date(start_time_txt.getText().toString());
                calendar.setTime(date);
                DatePickerHelper.showDateTimePickerDialog(VideoFilePlaybackActivity.this, calendar, new DatePickerHelper.OnDateTimeSelectedBlock() {
                    @Override
                    public void onDateSelected(int year, int month, int dayOfMonth, int hour, int min, int seconds) {
                        String datetime = String.format("%d/%02d/%02d %02d:%02d:%02d", year, month+1, dayOfMonth, hour, min, seconds);
                        start_time_txt.setText(datetime);
                        AppLog.d("日期选择器回调", datetime);

                    }
                });
            }
        });
        end_time_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*年月日时间选择器*/
                Calendar calendar = Calendar.getInstance();
                Date date = DateConverter.timeStr2Date(end_time_txt.getText().toString());
                calendar.setTime(date);
                DatePickerHelper.showDateTimePickerDialog(VideoFilePlaybackActivity.this, calendar, new DatePickerHelper.OnDateTimeSelectedBlock() {
                    @Override
                    public void onDateSelected(int year, int month, int dayOfMonth, int hour, int min, int seconds) {
                        String datetime = String.format("%d/%02d/%02d %02d:%02d:%02d", year, month+1, dayOfMonth, hour, min, seconds);
                        end_time_txt.setText(datetime);
                        AppLog.d("日期选择器回调", datetime);
                    }
                });            }
        });

        updateSpinner();
        setPvLayout(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initializePlayer();
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
        releasePlayer();
        AppLog.reInitLog();

        super.onDestroy();
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


    private void initializePlayer() {
        playbackStateListener = new PlaybackStateListener();
        if (player == null) {

            DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
            trackSelector.setParameters(
                    trackSelector.buildUponParameters().setMaxVideoSizeSd());
            player = new SimpleExoPlayer.Builder(this)
                    .setTrackSelector(trackSelector)
                    .build();

            player.addAnalyticsListener(new PlayerEventLogger(trackSelector, "ExoPlayerDebug"));

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

    private class PlayerEventLogger extends EventLogger {

        public PlayerEventLogger(DefaultTrackSelector trackSelector, String exoPlayerDebug) {
            super(trackSelector, exoPlayerDebug);
        }

        @Override
        protected void logd(String msg) {
            super.logd(msg);
//            debugInfo += msg;
        }

        @Override
        protected void loge(String msg) {
            super.loge(msg);
            debugInfo += msg;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    debug_info_txt.setText(debugInfo);
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

    private void play() {
        ThreadPoolUtils.getInstance().executorNetThread(new Runnable() {
            @Override
            public void run() {

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyProgressDialog.showProgressDialog(VideoFilePlaybackActivity.this);
                        if (player.isPlaying() || player.isLoading()) {
                            player.stop();
                        }
                        debug_info_txt.setText("");
                    }
                });

//                String url = getUrl();
//
//                if (url != null && !url.isEmpty()) {
//                    player(url);
//                } else {
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            MyProgressDialog.closeProgressDialog();
//                            MyToast.show(VideoFilePlaybackActivity.this, "play failed");
//                        }
//                    });
//                }

                List<MediaItem> urls = getUrlList();

                if (urls != null && !urls.isEmpty()) {
                    playerList(urls);
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            MyProgressDialog.closeProgressDialog();
                            MyToast.show(VideoFilePlaybackActivity.this, "play failed");
                        }
                    });
                }

            }
        }, 200);

    }

    private void pauseVideo() {
        player.pause();
    }

    private String getUrl() {
        if (kvsArchivedMediaClient == null) {
            NADKAuthorization authorization = NADKConfig.getInstance().getAWSKVSStreamAuthorization();
            if (authorization != null) {
                kvsArchivedMediaClient = new KVSArchivedMediaClient(authorization);
            }
        }

        if (kvsArchivedMediaClient != null) {
            Date startTime = DateUtil.camTime2Date(start_time_txt.getText().toString());
            Date endTime = DateUtil.camTime2Date(end_time_txt.getText().toString());
            AppLog.d(TAG, "getUrl: startTime = " + startTime.getTime() + ", endTime = " + endTime.getTime());
            if (currentProtocol.equals("HLS")) {
                return kvsArchivedMediaClient.getHLSUrl(startTime, endTime);
            } else if (currentProtocol.equals("DASH")) {
                return kvsArchivedMediaClient.getDashUrl(startTime, endTime);
            }
        }

        return "";

    }

    private List<MediaItem> getUrlList() {

        String STORAGE_PATH = "/storage/self/primary/LocalPlayback";
        String LOCAL_FILE_PREFIX = "file://";
        File directory = new File(STORAGE_PATH);
        List<MediaItem> mediaItemList = new ArrayList<>();
        MediaItem mediaItem1 = new MediaItem.Builder()
                .setUri("https://dxtest-hls-public.s3.cn-northwest-1.amazonaws.com.cn/bpsc_hls.m3u8")
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .build();
        mediaItemList.add(mediaItem1);
        if (directory.exists()) {
            if (directory.isDirectory()) {
                String[] files = directory.list();
                if (files != null) {
                    for (String file : files) {
                        if (file.endsWith(".mp4") || file.endsWith(".MP4") || file.endsWith(".lrv") || file.endsWith(".LRV")) {
                            String url = LOCAL_FILE_PREFIX + STORAGE_PATH + "/" + file;
                            MediaItem mediaItem = new MediaItem.Builder()
                                    .setUri(url)
                                    .setMimeType(MimeTypes.APPLICATION_MP4)
                                    .build();
                            mediaItemList.add(mediaItem);
                        }
                    }
                }
            }
        }

        return mediaItemList;

    }


    private void player(String url) {
        String mimeType = MimeTypes.APPLICATION_M3U8;
        if (url.endsWith(".mp4") || url.endsWith(".MP4")) {
            mimeType = MimeTypes.APPLICATION_MP4;
        } else if (currentProtocol.equals("HLS")) {
            mimeType = MimeTypes.APPLICATION_M3U8;
        } else if (currentProtocol.equals("DASH")) {
            mimeType = MimeTypes.APPLICATION_MPD;
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

    private void playerList(List<MediaItem> mediaItemList) {
        handler.post(new Runnable() {
            @Override
            public void run() {

                player.setPlayWhenReady(playWhenReady);
                player.setMediaItems(mediaItemList);

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

    private void updateSpinner() {
        supportProtocolList = new ArrayList<>(Arrays.asList(supportProtocol));
        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, supportProtocolList);
//        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        option_spinner.setAdapter(adapter);
        for (int i = 0; i < supportProtocolList.size(); i++) {
            String protocol = supportProtocolList.get(i);
            if (protocol.equals(currentProtocol)) {
                option_spinner.setSelection(i);
                break;
            }
        }

        option_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                currentProtocol = supportProtocolList.get(position);
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        updateCurrentOption();
//                    }
//                });


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private class PlaybackStateListener implements Player.Listener{

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

}