package com.icatchtek.nadk.show.timeline;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.icatchtek.baseutil.log.AppLog;
import com.icatchtek.nadk.show.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sha.liu on 2023/8/21.
 */
public class ZFTimeLine extends View {

    private final int SCALE_TYPE_BIG = 1;           //大刻度
    private final int SCALE_TYPE_SMALL = 2;         //小刻度

    private int intervalValue;                    //小刻度宽度
    private int scaleType;
    private long currentInterval;                  //中间刻度对应的时间戳

    private SimpleDateFormat formatterScale;        //日期格式化,用于时间戳和时间字符的转换
    private SimpleDateFormat formatterProject;      //日期格式化,用于时间戳和时间字符的转换

    private Paint paintIntervalLine, paintSelect, paintCenter, paintLine, paintSos, paintEndLine, paintBackground;   //三种不同颜色的画笔
    private int point = 0;                          //用于当前触控点数量
    private float moveStartX = 0;                   //用于记录单点触摸点位置,用于计算拖距离
    private float scaleValue = 0;                   //用于记录两个触摸点间距,用于时间轴缩放计算

    private boolean onLock;                         //用于屏蔽时间轴拖动,为true时无法拖动

    private OnZFTimeLineListener listener;          //时间轴拖动监听,这个只在拖动完成时返回数据

    //已录制视频数据信息
    List<VideoInfo> calstuff;
    List<VideoInfo> intervalCalstuff;

    int currentI = -1;
    //设置监听
    public void setListener(OnZFTimeLineListener listener) {
        this.listener = listener;
    }

    //拖动时间轴监听
    public interface OnZFTimeLineListener{
        void didMoveToDate(long date, int position, boolean showThumbnail);

        void didMoveToUp(long date, int position);
    }
    public ZFTimeLine(Context context) {
        super(context);
        init();
    }

