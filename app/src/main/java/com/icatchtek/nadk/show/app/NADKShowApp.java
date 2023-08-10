package com.icatchtek.nadk.show.app;

import static com.tencent.bugly.BuglyStrategy.a.CRASHTYPE_NATIVE;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.icatchtek.baseutil.ThreadPoolUtils;
import com.icatchtek.baseutil.log.AppLog;
import com.icatchtek.nadk.show.R;
import com.icatchtek.nadk.show.utils.CrashHandler;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class NADKShowApp extends Application {
    private static final String TAG = NADKShowApp.class.getSimpleName();
    private static final String APP_BUGLY_ID = "5577da336f";

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
//        AppLog.enableAppLog(this, new BPSCamLog());
        //App异常崩溃Crash不自动重启，直接退出app，可以避免无限crash或者是丢失必要的传递信息引起其他的crash
//        CrashHandler.getInstance().init(this, BuildConfig.DEBUG);
        CrashHandler.getInstance().init(this, true);
        initBugly();

        //适配androdi 8.0 通知栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "subscribe";
            String channelName = getResources().getString(R.string.text_subscribe_msg);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            createNotificationChannel(channelId,channelName,importance);
        }


        //初始化内存泄漏分析工具
//        initLeakCanary();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    //判断用户是否已经打开App
    private boolean shouldInit() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos;
        try {
            // TODO: Do not use this API.
            processInfos = am.getRunningAppProcesses();
        } catch (NullPointerException e) {
            return false;
        }
        String mainProcessName = getPackageName();
        int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

    private static View getContentView(Activity ac) {
        if (ac == null) {
            return null;
        }
        ViewGroup view = (ViewGroup) ac.getWindow().getDecorView();
        FrameLayout content = (FrameLayout) view.findViewById(android.R.id.content);
        return content.getChildAt(0);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        NotificationManager notificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

    private void initBugly() {
        /***** Beta高级设置 *****/
        /**
         * true表示app启动自动初始化升级模块;
         * false不会自动初始化;
         * 开发者如果担心sdk初始化影响app启动速度，可以设置为false，
         * 在后面某个时刻手动调用Beta.init(getApplicationContext(),false);
         */

        /***** 统一初始化Bugly产品，包含Beta *****/
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(this);
//        strategy.setEnableANRCrashMonitor(true);
        strategy.setCrashHandleCallback(new CrashReport.CrashHandleCallback() {
            @Override
            public Map<String, String> onCrashHandleStart(int crashType, String errorType,
                                                          String errorMessage, String errorStack) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("Key", "Value");
                AppLog.e(TAG,"onCrashHandleStart crashType:" + crashType);
                AppLog.e(TAG,"onCrashHandleStart errorType:" + errorType);
                AppLog.e(TAG,"onCrashHandleStart errorMessage:" + errorMessage);
                AppLog.e(TAG,"onCrashHandleStart errorStack:");
                AppLog.e(TAG,"#++++++++++++++++++++++++++++++++++++++++++#");
                String[] callstackList = errorStack.split("\n");
                for (String error : callstackList) {
                    AppLog.e(TAG,"onCrashHandleStart errorStack: " + error);
                }
                AppLog.e(TAG,"#++++++++++++++++++++++++++++++++++++++++++#");

//                if (crashType == CRASHTYPE_NATIVE) {
//                    ThreadPoolUtils.getInstance().schedule(new Runnable() {
//                        @Override
//                        public void run() {
//                            CrashHandler.getInstance().uncaughtException(new Thread(), new Exception("CRASHTYPE_NATIVE: " + errorStack));
//                        }
//                    }, 500, TimeUnit.MILLISECONDS);
//                }
                return null;
            }

            @Override
            public byte[] onCrashHandleStart2GetExtraDatas(int crashType, String errorType,
                                                           String errorMessage, String errorStack) {
//                AppLog.e(TAG,"onCrashHandleStart2GetExtraDatas crashType:" + crashType);
//                AppLog.e(TAG,"onCrashHandleStart2GetExtraDatas errorType:" + errorType);
//                AppLog.e(TAG,"onCrashHandleStart2GetExtraDatas errorMessage:" + errorMessage);
//                AppLog.e(TAG,"onCrashHandleStart2GetExtraDatas errorStack:");
//                AppLog.e(TAG,"#++++++++++++++++++++++++++++++++++++++++++#");
//                String[] callstackList = errorStack.split("\n");
//                for (String error : callstackList) {
//                    AppLog.e(TAG,"onCrashHandleStart errorStack: " + error);
//                }
//                AppLog.e(TAG,"#++++++++++++++++++++++++++++++++++++++++++#");
//
//                if (crashType == CRASHTYPE_NATIVE) {
//                    ThreadPoolUtils.getInstance().schedule(new Runnable() {
//                        @Override
//                        public void run() {
//                            CrashHandler.getInstance().uncaughtException(new Thread(), new Exception("CRASHTYPE_NATIVE: " + errorStack));
//                        }
//                    }, 1000, TimeUnit.MILLISECONDS);
//                }
//                try {
//                    return "Extra data.".getBytes("UTF-8");
//                } catch (Exception e) {
//                    return null;
//                }

                return null;
            }

        });

        Bugly.init(getApplicationContext(), APP_BUGLY_ID, true, strategy);
        CrashReport.setHandleNativeCrashInJava(true);

    }

}
