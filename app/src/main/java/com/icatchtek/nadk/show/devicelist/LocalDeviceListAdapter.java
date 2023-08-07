package com.icatchtek.nadk.show.devicelist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.icatchtek.basecomponent.utils.ClickUtils;
import com.icatchtek.baseutil.imageloader.ImageLoaderUtil;
import com.icatchtek.baseutil.info.SystemInfo;
import com.icatchtek.baseutil.log.AppLog;
import com.icatchtek.nadk.show.R;
import com.tinyai.libmediacomponent.components.cameralist.DeviceItem;
import com.tinyai.libmediacomponent.components.cameralist.DeviceItemProperty;
import com.tinyai.libmediacomponent.components.cameralist.DeviceType;
import com.tinyai.libmediacomponent.components.cameralist.ItemContentClickListener;

import java.util.List;


public class LocalDeviceListAdapter extends BaseAdapter {
    private ItemContentClickListener itemContentClickListener;
    private String TAG = LocalDeviceListAdapter.class.getSimpleName();
    private Context context;
    private List<DeviceItem> list;
    private Handler handler;
    private int maxCoverSize = 60; //60KB
    private AttributeSet attrs;
    private DeviceItemProperty deviceItemProperty;
    private int deviceOnlineIcon = R.drawable.meida_online;
    private int deviceOfflineIcon = R.drawable.meida_offline;


    public LocalDeviceListAdapter(Context context, final List<DeviceItem> list, DeviceItemProperty deviceItemProperty) {
        this.context = context;
        this.list = list;
        this.handler = new Handler();
        this.deviceItemProperty = deviceItemProperty;
    }

   public void setItemContentClickListener(ItemContentClickListener controlListener){
       itemContentClickListener = controlListener;

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.media_local_device_item, null);
        } else {
            view = convertView;
        }

        if (position >= list.size()) {
            return view;
        }
        final DeviceItem camera = list.get(position);
        final ImageView pvImageView = (ImageView) view.findViewById(R.id.camera_pv_image);
        TextView mTextView = (TextView) view.findViewById(R.id.name_txv);
        TextView lastPvTimeTxv = (TextView) view.findViewById(R.id.last_preview_time);
        mTextView.setText(camera.getName());
        ImageButton playBtn = (ImageButton) view.findViewById(R.id.play_btn);
        LinearLayout albumLayout = (LinearLayout) view.findViewById(R.id.album_layout);
        LinearLayout shareLayout = (LinearLayout) view.findViewById(R.id.share_layout);
        LinearLayout deleteLayout = (LinearLayout) view.findViewById(R.id.delete_layout);
        LinearLayout wifiLayout = (LinearLayout) view.findViewById(R.id.wifi_layout);
        RelativeLayout settingLayout = (RelativeLayout) view.findViewById(R.id.setting_layout);
        RelativeLayout messageLayout = (RelativeLayout) view.findViewById(R.id.message_layout);
        ImageView newMessageIcon = (ImageView) view.findViewById(R.id.message_new);
        ImageView newHWVersionIcon = (ImageView) view.findViewById(R.id.hwversion_new);
        RelativeLayout previewContainerLayout = (RelativeLayout) view.findViewById(R.id.preview_container);
        ImageView shareImv = view.findViewById(R.id.share_imv);
        ImageView wifiImv = view.findViewById(R.id.wifi_imv);
        ImageView deviceStatus = view.findViewById(R.id.device_status);

        ImageView messageBtn = view.findViewById(R.id.message_btn);
        ImageView albumBtn = view.findViewById(R.id.album_btn);
        ImageView shareBtn = view.findViewById(R.id.share_imv);
        ImageView deleteBtn = view.findViewById(R.id.delete_btn);
        ImageView settingBtn = view.findViewById(R.id.setting_imv);
        TextView ipTxv = view.findViewById(R.id.ip_txv);

        if(deviceItemProperty != null){
            int device_online_icon = deviceItemProperty.getDevice_online_icon();
            if(device_online_icon != -1){
                this.deviceOnlineIcon = device_online_icon;
            }

            int device_offline_icon = deviceItemProperty.getDevice_offline_icon();
            if(device_offline_icon != -1){
                this.deviceOfflineIcon = device_offline_icon;
            }

            int message_icon = deviceItemProperty.getMessage_icon();
            if(message_icon != -1){
                messageBtn.setBackgroundResource(message_icon);
            }

            int file_icon = deviceItemProperty.getFile_icon();
            if(file_icon != -1){
                albumBtn.setBackgroundResource(file_icon);
            }

            int share_icon = deviceItemProperty.getShare_icon();
            if(share_icon != -1){
                shareBtn.setBackgroundResource(share_icon);
            }

            int delete_icon = deviceItemProperty.getDelete_icon();
            if(delete_icon != -1){
                deleteBtn.setBackgroundResource(delete_icon);
            }

            int setting_icon = deviceItemProperty.getSetting_icon();
            if(setting_icon != -1){
                settingBtn.setBackgroundResource(setting_icon);
            }
        }


        ViewGroup.LayoutParams params = previewContainerLayout.getLayoutParams();
        params.width = SystemInfo.getScreenWidth(context) - 10 * 2;
        params.height = params.width * 9 / 16;
        previewContainerLayout.setLayoutParams(params);
        byte[] thumb = camera.getCoverData();
        if (thumb != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(thumb, 0, thumb.length);
            pvImageView.setImageBitmap(bitmap);
        }

        String url = camera.getCoverUrl();
        if(url != null && !url.isEmpty()){
//            ImageLoaderUtil.loadImageView(url,pvImageView);
            ImageLoaderUtil.loadImageView(url, pvImageView, 0, new ImageLoaderUtil.MyLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view) {
                    pvImageView.setImageDrawable(null);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                    byte[] cover = BitmapTools.qualityCompress(loadedImage, maxCoverSize);
//                    camera.updatePvThumb(cover);
                }
            });
        }
        if (itemContentClickListener != null) {
            albumLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ClickUtils.isFastDoubleClick(view)) {
                        AppLog.i(TAG, "isFastDoubleClick the v.id=" + view.getId());
                        return;
                    }
                    itemContentClickListener.onMediaClick(camera);
                }
            });
            shareLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ClickUtils.isFastDoubleClick(view)) {
                        AppLog.i(TAG, "isFastDoubleClick the v.id=" + view.getId());
                        return;
                    }
                    itemContentClickListener.onShareClick(camera);
                }
            });

            deleteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ClickUtils.isFastDoubleClick(view)) {
                        AppLog.i(TAG, "isFastDoubleClick the v.id=" + view.getId());
                        return;
                    }
                    itemContentClickListener.onDeleteClick(camera);
                }
            });

            wifiLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ClickUtils.isFastDoubleClick(view)) {
                        AppLog.i(TAG, "isFastDoubleClick the v.id=" + view.getId());
                        return;
                    }
                    itemContentClickListener.onWifiSetingClick(camera);
                }
            });

            playBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ClickUtils.isFastDoubleClick(view)) {
                        AppLog.i(TAG, "isFastDoubleClick the v.id=" + view.getId());
                        return;
                    }