    public ZFTimeLine(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ZFTimeLine(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    //数据数据初始化
    private void init(){

        scaleType = SCALE_TYPE_SMALL;
        intervalValue = 0;
//        setAlpha(0.8f);
        setBackgroundColor(getResources().getColor(R.color.black));
        timeNow();

        onLock = false;

        formatterScale = new SimpleDateFormat("HH:mm");
        formatterProject = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        paintIntervalLine = new Paint();
        paintIntervalLine.setColor(getResources().getColor(R.color.timelineIntervalLine));
        paintIntervalLine.setTextSize(intDip2px(10));
        paintIntervalLine.setTextAlign(Paint.Align.CENTER);
        paintIntervalLine.setStrokeWidth(dip2px(2));

        paintEndLine = new Paint();
        paintEndLine.setColor(getResources().getColor(R.color.timelineEndLine));
        paintEndLine.setTextSize(intDip2px(10));
        paintEndLine.setTextAlign(Paint.Align.CENTER);
        paintEndLine.setStrokeWidth(dip2px(1));

        paintLine = new Paint();
        paintLine.setColor(getResources().getColor(R.color.timelineLine));
        paintLine.setTextSize(intDip2px(10));
        paintLine.setTextAlign(Paint.Align.CENTER);
        paintLine.setStrokeWidth(dip2px(2));

        paintSelect = new Paint();
        paintSelect.setColor(getResources().getColor(R.color.timelineSelect));

        paintBackground = new Paint();
        paintBackground.setColor(getResources().getColor(R.color.timelineBackground));

        paintCenter = new Paint();
        paintCenter.setColor(getResources().getColor(R.color.timelineCenter));

        paintSos = new Paint();
        paintSos.setColor(getResources().getColor(R.color.timelineSos));
    }

    //把当前时间戳设置我中间刻度对应的时间戳
    private void timeNow(){
        currentInterval = System.currentTimeMillis();
    }
    //宽度1所代表的毫秒数
    private long milliscondsOfIntervalValue(){
        if (scaleType == SCALE_TYPE_BIG){
            return (long) (6*60000.0/intervalValue);
        }else {
            return (long) (60000.0/intervalValue);
        }
    }

    private float dip2px(float dipValue){
        return dipValue * (getResources().getDisplayMetrics().densityDpi / 240);
    }
    private int intDip2px(float dipValue){
        return (int) (dip2px(dipValue) + 0.5);
    }

    private float getSosHeight() {
        return dip2px(10) + dip2px(10);
    }

    private boolean isSoSFile(VideoInfo videoInfo) {
//        return videoInfo.isSosFile();
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //初始化小刻度的间隔,在init里densityDpi的数据为0,所以放到这里了
        if (intervalValue == 0) intervalValue = intDip2px(10);

        //中间线的x值
        long centerX = getWidth()/2;
        long minX = 0;
        long maxX = getWidth();
        //左边界线代表的时间戳
        long leftInterval = currentInterval - centerX * milliscondsOfIntervalValue();
        //右边界线时间戳
        long rightInterval = currentInterval + centerX * milliscondsOfIntervalValue();

        long x;             //记录绘制刻度线的位置
        long interval;      //记录所绘制刻度线代表的时间戳
        float startY = dip2px(10);

        //下面计算需要绘制的第一个刻度线的位置和所代表的时间戳
        if (scaleType == SCALE_TYPE_BIG){
            long a = leftInterval/(60 * 6 * 1000);
            interval =  ((a + 1) * (60 * 6 * 1000));
            x = (interval - leftInterval) / milliscondsOfIntervalValue();
        }else {
            long a = leftInterval/(60 * 1000);
            interval = ((a + 1) * (60 * 1000));
            x = (interval - leftInterval) / milliscondsOfIntervalValue();
        }

        //这里是这个项目特有的需求,根据视频数据绘制绿色和红色区域,分别代表该位置有已录制的普通视频和紧急视频(行车记录仪)
        if (calstuff != null){

            List<VideoInfo> rangeList = new ArrayList<>();
            long start_x = 0;
            long end_x = 0;
//            int currentI = getCurrentVideoInfo(currentInterval);
            if (currentI >= 0) {
                VideoInfo info = calstuff.get(currentI);
                //获取视频文件的开始时间戳和结束时间戳
                long startInterval = info.getStartTime().getTime();
                long endInterval = info.getEndTime().getTime();
                long rangeEndX = (endInterval - currentInterval) / milliscondsOfIntervalValue();
                long rangeStartX = (currentInterval - startInterval) / milliscondsOfIntervalValue();
                start_x = centerX - rangeStartX;
                end_x = centerX + rangeEndX;

                if (end_x >= maxX) {
                    canvas.drawRect(centerX,startY, maxX,getHeight(), paintSelect);
                    if (isSoSFile(info)){
                        //紧急视频 为红色区域色块
                        canvas.drawRect(centerX,startY, maxX, getSosHeight(), paintSos);
                    }

                } else {
                    canvas.drawRect(centerX,startY, end_x,getHeight(), paintSelect);
                    if (isSoSFile(info)){
                        //紧急视频 为红色区域色块
                        canvas.drawRect(centerX,startY, end_x, getSosHeight(), paintSos);
                    }
                    int i = currentI;
                    long startX = end_x;
                    long endX = end_x;
                    boolean endLine = false;
                    while (i < calstuff.size() - 1) {
                        ++i;
                        VideoInfo nextInfo = calstuff.get(i);
                        long nextStartInterval = nextInfo.getStartTime().getTime();
                        long nextEndInterval = nextInfo.getEndTime().getTime();
                        long nextRangeX = (nextEndInterval - nextStartInterval) / milliscondsOfIntervalValue();
                        long nextEndx = endX + nextRangeX;

                        if (nextStartInterval - endInterval > 5 * 1000) {
                            canvas.drawLine(endX,startY, endX, getHeight(), paintEndLine);
                            endLine = true;
                            if (nextEndx >= maxX) {
                                canvas.drawRect(endX, startY, maxX, getHeight(), paintBackground);
                                if (isSoSFile(nextInfo)){
                                    //紧急视频 为红色区域色块
                                    canvas.drawRect(endX,startY, maxX, getSosHeight(), paintSos);
                                }
                                break;
                            } else {
                                canvas.drawRect(endX, startY, nextEndx, getHeight(), paintBackground);
                                if (isSoSFile(nextInfo)){
                                    //紧急视频 为红色区域色块
                                    canvas.drawRect(endX,startY, nextEndx, getSosHeight(), paintSos);
                                }
                                endX = nextEndx;
                                endInterval = nextEndInterval;
                            }
                        } else {
                            if (nextEndx >= maxX) {
                                if (!endLine) {
                                    canvas.drawRect(endX,startY, maxX,getHeight(), paintSelect);
                                } else {
                                    canvas.drawRect(endX, startY, maxX, getHeight(), paintBackground);
                                }
                                if (isSoSFile(nextInfo)){
                                    //紧急视频 为红色区域色块
                                    canvas.drawRect(endX,startY, maxX, getSosHeight(), paintSos);
                                }
                                break;
                            } else {
                                if (!endLine) {
                                    canvas.drawRect(endX,startY, nextEndx,getHeight(), paintSelect);
                                } else {
                                    canvas.drawRect(endX, startY, nextEndx, getHeight(), paintBackground);
                                }
                                if (isSoSFile(nextInfo)){
                                    //紧急视频 为红色区域色块
                                    canvas.drawRect(endX,startY, nextEndx, getSosHeight(), paintSos);
                                }
                                endX = nextEndx;
                                endInterval = nextEndInterval;
                            }

                        }

                    }

                    if (i >= calstuff.size() - 1) {
                        canvas.drawLine(endX,startY, endX, getHeight(), paintEndLine);
                    }

                }


                if (start_x <= minX) {
                    canvas.drawRect(minX,startY, centerX,getHeight(), paintSelect);
                    if (isSoSFile(info)){
                        //紧急视频 为红色区域色块
                        canvas.drawRect(minX,startY, centerX, getSosHeight(), paintSos);
                    }
                } else {
                    canvas.drawRect(start_x,startY, centerX,getHeight(), paintSelect);
                    if (isSoSFile(info)){
                        //紧急视频 为红色区域色块
                        canvas.drawRect(start_x,startY, centerX, getSosHeight(), paintSos);
                    }
                    int i = currentI;
                    long startX = start_x;
                    long endX = start_x;
                    boolean endLine = false;
                    while (i > 0) {
                        --i;
                        VideoInfo nextInfo = calstuff.get(i);
                        long nextStartInterval = nextInfo.getStartTime().getTime();
                        long nextEndInterval = nextInfo.getEndTime().getTime();
                        long nextRangeX = (nextEndInterval - nextStartInterval) / milliscondsOfIntervalValue();
                        long nextStartX = startX - nextRangeX;

                        if (startInterval - nextEndInterval > 5 * 1000) {
                            canvas.drawLine(startX,startY, startX, getHeight(), paintEndLine);
                            endLine = true;
                            if (nextStartX <= minX) {
                                canvas.drawRect(minX, startY, startX, getHeight(), paintBackground);
                                if (isSoSFile(nextInfo)){
                                    //紧急视频 为红色区域色块
                                    canvas.drawRect(minX,startY, startX, getSosHeight(), paintSos);
                                }
                                break;
                            } else {
                                canvas.drawRect(nextStartX, startY, startX, getHeight(), paintBackground);
                                if (isSoSFile(nextInfo)){
                                    //紧急视频 为红色区域色块
                                    canvas.drawRect(nextStartX,startY, startX, getSosHeight(), paintSos);
                                }
                                startX = nextStartX;
                                startInterval = nextStartInterval;
                            }
                        } else {
                            if (nextStartX <= minX) {
                                if (!endLine) {
                                    canvas.drawRect(minX,startY, startX,getHeight(), paintSelect);
                                } else {
                                    canvas.drawRect(minX,startY, startX,getHeight(), paintBackground);
                                }
                                if (isSoSFile(nextInfo)){
                                    //紧急视频 为红色区域色块
                                    canvas.drawRect(minX,startY, startX, getSosHeight(), paintSos);
                                }
                                break;
                            } else {
                                if (!endLine) {
                                    canvas.drawRect(nextStartX,startY, startX,getHeight(), paintSelect);
                                } else {
                                    canvas.drawRect(nextStartX,startY, startX,getHeight(), paintBackground);
                                }
                                if (isSoSFile(nextInfo)){
                                    //紧急视频 为红色区域色块
                                    canvas.drawRect(nextStartX,startY, startX, getSosHeight(), paintSos);
                                }
                                startX = nextStartX;
                                startInterval = nextStartInterval;
                            }

                        }

                    }

                    if (i <= 0) {
                        canvas.drawLine(startX,startY, startX, getHeight(), paintEndLine);
                    }

                }



            }
//            while (i < calstuff.size()) {
//
//                VideoInfo info = calstuff.get(i);
//                //获取视频文件的开始时间戳和结束时间戳
//                long startInterval = info.getStartTime().getTime();
//                long endInterval = info.getEndTime().getTime();
//
//                if (startInterval <= leftInterval && endInterval >= rightInterval) {
//                    long startX = (startInterval - leftInterval)/milliscondsOfIntervalValue();
//                    long endX = (endInterval - leftInterval)/milliscondsOfIntervalValue();
//                    if (info.getFileName().contains("SOS")){
//                        //紧急视频 为红色区域色块
//                        canvas.drawRect(startX,0,endX,getHeight()-dip2px(24),paintRed);
//                    } else {
//                        canvas.drawRect(startX,0,endX,getHeight()-dip2px(24),paintGreen);
//                    }
//                    break;
//                } else if (endInterval < leftInterval) {
//                    i++;
//                    continue;
//                } else if (startInterval > rightInterval) {
//                    break;
//                }
//
//                boolean drawEndLine = false;
//                boolean getNext = true;
//
//                rangeList.add(info);
//
//                if (i == 0 && (startInterval >= leftInterval && startInterval < rightInterval)) {
//                    long startX = (startInterval - leftInterval)/milliscondsOfIntervalValue();
//                    canvas.drawLine(startX,0, startX, getHeight(), paintWhite);
//                    start_x = startX;
//                }
//
//
//                while (i < calstuff.size()) {
//                    if (endInterval > rightInterval) {
//                        i++;
//                        break;
//                    }
//
//                    if (i == calstuff.size() - 1) {
//                        i++;
//                        break;
//                    }
//
//
//
//                    VideoInfo nextInfo = calstuff.get(i + 1);
//
//                    i++;
//                    //获取视频文件的开始时间戳和结束时间戳
//                    long nextStartInterval = nextInfo.getStartTime().getTime();
//
//                    if (nextStartInterval - endInterval > 5 * 1000) {
//                        drawEndLine = true;
//                        break;
//                    } else {
//                        endInterval = nextInfo.getEndTime().getTime();
//                    }
//
//                }
//
//                if (drawEndLine && (endInterval < rightInterval)) {
//                    long startX = (endInterval - leftInterval)/milliscondsOfIntervalValue();
//                    canvas.drawLine(startX,0, startX, getHeight(), paintWhite);
//                }
//
//                if (startInterval < leftInterval) {
//                    startInterval = leftInterval;
//                }
//                if (endInterval > rightInterval) {
//                    endInterval = rightInterval;
//                }
//
//                if (currentInterval > startInterval && currentInterval < endInterval) {
//                    //将开始和结束时间戳转化为对应的x的位置
//                    long startX = (startInterval - leftInterval)/milliscondsOfIntervalValue();
//                    long endX = (endInterval - leftInterval)/milliscondsOfIntervalValue();
//                    //普通的为绿色
//                    canvas.drawRect(startX,0,endX,getHeight()-dip2px(24),paintGreen);
//                }
//
//
//                if (!rangeList.isEmpty()) {
//                    for (VideoInfo videoInfo : rangeList) {
//                        //获取视频文件的开始时间戳和结束时间戳
//                        long start = videoInfo.getStartTime().getTime();
//                        long end = videoInfo.getEndTime().getTime();
//                        long x_start = (start - leftInterval)/milliscondsOfIntervalValue();
//                        long x_end = (end - leftInterval)/milliscondsOfIntervalValue();
//
//                        //紧急视频 为红色区域色块
//                        canvas.drawRect(x_start,0, x_end,getHeight()-dip2px(24),paintRed);
//
//                    }
//                    rangeList.clear();
//                }
//
//
//            }



//            for (int i = 0;i<calstuff.size();i++){
//                VideoInfo info = calstuff.get(i);
//                //获取视频文件的开始时间戳和结束时间戳
//                long startInterval = info.getStartTime().getTime();
//                long endInterval = info.getEndTime().getTime();
//
//                //判断是否需要绘制
//                if ((startInterval > leftInterval && startInterval < rightInterval)
//                        || (endInterval > leftInterval && endInterval < rightInterval)
//                        || (startInterval < leftInterval && endInterval > rightInterval)){
//                    //将开始和结束时间戳转化为对应的x的位置
//                    long startX = (startInterval - leftInterval)/milliscondsOfIntervalValue();
//                    long endX = (endInterval - leftInterval)/milliscondsOfIntervalValue();
//                    if (info.getFileName().contains("SOS")){
//                        //紧急视频 为红色区域色块
//                        canvas.drawRect(startX,0,endX,getHeight()-dip2px(24),paintRed);
//                    }else {
//                        //普通的为绿色
//                        canvas.drawRect(startX,0,endX,getHeight()-dip2px(24),paintGreen);
//                    }
//                }
//
////                Dbug.e("====>", "" + info.getStartTime().getTimeInMillis());
//            }
        }
        //画刻度线
        while (x >= 0 && x<= getWidth()){
            int a;          //长刻度线间隔所代表的时间长度,用于计算,单位是毫秒
            if (scaleType == SCALE_TYPE_BIG){
                a= 60000 * 6;
            }else {
                a = 60000;
            }
            long rem = interval % (a * 5);
            //根据时间戳值对大刻度间隔是否整除判断画长刻度或者短刻度
            if (rem != 0){//小刻度
                canvas.drawLine(x,getHeight() - dip2px(5),x,getHeight(), paintIntervalLine);
            }else {//大刻度
                canvas.drawLine(x,getHeight() - dip2px(10),x,getHeight(), paintIntervalLine);
                //大刻度绘制时间文字
//                String time = formatterScale.format(interval);
//                canvas.drawText(time,x,getHeight() - dip2px(12), paintIntervalLine);
            }
            //下一个刻度
            x = x + intervalValue;
            interval = interval + a;
        }
        //画中间线
        canvas.drawLine(0,dip2px(10), getWidth(), dip2px(10), paintLine);
        canvas.drawLine(centerX,0,centerX,getHeight(), paintCenter);
        canvas.drawRect(centerX - dip2px(5), 0, centerX + dip2px(5),dip2px(10), paintCenter);
        Path path =new Path();
        path.moveTo(centerX - dip2px(5), dip2px(10));
        path.lineTo(centerX + dip2px(5), dip2px(10));
        path.lineTo(centerX,dip2px(15));
        canvas.drawPath(path, paintCenter);


        canvas.drawLine(0,getHeight(), getWidth(), getHeight(), paintLine);

    }

    //通过onTouchEvent来实现拖动和缩放
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean actionUp = false;
        switch (event.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:{
                AppLog.e("touch","ACTION_DOWN" + event.getX());
                point = 1;
                moveStartX = event.getX();
            }break;
            case MotionEvent.ACTION_POINTER_DOWN:{
                AppLog.e("touch","ACTION_POINTER_DOWN" + event.getX(0) + "-----" + event.getX(1));
//                point = point + 1;
//                if (point == 2){
//                    scaleValue = Math.abs(event.getX(1) - event.getX(0));
//                }
            }break;
            case MotionEvent.ACTION_MOVE:{
                AppLog.e("touch","ACTION_MOVE");
                if (point == 1){
                    //拖动
                    currentInterval = currentInterval - milliscondsOfIntervalValue() * ((long) (event.getX() -
                            moveStartX));
                    VideoInfo first = calstuff.get(0);
                    if (currentInterval < first.getStartTime().getTime()) {
                        currentInterval = first.getStartTime().getTime();
                    }

                    VideoInfo last = calstuff.get(calstuff.size() -1);
                    if (currentInterval > last.getEndTime().getTime()) {
                        currentInterval = last.getEndTime().getTime();
                    }

                    if (intervalCalstuff != null) {
                        for (VideoInfo info : intervalCalstuff) {
                            long start = info.getStartTime().getTime();
                            long end = info.getEndTime().getTime();
                            if (currentInterval > start && currentInterval < end) {
                                if (event.getX() - moveStartX > 0) {
                                    currentInterval = start;
                                } else {
                                    currentInterval = end;
                                }

                            }
                        }
                    }
                    moveStartX = event.getX();
                }else if (point == 2){
                    float value = Math.abs(event.getX(1) - event.getX(0));

                    if (scaleType == SCALE_TYPE_BIG){
                        if (scaleValue - value < 0){//变大
                            intervalValue = intervalValue + ((int) ((value - scaleValue)/dip2px(100)));
                            if (intervalValue >= intDip2px(15)){
                                scaleType = SCALE_TYPE_SMALL;
                                intervalValue = intDip2px(10);
                            }
                        }else {//变小
                            intervalValue = intervalValue + ((int) ((value - scaleValue)/dip2px(100)));
                            if (intervalValue < intDip2px(10)){
                                intervalValue = intDip2px(10);
                            }
                        }
                    }else {
                        if (scaleValue - value < 0){//变大
                            intervalValue = intervalValue + ((int) ((value - scaleValue)/dip2px(100)));
                            if (intervalValue >= intDip2px(15)){
                                intervalValue = intDip2px(15);
                            }
                        }else {//变小
                            intervalValue = intervalValue + ((int) ((value - scaleValue)/dip2px(100)));
                            if (intervalValue < intDip2px(10)){
                                scaleType = SCALE_TYPE_BIG;
                                intervalValue = intDip2px(10);
                            }
                        }
                    }
                }else {
                    return true;
                }
            }break;
            case MotionEvent.ACTION_POINTER_UP:{
                AppLog.e("touch","ACTION_POINTER_UP");
                point = point - 1;
            }break;
            case MotionEvent.ACTION_UP:{
                AppLog.e("touch","ACTION_UP");
                point = 0;
                actionUp = true;
                //拖动结束  这里应该有Bug没有区分移动可缩放状态 不过影响不大
//                if (listener != null){
//                    listener.didMoveToDate(formatterProject.format(currentInterval));
//                }
            }break;
        }

        currentI = getCurrentVideoInfoIndex(currentInterval);
        if (listener != null){
            listener.didMoveToDate(currentInterval, currentI, true);
        }

        if (actionUp) {
            if (listener != null){
                listener.didMoveToUp(currentInterval, currentI);
            }
        }
        //重新绘制
        invalidate();
        return true;
    }

    //所有暴露的刷新方法使用不当会引起崩溃(在时间轴创建之后但是没有显示的时候调用),解决办法是使用handel来调用该方法
    //刷新,重新绘制
    public void refresh(){
        invalidate();
    }

    //刷新到当前时间
    public void refreshNow(){
        if (onLock || point != 0){
            return;
        }
        timeNow();
        refresh();
    }

    //移动到某时间  传入参数格式举例 20170918120000
    public void moveTodate(String timeStr, boolean showThumbnail){
        if (onLock || point != 0){
            return;
        }
        try {
            currentInterval = formatterProject.parse(timeStr).getTime();
            currentI = getCurrentVideoInfoIndex(currentInterval);
            invalidate();
            if (listener != null){
                listener.didMoveToDate(currentInterval, currentI, showThumbnail);
            }
            if (listener != null){
                listener.didMoveToUp(currentInterval, currentI);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    //移动到某时间 传入时间戳
    public void moveTodate(long timeInterval, boolean showThumbnail){
        if (onLock || point != 0){
            return;
        }
        if (timeInterval == 0)return;
        currentInterval = timeInterval;
        if (intervalCalstuff != null) {
            for (VideoInfo info : intervalCalstuff) {
                long start = info.getStartTime().getTime();
                long end = info.getEndTime().getTime();
                if (currentInterval > start && currentInterval < end) {
                    currentInterval = end;
                }
            }
        }
        if (currentInterval > calstuff.get(calstuff.size() - 1).getEndTime().getTime()) {
            currentInterval = calstuff.get(calstuff.size() - 1).getEndTime().getTime();
        } else if (currentInterval < calstuff.get(0).getStartTime().getTime()) {
            currentInterval = calstuff.get(0).getStartTime().getTime();
        }
        currentI = getCurrentVideoInfoIndex(currentInterval);
        if (listener != null){
            listener.didMoveToDate(currentInterval, currentI, showThumbnail);
        }

        if (listener != null){
            listener.didMoveToUp(currentInterval, currentI);
        }
        invalidate();
    }

    public void moveTodate2(long timeInterval, boolean showThumbnail){
        if (onLock || point != 0){
            return;
        }
        if (timeInterval == 0)return;
        currentInterval = timeInterval;
        if (intervalCalstuff != null) {
            for (VideoInfo info : intervalCalstuff) {
                long start = info.getStartTime().getTime();
                long end = info.getEndTime().getTime();
                if (currentInterval > start && currentInterval < end) {
                    currentInterval = end;
                }
            }
        }
        if (currentInterval > calstuff.get(calstuff.size() - 1).getEndTime().getTime()) {
            currentInterval = calstuff.get(calstuff.size() - 1).getEndTime().getTime();
        } else if (currentInterval < calstuff.get(0).getStartTime().getTime()) {
            currentInterval = calstuff.get(0).getStartTime().getTime();
        }
        currentI = getCurrentVideoInfoIndex(currentInterval);
        if (listener != null){
            listener.didMoveToDate(currentInterval, currentI, showThumbnail);
        }

//        if (listener != null){
//            listener.didMoveToUp(currentInterval, currentI);
//        }
        invalidate();
    }

    //获取当前时间轴指向的时间 返回参数格式举例 20170918120000
    public String currentTimeStr(){
        return formatterProject.format(currentInterval);
    }

    //锁定,不可拖动和缩放
    public void lockMove(){
        onLock = true;
    }

    //解锁,可以拖动和缩放
    public void unLockMove(){
        onLock = false;
    }

    //获取当前时间轴指向的时间的时间戳
    public long getCurrentInterval(){
        return currentInterval;
    }

    public int getCurrentIndex() {
        return currentI;
    }
    //把时间数据转化为时间戳
    public long timeIntervalFromStr(String str){
        try {
            return formatterProject.parse(str).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
    //把时间戳转化为时间字符串
    public String timeStrFromInterval(long interval){
        return formatterProject.format(interval);
    }

    //写入视频数据
    public void setCalstuff(List<VideoInfo> mcalstuff) {
        this.calstuff = mcalstuff;
        if (intervalCalstuff == null) {
            intervalCalstuff = new ArrayList<>();
        }
        for(int i = 0; i < mcalstuff.size(); i++) {
            VideoInfo info = mcalstuff.get(i);
            if (i + 1 >= mcalstuff.size()){
                break;
            }
            VideoInfo nextInfo = mcalstuff.get(i+1);
            long end = info.getEndTime().getTime();
            long nextStart = nextInfo.getStartTime().getTime();
            if (nextStart - end > 5 * 1000) {
                VideoInfo intervalInfo = new VideoInfo("test", "test", new Date(end), new Date(nextStart));
                intervalCalstuff.add(intervalInfo);
            }
        }
        currentI = getCurrentVideoInfoIndex(currentInterval);
        refresh();
    }

    public int getCurrentVideoInfoIndex(long currentInterval) {
        if (calstuff == null) {
            return -1;
        }
        for (int i = 0; i < calstuff.size(); i++) {
            VideoInfo videoInfo = calstuff.get(i);
            if (videoInfo.getStartTime().getTime() <= currentInterval && videoInfo.getEndTime().getTime() >= currentInterval) {
                return i;
            }
        }
        return -1;
    }

    //清除视频信息
    public void clearVideoInfos(){
        this.calstuff = null;
        this.intervalCalstuff = null;
        refresh();
    }
}
