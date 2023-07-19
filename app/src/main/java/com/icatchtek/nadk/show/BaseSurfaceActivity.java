/**************************************************************************
 *
 *    Copyright (c) 2014-2022 by iCatch Technology Co., Ltd.
 *
 *  This software is copyrighted by and is the property of Sunplus
 *  Technology Co., Ltd. All rights are reserved by Sunplus Technology
 *  Co., Ltd. This software may only be used in accordance with the
 *  corresponding license agreement. Any unauthorized use, duplication,
 *  distribution, or disclosure of this software is expressly forbidden.
 *
 *  This Copyright notice MUST not be removed or modified without prior
 *  written consent of Sunplus Technology Co., Ltd.
 *
 *  Sunplus Technology Co., Ltd. reserves the right to modify this
 *  software without notice.
 *
 *  Sunplus Technology Co., Ltd.
 *  19, Innovation First Road, Science-Based Industrial Park,
 *  Hsin-Chu, Taiwan, R.O.C.
 *
 *  Author: peng.tan
 *  Email:  peng.tan@sunmedia.com.cn
 *
 **************************************************************************/

package com.icatchtek.nadk.show;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;

import com.icatchtek.baseutil.log.AppLog;
import com.icatchtek.nadk.reliant.NADKException;
import com.icatchtek.nadk.streaming.render.NADKStreamingRender;
import com.icatchtek.nadk.streaming.render.gl.surface.NADKSurfaceContext;


public abstract class BaseSurfaceActivity extends Activity
{
    protected boolean surfaceReady = false;
    protected NADKSurfaceContext surfaceContext;
    protected RelativeLayout live_view_control_layout;
    protected SurfaceView surfaceView;
    private SurfaceHolder.Callback callback;



    /**
     * The sub class should provide a layout file contains SurfaceView component named surfaceView1
     */
    protected abstract void setContentViewWhichHasSurfaceView1();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentViewWhichHasSurfaceView1();
        live_view_control_layout = findViewById(R.id.live_view_control_layout);
        surfaceView = findViewById(R.id.surfaceView1);
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (live_view_control_layout.getVisibility() == View.VISIBLE) {
                    live_view_control_layout.setVisibility(View.GONE);
                } else {
                    live_view_control_layout.setVisibility(View.VISIBLE);
                }
            }
        });

        initSurface(null);
    }

    protected void initSurface(NADKStreamingRender render) {
        Log.i("__render__", "Base Activity: ");
        if (callback != null) {
            surfaceView.getHolder().removeCallback(callback);
        }

        callback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                AppLog.i("LiveView", "Flow, surfaceReady");
                surfaceContext = new NADKSurfaceContext(surfaceView.getHolder().getSurface());
                surfaceReady = true;
                if (render != null) {
                    try {
                        render.changeSurfaceContext(surfaceContext);
                    } catch (NADKException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("__render__", "surface available: " + surfaceContext);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
            {
                try {
                    surfaceContext.setViewPort(0, 0, width, height);
                    Log.i("__render__", "surface changed: " + surfaceContext);
                } catch (NADKException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                surfaceReady = false;
                Log.i("__render__", "surface destroyed: ");
            }
        };
        surfaceView.getHolder().addCallback(callback);

    }
}