//                    MyProgressDialog.showProgressDialog((Activity) context, R.string.wait);
                    itemContentClickListener.onPreviewClick(camera);
                }
            });

            messageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ClickUtils.isFastDoubleClick(v)) {
                        AppLog.i(TAG, "isFastDoubleClick the v.id=" + v.getId());
                        return;
                    }
                    itemContentClickListener.onMessageClick(camera);
                }
            });
            settingLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemContentClickListener.onSettingClick(camera);
                }
            });
        }
        int deviceType = camera.getDeviceType();
        if(deviceType == DeviceType.TYPE_REMOTE){
            boolean isAdmin = camera.isAdmin();
            if (!isAdmin) {
                shareLayout.setClickable(false);
                shareImv.setEnabled(false);
                wifiLayout.setClickable(false);
                wifiImv.setEnabled(false);
                settingLayout.setVisibility(View.GONE);
            } else {
                shareLayout.setClickable(true);
                shareImv.setEnabled(true);
                wifiLayout.setClickable(true);
                wifiImv.setEnabled(true);
                settingLayout.setVisibility(View.VISIBLE);

                if (camera.isNewSetting()) {
                    newHWVersionIcon.setVisibility(View.VISIBLE);
                } else {
                    newHWVersionIcon.setVisibility(View.GONE);
                }
            }
            if (camera.isNewMessage()) {
                newMessageIcon.setVisibility(View.VISIBLE);
            } else {
                newMessageIcon.setVisibility(View.GONE);
            }

            if (camera.isOnline()) {
                AppLog.i(TAG, "isDeviceOnline: true");
                deviceStatus.setImageResource(deviceOnlineIcon);
            } else {
                AppLog.i(TAG, "isDeviceOnline: false");
                deviceStatus.setImageResource(deviceOfflineIcon);
            }

            ipTxv.setVisibility(View.GONE);
        }else if(deviceType == DeviceType.TYPE_LOCAL){
            messageLayout.setVisibility(View.GONE);
            shareLayout.setVisibility(View.GONE);
            albumLayout.setVisibility(View.VISIBLE);
            deleteLayout.setVisibility(View.GONE);
            settingLayout.setVisibility(View.GONE);
            deviceStatus.setVisibility(View.VISIBLE);

            String ip = camera.getIp();
            if(ip != null && !ip.isEmpty()){
                ipTxv.setVisibility(View.GONE);
                ipTxv.setText(ip);
            }else {
                ipTxv.setVisibility(View.GONE);
            }
        }


        return view;
    }
//    修改CameraItem 中的内容并更新UI
    public void update(int position, DeviceItem camera){
        if(list != null){
            list.set(position,camera);
            notifyDataSetChanged();
        }
    }

//    删除CameraItem 并更新UI
    public void delete(int position, DeviceItem camera){


        if(list != null){
            list.remove(position);
            notifyDataSetChanged();
        }
    }


//新增CameraItem并更新UI
    public void add(DeviceItem camera){
        if(list != null){
            list.add(0,camera);
            notifyDataSetChanged();
        }
    }





}
