package com.XMBT.bluetooth.le.ui.gbattery;

import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Switch;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.base.BaseActivity;
import com.XMBT.bluetooth.le.bean.YunCheDeviceEntity;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.sp.UserSp;
import com.XMBT.bluetooth.le.utils.LogUtils;
import com.XMBT.bluetooth.le.utils.StatusBarHelper;
import com.XMBT.bluetooth.le.view.TitleBar;
import com.XMBT.bluetooth.le.view.dialog.WarningDialog;
import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Response;

/**
 * 设防控制
 */
public class FortificationActivity extends BaseActivity implements SpeechSynthesizerListener {

    private RadioButton[] btnAry = new RadioButton[2];
    private MyButtonListener myButtonListener;
    private YunCheDeviceEntity device;
    private String defenceStatus = "";
    private String gpsOnOff = "";
    public final String ON = "1";
    public final String OFF = "0";
    public final String SAFFON = "saffon";
    public final String SAFFOFF = "saffoff";

    private LinearLayout llStealth, llFortification;
    private Switch swStealth, swFortification;
    private CheckBox cbStealth, cbFortification;
    private boolean isFirstIn = true;

    private SpeechSynthesizer mSpeechSynthesizer;
    private String mSampleDirPath;
    private static final String SAMPLE_DIR_NAME = "baiduTTS";
    private static final String SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female.dat";
    private static final String SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male.dat";
    private static final String TEXT_MODEL_NAME = "bd_etts_text.dat";


    Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (defenceStatus == ON) {
                cbFortification.setChecked(true);
                swFortification.setChecked(true);
            }
            if (gpsOnOff.equals(ON)) {
                cbStealth.setChecked(true);
                swStealth.setChecked(true);
            }
            isFirstIn = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fortification);
        StatusBarHelper.setStatusBarColor(this, R.color.title_color);
        device = (YunCheDeviceEntity) getIntent().getSerializableExtra(DeviceFragment.DATA_DEVICE);
        initTTS();
        initViews();
        addListener();
        getStatus();
    }

    //初始化语音合成
    private void initTTS() {
        showLoadingDialog(null);

        if (mSampleDirPath == null) {
            String sdcardPath = Environment.getExternalStorageDirectory().toString();
            mSampleDirPath = sdcardPath + "/" + SAMPLE_DIR_NAME;
        }
        makeDir(mSampleDirPath);
        copyFromAssetsToSdcard(false, SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/" + SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/" + SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, TEXT_MODEL_NAME, mSampleDirPath + "/" + TEXT_MODEL_NAME);

        String api_key = "", secret_key = "", app_id = "";
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            api_key = appInfo.metaData.getString("com.baidu.tts.API_KEY") + "";
            secret_key = appInfo.metaData.getString("com.baidu.tts.SECRET_KEY") + "";
            app_id = appInfo.metaData.getInt("com.baidu.tts.APP_ID") + "";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        this.mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        this.mSpeechSynthesizer.setContext(this);
        this.mSpeechSynthesizer.setSpeechSynthesizerListener(this);
        // 文本模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, mSampleDirPath + "/"
                + TEXT_MODEL_NAME);
        // 声学模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, mSampleDirPath + "/"
                + SPEECH_FEMALE_MODEL_NAME);
        // 请替换为语音开发者平台上注册应用得到的App ID (离线授权)
        this.mSpeechSynthesizer.setAppId(app_id);
        // 请替换为语音开发者平台注册应用得到的apikey和secretkey (在线授权)
        this.mSpeechSynthesizer.setApiKey(api_key,
                secret_key);
        // 发音人（在线引擎），可用参数为0,1,2,3。。。（服务器端会动态增加，各值含义参考文档，以文档说明为准。0--普通女声，1--普通男声，2--特别男声，3--情感男声。。。）
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置Mix模式的合成策略
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);

        // 授权检测接口(只是通过AuthInfo进行检验授权是否成功。)
        // AuthInfo接口用于测试开发者是否成功申请了在线或者离线授权，如果测试授权成功了，可以删除AuthInfo部分的代码（该接口首次验证时比较耗时），不会影响正常使用（合成使用时SDK内部会自动验证授权）
        AuthInfo authInfo = this.mSpeechSynthesizer.auth(TtsMode.MIX);
        // 判断授权信息是否正确，如果正确则初始化语音合成器并开始语音合成，如果失败则做错误处理
        if (authInfo.isSuccess()) {
            mSpeechSynthesizer.initTts(TtsMode.MIX);
            isInitTTS = true;
        } else {
            // 授权失败
            isInitTTS = false;
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            LogUtils.e("auth failed errorMsg=" + errorMsg);
        }
        dismissLoadingDialog();
    }

    private void makeDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 将sample工程需要的资源文件拷贝到SD卡中使用（授权文件为临时授权文件，请注册正式授权）
     *
     * @param isCover 是否覆盖已存在的目标文件
     * @param source
     * @param dest
     */
    private void copyFromAssetsToSdcard(boolean isCover, String source, String dest) {
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = getResources().getAssets().open(source);
                String path = dest;
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initViews() {
        TitleBar titleBar = (TitleBar) findViewById(R.id.titleBar);
        titleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        btnAry[0] = (RadioButton) findViewById(R.id.radio1);
        btnAry[1] = (RadioButton) findViewById(R.id.radio2);
        llFortification = (LinearLayout) findViewById(R.id.ll_fortification);
        llStealth = (LinearLayout) findViewById(R.id.ll_stealth);
        swStealth = (Switch) findViewById(R.id.sw_stealth);
        swFortification = (Switch) findViewById(R.id.sw_fortification);
        cbFortification = (CheckBox) findViewById(R.id.cb_fortification);
        cbStealth = (CheckBox) findViewById(R.id.cb_stealth);

        swFortification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (!isFirstIn) {
                    if (isChecked) {
                        if (device.equipmentStatus == 0) {
                            if (isInitTTS) {
                                mSpeechSynthesizer.speak("设备已离线");
                                showToast("设备已离线");
                            }
                            swFortification.setChecked(false);
                        } else if (device.equipmentStatus == 1) {
                            if (isInitTTS) {
                                mSpeechSynthesizer.speak("设备未启用");
                                showToast("设备未启用");
                            }
                            swFortification.setChecked(false);
                        } else if (device.equipmentStatus == 2) {
                            setFortification(SAFFON);
                            cbFortification.setChecked(true);
                        }
                    } else {
                        setFortification(SAFFOFF);
                        cbFortification.setChecked(false);
                    }
                }
            }
        });

        swStealth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (!isFirstIn) {
                    if (isChecked) {
                        WarningDialog.normal(FortificationActivity.this, "提示", "车辆隐身后，系统将不再记录所有行程及车身信息", "确定", "取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == DialogInterface.BUTTON_POSITIVE) {
                                    setStealth(ON);
                                    cbStealth.setChecked(true);
                                } else {
                                    swStealth.setChecked(false);
                                }
                            }
                        }).show();
                    } else {
                        setStealth(OFF);
                        cbStealth.setChecked(false);
                    }
                }
            }
        });

    }

    private void addListener() {
        myButtonListener = new MyButtonListener();
        for (int i = 0; i < btnAry.length; i++) {
            btnAry[i].setOnClickListener(myButtonListener);
        }
    }

    @Override
    public void onSynthesizeStart(String s) {
        // 监听到合成开始，在此添加相关操作
    }

    @Override
    public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {
        // 监听到合成结束，在此添加相关操作
    }

    @Override
    public void onSynthesizeFinish(String s) {
        // 监听到有合成数据到达，在此添加相关操作
    }

    @Override
    public void onSpeechStart(String s) {
        // 监听到合成并播放开始，在此添加相关操作
    }

    @Override
    public void onSpeechProgressChanged(String s, int i) {
        // 监听到播放进度有变化，在此添加相关操作
    }

    @Override
    public void onSpeechFinish(String s) {
        // 监听到播放结束，在此添加相关操作
    }

    @Override
    public void onError(String s, SpeechError speechError) {
        // 监听到出错，在此添加相关操作
    }

    class MyButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.radio1:
                    llFortification.setVisibility(View.VISIBLE);
                    llStealth.setVisibility(View.GONE);
                    break;
                case R.id.radio2:
                    llStealth.setVisibility(View.VISIBLE);
                    llFortification.setVisibility(View.GONE);
                    break;
            }
        }
    }

    public void getStatus() {
        showLoadingDialog(null);
        String mds = UserSp.getInstance(this).getMds(GlobalConsts.userName);
        OkGo.post(GlobalConsts.GET_DATE)
                .tag(this)
                .params("method", "getUserStatus")
                .params("mds", mds)
                .params("macid", device.macid)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        try {
                            LogUtils.d(s);
                            JSONObject jsonObject = new JSONObject(s);
                            String success = jsonObject.getString("success");
                            if (success.equals("false")) {
                                String msg = jsonObject.getString("msg");
                                showToast(msg);
                            } else {
                                JSONObject rowsObj = jsonObject.getJSONObject("rows");
                                defenceStatus = rowsObj.getString("defenceStatus");
                                gpsOnOff = rowsObj.getString("gpsOnOff");
                                mhandler.sendEmptyMessage(0);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onAfter(String s, Exception e) {
                        dismissLoadingDialog();
                    }
                });

    }

    public void setFortification(String status) {
        showLoadingDialog(null);
        String mds = UserSp.getInstance(this).getMds(GlobalConsts.userName);
        OkGo.post(GlobalConsts.GET_DATE)
                .tag(this)
                .params("method", "SendCommands")
                .params("mds", mds)
                .params("macid", device.macid)
                .params("cmd", status)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        try {
                            LogUtils.d(s);
                            JSONObject jsonObject = new JSONObject(s);
                            String success = jsonObject.getString("success");
                            if (success.equals("false")) {
                                String msg = jsonObject.getString("msg");
                                showToast(msg);
                            } else {
                                showToast("设置成功");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onAfter(String s, Exception e) {
                        dismissLoadingDialog();
                    }
                });

    }

    public void setStealth(String stealth) {
        showLoadingDialog(null);
        String mds = UserSp.getInstance(this).getMds(GlobalConsts.userName);
        OkGo.post(GlobalConsts.GET_DATE)
                .tag(this)
                .params("method", "gpsOnOff")
                .params("mds", mds)
                .params("macid", device.macid)
                .params("state", stealth)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        try {
                            LogUtils.d(s);
                            JSONObject jsonObject = new JSONObject(s);
                            String success = jsonObject.getString("success");
                            if (success.equals("false")) {
                                String msg = jsonObject.getString("msg");
                                showToast(msg);
                            } else {
                                showToast("设置成功");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onAfter(String s, Exception e) {
                        dismissLoadingDialog();
                    }
                });

    }

    private boolean isInitTTS;

}
