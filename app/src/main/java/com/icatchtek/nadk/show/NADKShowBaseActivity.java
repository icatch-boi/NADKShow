package com.icatchtek.nadk.show;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.githang.statusbar.StatusBarCompat;
import com.icatchtek.basecomponent.activitymanager.BaseActivity;

/**
 * Created by sha.liu on 2023/8/10.
 */
public class NADKShowBaseActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setStatusBarColor(this, this.getResources().getColor(R.color.colorPrimaryDark), false);
    }
}
