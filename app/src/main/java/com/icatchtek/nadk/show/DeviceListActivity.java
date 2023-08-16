package com.icatchtek.nadk.show;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.icatchtek.basecomponent.prompt.MyProgressDialog;
import com.icatchtek.basecomponent.utils.ClickUtils;
import com.icatchtek.baseutil.ThreadPoolUtils;
import com.icatchtek.baseutil.log.AppLog;
import com.icatchtek.nadk.reliant.NADKSignalingType;
import com.icatchtek.nadk.show.device.DeviceManager;
import com.icatchtek.nadk.show.device.NADKLocalDevice;
import com.icatchtek.nadk.show.devicelist.LocalDeviceListView;
import com.icatchtek.nadk.show.ssdp.SSDPSearchResponse;
import com.icatchtek.nadk.show.ssdp.SearchDeviceThread;
import com.icatchtek.nadk.show.ssdp.SearchUtils;
import com.icatchtek.nadk.show.utils.NADKConfig;
import com.tinyai.libmediacomponent.components.cameralist.DeviceItem;
import com.tinyai.libmediacomponent.components.cameralist.ItemContentClickListener;;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class DeviceListActivity extends NADKShowBaseActivity {
    private static final String TAG = DeviceListActivity.class.getSimpleName();
    private Handler handler = new Handler();

    private RelativeLayout device_list_layout;
    private RelativeLayout top_bar_layout;
    private ImageButton back_btn;
    private ImageView search_btn;
    private LocalDeviceListView deviceListView;
    private List<DeviceItem> deviceItemList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        back_btn = findViewById(R.id.back_btn);
        search_btn = findViewById(R.id.search_btn);
        top_bar_layout = findViewById(R.id.toolbar_layout);
        device_list_layout = findViewById(R.id.device_list_layout);



        initDeviceList();


        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ClickUtils.isFastDoubleClick(v)){
                    AppLog.i(TAG, "isFastDoubleClick the v.id=" + v.getId());
                    return;
                }
                finish();
            }
        });

        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ClickUtils.isFastDoubleClick(v)){
                    AppLog.i(TAG, "isFastDoubleClick the v.id=" + v.getId());
                    return;
                }
                search();
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onDestroy() {
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
                break;
            case KeyEvent.KEYCODE_BACK:
                AppLog.d(TAG, "back");
                finish();
                break;
        }
        return true;
    }





    private void initDeviceList() {
        deviceListView = findViewById(R.id.device_list_view);
        deviceItemList = new LinkedList<>();
        DeviceManager.getInstance().reloadDevice(new LinkedList<>());
        RelativeLayout setting_layout = deviceListView.findViewById(R.id.setting_layout);
        if (setting_layout != null) {
            setting_layout.setVisibility(View.GONE);
        }
//        deviceListView.findViewById(R.id.delete_layout).setVisibility(View.GONE);
//        deviceListView.findViewById(R.id.album_layout).setVisibility(View.GONE);
//        deviceListView.findViewById(R.id.ip_txv).setVisibility(View.GONE);

//        deviceListView.setRefreshListener(new LocalDeviceListView.RefreshCallback() {
//            @Override
//            public List<DeviceItem> refreshData() {
//                return loadList();
//            }
//        });

        deviceListView.setItemContentClickListener(new ItemContentClickListener() {
            @Override
            public void onPreviewClick(DeviceItem deviceItem) {
                NADKLocalDevice device = DeviceManager.getInstance().getDevice(deviceItem.getId());
                if (device != null) {
                    NADKConfig.getInstance().setLanModeAuthorization(device.getNADKAuthorization());
                    NADKConfig.getInstance().serializeConfig();

                }
                Intent intent = new Intent(DeviceListActivity.this, LiveViewActivity.class);
                intent.putExtra("signalingType", NADKSignalingType.NADK_SIGNALING_TYPE_BASE_TCP);
                startActivity(intent);

            }

            @Override
            public void onDeleteClick(DeviceItem deviceItem) {

            }

            @Override
            public void onMediaClick(DeviceItem deviceItem) {
                NADKLocalDevice device = DeviceManager.getInstance().getDevice(deviceItem.getId());
                if (device != null) {
                    NADKConfig.getInstance().setLanModeAuthorization(device.getNADKAuthorization());
                    NADKConfig.getInstance().serializeConfig();

                }
                Intent intent = new Intent(DeviceListActivity.this, LocalPlaybackActivity.class);
                intent.putExtra("signalingType", NADKSignalingType.NADK_SIGNALING_TYPE_BASE_TCP);
                startActivity(intent);

            }

            @Override
            public void onShareClick(DeviceItem deviceItem) {


            }

            @Override
            public void onMessageClick(DeviceItem deviceItem) {

            }

            @Override
            public void onSettingClick(DeviceItem deviceItem) {

            }

            @Override
            public void onWifiSetingClick(DeviceItem deviceItem) {

            }
        });

        deviceListView.enableRefresh(false);

        search();

    }


    private List<DeviceItem> loadList() {
        List<DeviceItem> deviceItemList = new LinkedList<>();
        DeviceItem item = new DeviceItem("192.168.0.100", new Date(), null, null, new Date(), "192.168.0.100", true, "192.168.0.100");
        DeviceItem item2 = new DeviceItem("192.168.0.101", new Date(), null, null, new Date(), "192.168.0.101", true, "192.168.0.101");
        deviceItemList.add(item);
        deviceItemList.add(item2);
        return deviceItemList;
    }

