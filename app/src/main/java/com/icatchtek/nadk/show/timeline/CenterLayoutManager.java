package com.icatchtek.nadk.show.timeline;

import android.content.Context;
import android.util.DisplayMetrics;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by sha.liu on 2023/9/4.
 */
public class CenterLayoutManager extends LinearLayoutManager {

    static int lastPositon = 0;
    static int targetPosion = 0;

    public CenterLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        CenterSmoothScroller smoothScroller = new CenterSmoothScroller(recyclerView.getContext());
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }

    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int lastpositon, int position) {
        this.lastPositon = lastpositon;
        this.targetPosion = position;
        smoothScrollToPosition(recyclerView, state, position);
    }

    public static class CenterSmoothScroller extends LinearSmoothScroller {

        private static float duration = 400f;//显示到中间的动画时长

        public CenterSmoothScroller(Context context) {
            super(context);
        }

        @Override
        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
            return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
        }

        @Override
        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
            float newDuration = duration / (Math.abs(targetPosion - lastPositon));//重新计算相近两个位置的滚动间隔
            return newDuration / displayMetrics.densityDpi;
        }

        @Override
        protected int calculateTimeForScrolling(int dx) {
            return super.calculateTimeForScrolling(dx);
        }

    }
}
