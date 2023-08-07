package com.icatchtek.nadk.show.devicelist;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.icatchtek.nadk.show.R;
import com.tinyai.libmediacomponent.components.cameralist.DeviceItem;
import com.tinyai.libmediacomponent.components.cameralist.DeviceItemProperty;
import com.tinyai.libmediacomponent.components.cameralist.ItemContentClickListener;

import java.util.ArrayList;
import java.util.List;

public class LocalDeviceListView extends LinearLayout implements PullToRefreshBase.OnRefreshListener2{
    private static final String TAG = "FileListView";
    private int refresh_mode = 0;
    private PullToRefreshListView deviceFileListView;
    private  List<DeviceItem> deviceFileList = new ArrayList<>();
    private LocalDeviceListAdapter adapter;
    private ItemContentClickListener itemContentClickListener;
    private RefreshCallback refreshListener;
    private DeviceItemProperty deviceItemProperty = new DeviceItemProperty();


    private  AttributeSet attrs;
    private Context context;

    public LocalDeviceListView(Context context) {
        this(context, null);
    }

    public LocalDeviceListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LocalDeviceListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        this.attrs = attrs;
        this.context =context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CameraListView);
        //从布局文件中获取到设置内容
//        this.enableChecked = typedArray.getBoolean(R.styleable.FileListView_enable_checked,true);
//        this.enableDownload = typedArray.getBoolean(R.styleable.FileListView_enable_download,true);
//        this.enableDelete = typedArray.getBoolean(R.styleable.FileListView_enable_delete,false);
//
//        MediaLog.d(TAG,"enableChecked:" +enableChecked + " enableDownload=" +enableDownload+ " enableDelete" + enableDelete);

        //回收
        typedArray.recycle();

        LayoutInflater.from(context).inflate(R.layout.meida_local_device_list_layout, this);
        //获取控件ID
        deviceFileListView = findViewById(R.id.list_view);
        deviceFileListView.getRefreshableView().setSelector(android.R.color.transparent);
        deviceFileListView.setOnRefreshListener(this);
//        setRefreshMode(refresh_mode);
        deviceFileListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

    }

    public void setItemContentClickListener(ItemContentClickListener controlListener){
        this.itemContentClickListener = controlListener;
        if(adapter != null){
            adapter.setItemContentClickListener(itemContentClickListener);
        }
    }

    public  void renderList(List<DeviceItem> list){
        this.deviceFileList.clear();
        this.deviceFileList.addAll(list);
        if (adapter == null) {
            adapter = new LocalDeviceListAdapter(this.context, deviceFileList, deviceItemProperty);
            if(itemContentClickListener != null){
                adapter.setItemContentClickListener(itemContentClickListener);
            }
            deviceFileListView.setAdapter(adapter);
        }else {
            adapter.notifyDataSetChanged();
        }
    }

    public void setRefreshListener(RefreshCallback refreshListener) {
        this.refreshListener = refreshListener;
    }

    public void updateName(String deviceId,String name){
        if(deviceFileList != null){
            DeviceItem item = getItemById(deviceId);
            if(item != null){
                item.setName(name);
                if(adapter != null){
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    public void updateCover(String deviceId,String coverUrl){
        if(deviceFileList != null){
            DeviceItem item = getItemById(deviceId);
            if(item != null){
                item.setCoverUrl(coverUrl);
                if(adapter != null){
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    public void updateCover(String deviceId,byte[] coverData){
        if(deviceFileList != null){
            DeviceItem item = getItemById(deviceId);
            if(item != null){
                item.setCoverData(coverData);
                if(adapter != null){
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    public void updateMessageState(String deviceId,boolean newMessage){
        if(deviceFileList != null){
            DeviceItem item = getItemById(deviceId);
            if(item != null){
                item.setNewMessage(newMessage);
                if(adapter != null){
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    public void updateSettingState(String deviceId,boolean newSetting){
        if(deviceFileList != null){
            DeviceItem item = getItemById(deviceId);
            if(item != null){
                item.setNewSetting(newSetting);
                if(adapter != null){
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    public void updateOnlineState(String deviceId,boolean online){
        if(deviceFileList != null){
            DeviceItem item = getItemById(deviceId);
            if(item != null){
                item.setOnline(online);
                if(adapter != null){
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    public void delete(String deviceId){
        if(deviceFileList != null){
            int index = getItemIndexById(deviceId);
            if(index >= 0 && index < deviceFileList.size()){
                deviceFileList.remove(index);
                if(adapter != null){
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    public void add(DeviceItem camera){
        if(deviceFileList != null){
            deviceFileList.add(0,camera);
            if(adapter != null){
                adapter.notifyDataSetChanged();
            }
        }
    }

    public int getItemIndexById(String id){
        if(deviceFileList != null){
            for (int i = 0;i<deviceFileList.size();i++){
                String tempId = deviceFileList.get(i).getId();
                    if(tempId != null && tempId.equals(id)){
                        return i;
                    }
            }
        }
        return -1;
    }

    public DeviceItem getItemById(String id){
        if(deviceFileList != null){
            for (int i = 0;i<deviceFileList.size();i++){
                String tempId = deviceFileList.get(i).getId();
                if(tempId != null && tempId.equals(id)){
                    return deviceFileList.get(i);
                }
            }
        }
        return null;
    }

    public void enableRefresh(boolean enable){
        if(deviceFileListView != null){
            if(enable){
                deviceFileListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            }else {
                deviceFileListView.setMode(PullToRefreshBase.Mode.DISABLED);
            }

        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase pullToRefreshBase) {
        if(refreshListener!= null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<DeviceItem> list =  refreshListener.refreshData();
                    deviceFileList.addAll(0,list);
                    post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                            refreshComplete();
                        }
                    });
                }
            }).start();
        }
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase pullToRefreshBase) {
        if(refreshListener!= null){
        }
    }


    public interface RefreshCallback {
        List<DeviceItem> refreshData();
    }


    public void refreshComplete() {
        if (deviceFileListView != null) {
            deviceFileListView.onRefreshComplete();
        }
    }
}
