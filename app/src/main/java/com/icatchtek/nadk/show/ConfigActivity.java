package com.icatchtek.nadk.show;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.icatchtek.nadk.show.utils.NADKConfig;
import com.icatchtek.nadk.webrtc.assist.NADKAuthorization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ConfigActivity extends AppCompatActivity {
    private static final String TAG = ConfigActivity.class.getSimpleName();
    private static final String[] supportOptions = {"AWS_KVS_WEBRTC", "AWS_KVS_STREAM", "TINYAI_RTC", "LAN_MODE", "DEBUG"};
    private static final String[] booleanOptions = {"true", "false"};
    private ImageButton back_btn;
    private Spinner option_spinner;
    private LinearLayout auth_info_layout;
    private EditText channelname_edt;
    private EditText region_edt;
    private LinearLayout region_layout;
    private EditText endpoint_edt;
    private LinearLayout endpoint_layout;
    private EditText accesskey_edt;
    private LinearLayout accesskey_layout;
    private EditText secretkey_edt;
    private LinearLayout secretkey_layout;
    private EditText clientid_edt;
    private LinearLayout clientid_layout;
    private LinearLayout debug_info_layout;
    private Spinner srtp_spinner;
    private Spinner rtcptwcc_spinner;
    private Button save_btn;
    private List<String> supportOptionList;
    private String currentOption = supportOptions[0];
    private int currentOptionIndex = 0;
    private Handler handler = new Handler();
    private boolean srtp = true;
    private boolean rtcptwcc = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        back_btn = findViewById(R.id.back_btn);
        option_spinner = findViewById(R.id.option_spinner);
        auth_info_layout = findViewById(R.id.auth_info_layout);
        channelname_edt = findViewById(R.id.channelname_edt);
        region_edt = findViewById(R.id.region_edt);
        region_layout = findViewById(R.id.region_layout);
        endpoint_edt = findViewById(R.id.endpoint_edt);
        endpoint_layout = findViewById(R.id.endpoint_layout);
        accesskey_edt = findViewById(R.id.accesskey_edt);
        accesskey_layout = findViewById(R.id.accesskey_layout);
        secretkey_edt = findViewById(R.id.secretkey_edt);
        secretkey_layout = findViewById(R.id.secretkey_layout);
        clientid_edt = findViewById(R.id.clientid_edt);
        clientid_layout = findViewById(R.id.clientid_layout);
        debug_info_layout = findViewById(R.id.debug_info_layout);
        srtp_spinner = findViewById(R.id.srtp_spinner);
        rtcptwcc_spinner = findViewById(R.id.rtcptwcc_spinner);
        save_btn = findViewById(R.id.save_btn);


        updateOptionSpinner();
        updateSrtpSpinner();
        updateRtcptwccSpinner();

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentOption();
            }
        });


    }

    private void updateOptionSpinner() {
        supportOptionList = new ArrayList<>(Arrays.asList(supportOptions));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, supportOptionList);
//        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        option_spinner.setAdapter(adapter);
        for (int i = 0; i < supportOptionList.size(); i++) {
            String service = supportOptionList.get(i);
            if (service.equals(currentOption)) {
                currentOptionIndex = i;
                option_spinner.setSelection(i);
                break;
            }
        }

        option_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                currentOptionIndex = position;
                currentOption = supportOptionList.get(position);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateCurrentOption();
                    }
                });


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void updateSrtpSpinner() {
        ArrayList<String> supportOptionList = new ArrayList<>(Arrays.asList(booleanOptions));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, supportOptionList);