//    private void search(long timeout_ms) {
//        ThreadPoolUtils.getInstance().executorNetThread(new Runnable() {
//            @Override
//            public void run() {
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        MyProgressDialog.showProgressDialog(DeviceListActivity.this);
//                    }
//                });
//                List<SSDPSearchResponse> responseList = SearchUtils.searchDevice(timeout_ms);
//                if (responseList != null) {
//                    List<NADKLocalDevice> localDevices = convertToNADKLocalDeviceList(responseList);
//                    DeviceManager.getInstance().reloadDevice(localDevices);
//                    deviceItemList = convertToDeviceItemList(localDevices);
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            deviceListView.renderList(deviceItemList);
//                            MyProgressDialog.closeProgressDialog();
//                        }
//                    });
//                }
//
//            }
//        }, 200);
//
//    }

    private void search() {
        search(2000);
    }

    private void search(long timeout_ms) {
        ThreadPoolUtils.getInstance().executorNetThread(new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyProgressDialog.showProgressDialog(DeviceListActivity.this);
                    }
                });
                List<SSDPSearchResponse> responseList = SearchUtils.searchDevice(timeout_ms, new SearchDeviceThread.SSDPSearchResponseListener() {
                    @Override
                    public void notify(SSDPSearchResponse response) {
                        if (DeviceManager.getInstance().getDevice(response.getIP()) == null) {
                            NADKLocalDevice device = convertToNADKLocalDevice(response);
                            DeviceManager.getInstance().addDevice(device);
                            deviceItemList = convertToDeviceItemList(DeviceManager.getInstance().getDeviceList());
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                deviceListView.renderList(deviceItemList);
                                MyProgressDialog.closeProgressDialog();
                            }
                        });
                    }
                });

//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        MyProgressDialog.closeProgressDialog();
//                    }
//                });
                if (responseList != null) {
                    List<NADKLocalDevice> localDevices = convertToNADKLocalDeviceList(responseList);
                    DeviceManager.getInstance().reloadDevice(localDevices);
                    deviceItemList = convertToDeviceItemList(DeviceManager.getInstance().getDeviceList());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            deviceListView.renderList(deviceItemList);
                            MyProgressDialog.closeProgressDialog();
                        }
                    });
                }

            }
        }, 200);

    }

    private NADKLocalDevice convertToNADKLocalDevice(SSDPSearchResponse response) {
        return new NADKLocalDevice(response.getIP(), response.getIP(), response.getIP(), NADKLocalDevice.LOCAL_SIGNALING_DEFAULT_PORT, response.getMac(), NADKLocalDevice.DEFAULT_CHANNEL_NAME);
    }

    private DeviceItem convertToDeviceItem(NADKLocalDevice device) {
        return new DeviceItem(device.getDeviceName(), new Date(), null, null, new Date(), device.getDeviceId(), true, device.getIp());
    }

    private List<NADKLocalDevice> convertToNADKLocalDeviceList(List<SSDPSearchResponse> responseList) {
        List<NADKLocalDevice> deviceList = new LinkedList<>();
        for(SSDPSearchResponse response : responseList) {
            deviceList.add(convertToNADKLocalDevice(response));
        }
        return deviceList;
    }

    public List<DeviceItem> convertToDeviceItemList(List<NADKLocalDevice> deviceList) {
        List<DeviceItem> deviceItemList = new LinkedList<>();
        for (NADKLocalDevice device : deviceList) {
            deviceItemList.add(convertToDeviceItem(device));
        }
        return deviceItemList;
    }



}