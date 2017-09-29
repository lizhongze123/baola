package com.XMBT.bluetooth.le.ui.gbattery;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.bean.YunCheDeviceEntity;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.map.MapUtils;
import com.XMBT.bluetooth.le.sp.UserSp;
import com.XMBT.bluetooth.le.utils.LogUtils;
import com.baidu.mapapi.model.LatLng;
import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Response;

import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;

/**
 * Created by lzz on 2017/9/13.
 */

public class DisplacementService extends Service implements SpeechSynthesizerListener {

    private YunCheDeviceEntity device;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            device = (YunCheDeviceEntity) intent.getSerializableExtra(DeviceActivity.DATA_DEVICE);
            if(device != null){
                //获取围栏状态
                getFenStatus();
                //获取位移设防状态
                getStatus();
                serviceTimerTask(device);
            }else{
                LogUtils.e("device为null");
            }
        }else{
            LogUtils.e("intent为null");
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        /**
         *创建Notification
         */
        /*NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentTitle("宝利奥");
        builder.setContentText("正在后台运行");

//        Intent i = new Intent(this, YunCheActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity
//                (this, 0, i, 0);
//        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        //启动到前台
        startForeground(1, notification);*/


        initTTS();
    }

    private SpeechSynthesizer mSpeechSynthesizer;
    private String mSampleDirPath;

    private static final String SAMPLE_DIR_NAME = "baiduTTS";
    private static final String SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female.dat";
    private static final String SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male.dat";
    private static final String TEXT_MODEL_NAME = "bd_etts_text.dat";

    //初始化语音合成
    private void initTTS() {
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
            BatteryUtils.isInitTTS = true;
        } else {
            // 授权失败
            BatteryUtils.isInitTTS = false;
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            LogUtils.e("auth failed errorMsg=" + errorMsg);
        }
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

    private Thread mThread;

    private void serviceTimerTask(final YunCheDeviceEntity device){
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try{
                        getLocate(device);
                        Thread.sleep(30000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
        mThread.start();
    }


    /**
     * 获取车辆位置
     */
    private void getLocate(YunCheDeviceEntity device) {
        String mds = UserSp.getInstance(this).getMds(GlobalConsts.userName);
        String id = UserSp.getInstance(this).getId(GlobalConsts.userName);
        OkGo.get(GlobalConsts.GET_DATE)
                .tag(this)
                .params("method", "getUserAndGpsInfoByIDsUtcNew")
                .params("school_id", id)
                .params("custid", id)
                .params("userIDs", device.id)
                .params("mapType", "BAIDU")
                .params("option", "cn")
                .params("mds", mds)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        LogUtils.i(s);
                        parseJson(s);
                    }

                    @Override
                    public void onAfter(String s, Exception e) {
                        super.onAfter(s, e);
                    }
                });
    }

    private void parseJson(String json) {
        /**
         "data": [
         {
         "key": {},
         "records": [[]],
         "groups": []
         }
         ]
         */
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray arydata = jsonObject.getJSONArray("data");
            for (int i = 0; i < arydata.length(); i++) {
                JSONObject datajson = arydata.getJSONObject(i);
                JSONObject keyjson = datajson.getJSONObject("key");
                int jingdu = keyjson.getInt("jingdu");
                int weidu = keyjson.getInt("weidu");
                JSONArray aryrecord = datajson.getJSONArray("records");
                for (int j = 0; j < aryrecord.length(); j++) {
                    JSONArray aryrecords = aryrecord.getJSONArray(j);
                    double jingdustr = aryrecords.getDouble(jingdu);
                    double weidustr = aryrecords.getDouble(weidu);

                    //检测位移报警
//                    checkDisplacement(weidustr, jingdustr);

                    //检测围栏报警
                    checkFence(weidustr, jingdustr);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkFence(double lat, double lng) {
        //如果围栏不设防，则不报警
        if(BatteryUtils.fenceStatus.equals("0")){
            LogUtils.d("车辆没有设置围栏报警");
            return;
        }
        //设防即计算当前位置和围栏中心点的位置
        int distance = MapUtils.getDistance(new LatLng(BatteryUtils.fenceLat, BatteryUtils.fenceLng),  new LatLng(lat,lng));
        LogUtils.d("围栏半径为--" + BatteryUtils.radius + "//距离为--" + distance);
        //如果距离超过半径即表示出了围栏
        if(distance > BatteryUtils.radius){
            if(BatteryUtils.isFirstFenceAlarm){
                //第一次报警，在页面关闭时复位
                BatteryUtils.fenceTime =  System.currentTimeMillis();
                alarm("围栏报警", "车辆出了围栏");
                BatteryUtils.isFirstFenceAlarm = false;
            }else if(System.currentTimeMillis() - BatteryUtils.fenceTime >= 600000){
                alarm("围栏报警", "车辆出了围栏");
            }
        }
    }


    private void checkDisplacement(double lat, double lng) {
        //如果位移不设防，则不报警
        if(BatteryUtils.displaceStatus.equals("0")){
            LogUtils.d("车辆没有设置位移报警");
            return;
        }

        if(BatteryUtils.last == null){
            BatteryUtils.last = new LatLng(lat,lng);
            BatteryUtils.time = System.currentTimeMillis();
        }else{
            int distance = MapUtils.getDistance(BatteryUtils.last,  new LatLng(lat,lng));
            LogUtils.d("位移距离为--" + distance);
            if(distance != 0){
                //车辆移动了
                if(System.currentTimeMillis() - BatteryUtils.time >= 600000){
                    //过了10分钟了
                    alarm("位移报警", "车移动了");
                    BatteryUtils.time = System.currentTimeMillis();
                }else if(BatteryUtils.isFirstDisplaceStatusAlarm){
                    //第一次报警
                    alarm("位移报警", "车移动了");
                    BatteryUtils.isFirstDisplaceStatusAlarm = false;
                }

            }else{

            }

            BatteryUtils.last = new LatLng(lat,lng);

        }
    }

    private void alarm(String type, String tips) {
//        Intent intent = new Intent(this, YunCheActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder localBuilder = new NotificationCompat.Builder(this);
        localBuilder.setSmallIcon(R.drawable.ic_launcher);
        localBuilder.setContentTitle("报警信息");
        localBuilder.setContentText("[" + device.fullname + "]" + type);
        localBuilder.setAutoCancel(true);
        //设置通知的优先级
        localBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        // 设置通知的提示音
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        localBuilder.setSound(alarmSound);
        localBuilder.setVisibility(VISIBILITY_PUBLIC);
//        localBuilder.setFullScreenIntent(pendingIntent, false);
//                          localBuilder.setContentIntent(pendingIntent);


        NotificationManager localNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        localNotificationManager.notify(1001, localBuilder.build());

        mSpeechSynthesizer.speak(tips);
    }

    /**
     * 获取围栏状态
     */
    public void getFenStatus() {
        String mds = UserSp.getInstance(this).getMds(GlobalConsts.userName);
        OkGo.post(GlobalConsts.GET_DATE)
                .tag(this)
                .params("method", "GetGpsUserDefence")
                .params("macid", device.macid)
                .params("language", "cn")
                .params("mapType", "BAIDU")
                .params("mds", mds)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        LogUtils.d(s);
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            JSONObject dataObj = jsonObject.getJSONObject("data");
                            if (dataObj != null) {

                                BatteryUtils.fenceLat = Double.valueOf(dataObj.getString("DefenceLat"));
                                BatteryUtils.fenceLng = Double.valueOf(dataObj.getString("DefenceLon"));
                                BatteryUtils.radius = Double.valueOf(dataObj.getString("DefenceRad"));
                                BatteryUtils.fenceStatus = dataObj.getString("DefenceStatus");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });
    }

    /**
     * 获取车辆位移设防状态
     */
    public void getStatus() {
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
                            } else {
                                JSONObject rowsObj = jsonObject.getJSONObject("rows");
                                BatteryUtils.displaceStatus = rowsObj.getString("defenceStatus");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onAfter(String s, Exception e) {
                    }
                });

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

}