//        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        srtp_spinner.setAdapter(adapter);
        if (srtp) {
            srtp_spinner.setSelection(0);
        } else {
            srtp_spinner.setSelection(1);
        }

        srtp_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    srtp = true;
                } else {
                    srtp = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void updateRtcptwccSpinner() {
        ArrayList<String> supportOptionList = new ArrayList<>(Arrays.asList(booleanOptions));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, supportOptionList);
//        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        rtcptwcc_spinner.setAdapter(adapter);
        if (rtcptwcc) {
            rtcptwcc_spinner.setSelection(0);
        } else {
            rtcptwcc_spinner.setSelection(1);
        }

        rtcptwcc_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    rtcptwcc = true;
                } else {
                    rtcptwcc = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void updateCurrentOption() {
        if (currentOptionIndex == 4) {
            srtp = NADKConfig.getInstance().getSrtp();
            if (srtp) {
                srtp_spinner.setSelection(0);
            } else {
                srtp_spinner.setSelection(1);
            }
            rtcptwcc = NADKConfig.getInstance().getRtcpTwcc();
            if (rtcptwcc) {
                rtcptwcc_spinner.setSelection(0);
            } else {
                rtcptwcc_spinner.setSelection(1);
            }

            auth_info_layout.setVisibility(View.GONE);
            debug_info_layout.setVisibility(View.VISIBLE);
        } else {
            NADKAuthorization authorization = null;
            if (currentOptionIndex == 0) {
                authorization = NADKConfig.getInstance().getAWSKVSWebrtcAuthorization();
                endpoint_layout.setVisibility(View.GONE);
                clientid_layout.setVisibility(View.VISIBLE);
                region_layout.setVisibility(View.VISIBLE);
                accesskey_layout.setVisibility(View.VISIBLE);
                secretkey_layout.setVisibility(View.VISIBLE);
            } else if (currentOptionIndex == 1) {
                authorization = NADKConfig.getInstance().getAWSKVSStreamAuthorization();
                endpoint_layout.setVisibility(View.GONE);
                clientid_layout.setVisibility(View.GONE);
                region_layout.setVisibility(View.VISIBLE);
                accesskey_layout.setVisibility(View.VISIBLE);
                secretkey_layout.setVisibility(View.VISIBLE);
            } else if (currentOptionIndex == 2) {
                authorization = NADKConfig.getInstance().getTinyaiRtcAuthorization();
                endpoint_layout.setVisibility(View.VISIBLE);
                clientid_layout.setVisibility(View.VISIBLE);
                region_layout.setVisibility(View.VISIBLE);
                accesskey_layout.setVisibility(View.VISIBLE);
                secretkey_layout.setVisibility(View.VISIBLE);
            } else if (currentOptionIndex == 3) {
                authorization = NADKConfig.getInstance().getLanModeAuthorization();
                endpoint_layout.setVisibility(View.VISIBLE);
                clientid_layout.setVisibility(View.VISIBLE);
                region_layout.setVisibility(View.GONE);
                accesskey_layout.setVisibility(View.GONE);
                secretkey_layout.setVisibility(View.GONE);
            }

            String channelName = authorization.getChannelName();
            String region = authorization.getRegion();
            String endpoint = authorization.getEndpoint();
            String accesskey = authorization.getAccessKey();
            String secretkey = authorization.getSecretKey();
            String clientid = authorization.getClientId();

            channelname_edt.setText(channelName);
            region_edt.setText(region);
            endpoint_edt.setText(endpoint);
            accesskey_edt.setText(accesskey);
            secretkey_edt.setText(secretkey);
            clientid_edt.setText(clientid);

            auth_info_layout.setVisibility(View.VISIBLE);
            debug_info_layout.setVisibility(View.GONE);
        }



    }

    private void saveCurrentOption() {

        if (currentOptionIndex == 4) {
            NADKConfig.getInstance().setSrtp(srtp);
            NADKConfig.getInstance().setRtcpTwcc(rtcptwcc);
        } else {
            String channelName = channelname_edt.getText().toString();
            String region = region_edt.getText().toString();
            String endpoint = endpoint_edt.getText().toString();
            String accesskey = accesskey_edt.getText().toString();
            String secretkey = secretkey_edt.getText().toString();
            String clientid = clientid_edt.getText().toString();

            NADKAuthorization authorization = new NADKAuthorization(channelName, region, endpoint, accesskey, secretkey, clientid);
            if (currentOptionIndex == 0) {
                NADKConfig.getInstance().setAWSKVSWebrtcAuthorization(authorization);
            } else if (currentOptionIndex == 1) {
                NADKConfig.getInstance().setAWSKVSStreamAuthorization(authorization);
            } else if (currentOptionIndex == 2) {
                NADKConfig.getInstance().setTinyaiRtcAuthorization(authorization);
            } else if (currentOptionIndex == 3) {
                NADKConfig.getInstance().setLanModeAuthorization(authorization);
            }
        }

        NADKConfig.getInstance().serializeConfig();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        NADKConfig.release();
    }
}