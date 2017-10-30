package com.XMBT.bluetooth.le.ui.gbattery;

import java.util.ArrayList;
import java.util.HashMap;


import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.ble.BleManager;
import com.XMBT.bluetooth.le.ble.Stalls_Activity;
import com.XMBT.bluetooth.le.consts.GlobalConsts;
import com.XMBT.bluetooth.le.utils.HexUtil;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;


public class XM_Bt_Demo extends Activity implements View.OnClickListener {

    private final static String TAG = "XM_Bt_Demo";
    private final String ACTION_NAME_RSSI = "XM_RSSI";
    private final String ACTION_NOTIFI = "XM_NOTIFI";


    public final static String EXTRA_DATA = "EXTRA_DATA";
    public final static String EXTRA_UUID = "EXTRA_UUID";
    public final static String EXTRA_STATUS = "EXTRA_STATUS";

    private Handler handler;

    private TextView tv_receive;
    private String strTemp;

    static final int rssibufferSize = 10;
    int[] rssibuffer = new int[rssibufferSize];
    int rssibufferIndex = 0;
    boolean rssiUsedFalg = false;

    public static final int REFRESH = 0x000001;
    private Handler mHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.xm_bt_activity);
        getActionBar().setTitle("氙明应急电源");

        handler = new mHandler();

        tv_receive = (TextView) this.findViewById(R.id.tv_receiver);
        tv_receive.setText("");

//		String[] data = {"白光(照明)",		"白光(爆闪)",		"白光(SOS)",  		"白光(关)",		"红蓝(R-R-B-B)",
//				  		 "红蓝(R-B-R-B)",	"红蓝(R-B快闪)",		"红蓝灯(关)",		"USB输出(开)",	"USB输出(关)",
//						 "DC输出(启动12V)", "DC输出(启动16V)",	"DC输出(启动19V)", 	"DC输出(关)",    "蜂鸣器(开)",
//						 "蜂鸣器(关)",		"读氙气灯(档位)",	"读氙气灯(电压)",	"车灯状态",		 "氙气灯(开)",
//						 "档位设置",  		"氙气灯(关)", 		"归家(30秒)",        "归家(60秒)",    "归家(90秒)" ,
//						 "归家(120秒)"
//		};
        String[] data = {"读氙气灯(档位)", "读氙气灯(电压)", "车灯状态", "氙气灯(开)",
                "档位设置", "氙气灯(关)", "归家(30秒)", "归家(60秒)", "归家(90秒)",
                "归家(120秒)"
        };
//		int[] icon = {	R.drawable.anima101,R.drawable.anima102,R.drawable.anima103,R.drawable.anima104,R.drawable.anima105,
//						R.drawable.anima106,R.drawable.anima107,R.drawable.anima108,R.drawable.anima109,R.drawable.anima110,
//						R.drawable.anima111,R.drawable.anima112,R.drawable.anima113,R.drawable.anima114,R.drawable.anima115,
//						R.drawable.anima116,R.drawable.anima117,R.drawable.anima118,R.drawable.anima119,R.drawable.anima120,
//						R.drawable.anima121,R.drawable.anima122,R.drawable.anima123,R.drawable.anima124,R.drawable.anima125,
//						R.drawable.anima126
//		};
        int[] icon = {R.drawable.anima117, R.drawable.anima118, R.drawable.anima119, R.drawable.anima120,
                R.drawable.anima121, R.drawable.anima122, R.drawable.anima123, R.drawable.anima124, R.drawable.anima125,
                R.drawable.anima126
        };

        ArrayList<HashMap<String, Object>> grid = new ArrayList<HashMap<String, Object>>();

        for (int i = 0; i < 10; i++) {

            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("grid_image", icon[i]);
            map.put("grid_text", data[i]);
            grid.add(map);
        }

        GridView gridView = (GridView) findViewById(R.id.gridView);

        SimpleAdapter adapter = new SimpleAdapter(XM_Bt_Demo.this,
                grid,
                R.layout.grid_activity,
                new String[]{"grid_image", "grid_text"},
                new int[]{R.id.grid_image, R.id.grid_text});

        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new ItemClickListener());

        BleManager.setNotify();
        registerBoradcastReceiver();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == REFRESH) {

                }
                super.handleMessage(msg);
            }
        };

        ReadParameter();
        UpdateAllParameter();
    }

    class mHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    }


    public class ItemClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            HashMap<String, Object> item = (HashMap<String, Object>) parent.getItemAtPosition(position);

            setTitle((String) item.get("grid_text"));

            String strTemp = (String) item.get("grid_text");

//			if ( strTemp.equals("白光(照明)")){
//
//				String newValue = "0452AD31CE";
//
//				byte[] dataToWrite = Utils.hexStringToBytes(newValue);
//
//				DeviceScanActivity.WriteCharX( DeviceScanActivity.gattCharacteristic_char1,dataToWrite );
//			}
//
//			if( strTemp.equals("白光(爆闪)") ){
//
//				String newValue = "0452AD42BD";
//				byte[] dataToWrite = Utils.hexStringToBytes(newValue);
//				DeviceScanActivity.WriteCharX( DeviceScanActivity.gattCharacteristic_char1,dataToWrite );
//			}
//
//
//			if( strTemp.equals("白光(SOS)") ){
//
//				String newValue = "0452AD42BD";
//				byte[] dataToWrite = Utils.hexStringToBytes(newValue);
//				DeviceScanActivity.WriteCharX( DeviceScanActivity.gattCharacteristic_char1,dataToWrite );
//			}
//
//			if( strTemp.equals("白光(关)") ){
//				String newValue = "0452AD00FF";
//				byte[] dataToWrite = Utils.hexStringToBytes(newValue);
//				DeviceScanActivity.WriteCharX( DeviceScanActivity.gattCharacteristic_char1,dataToWrite );
//			}
//
//			if( strTemp.equals("红蓝(R-R-B-B)") ){
//
//				String newValue =  "04639C649B";
//				byte[] dataToWrite = Utils.hexStringToBytes(newValue);
//
//				DeviceScanActivity.WriteCharX( DeviceScanActivity.gattCharacteristic_char1,dataToWrite );
//			}
//
//			if( strTemp.equals("红蓝(R-B-R-B)") ){
//				String newValue =  "04639C758A";
//				byte[] dataToWrite = Utils.hexStringToBytes(newValue);
//				DeviceScanActivity.WriteCharX( DeviceScanActivity.gattCharacteristic_char1,dataToWrite );
//			}
//
//			if( strTemp.equals("红蓝(R-B快闪)") ){
//
//				String newValue =  "04639C8679";
//				byte[] dataToWrite = Utils.hexStringToBytes(newValue);
//
//				DeviceScanActivity.WriteCharX( DeviceScanActivity.gattCharacteristic_char1,dataToWrite );
//			}
//
//			if( strTemp.equals("红蓝灯(关)") ){
//				String newValue =  "04639C00FF";
//				byte[] dataToWrite = Utils.hexStringToBytes(newValue);
//
//				DeviceScanActivity.WriteCharX( DeviceScanActivity.gattCharacteristic_char1,dataToWrite );
//			}
//
//			if( strTemp.equals("USB输出(开)") ){
//				String newValue =  "04748B9768";
//				byte[] dataToWrite = Utils.hexStringToBytes(newValue);
//
//				DeviceScanActivity.WriteCharX( DeviceScanActivity.gattCharacteristic_char1,dataToWrite );
//			}
//
//			if( strTemp.equals("USB输出(关)") ){
//
//				String newValue =  "04748B00FF";
//				byte[] dataToWrite = Utils.hexStringToBytes(newValue);
//
//				DeviceScanActivity.WriteCharX( DeviceScanActivity.gattCharacteristic_char1,dataToWrite );
//			}
//
//			if( strTemp.equals("DC输出(启动12V)") ){
//
//				String newValue =  "04857AA857";
//				byte[] dataToWrite = Utils.hexStringToBytes(newValue);
//
//				DeviceScanActivity.WriteCharX( DeviceScanActivity.gattCharacteristic_char1,dataToWrite );
//			}
//
//			if( strTemp.equals("DC输出(启动16V)") ){
//
//				String newValue =  "04857AB946";
//				byte[] dataToWrite = Utils.hexStringToBytes(newValue);
//
//				DeviceScanActivity.WriteCharX( DeviceScanActivity.gattCharacteristic_char1,dataToWrite );
//			}

//			if( strTemp.equals("DC输出(启动19V)") ){
//
//				String newValue =  "04857ACA35";
//				byte[] dataToWrite = Utils.hexStringToBytes(newValue);
//
//				IndexFragment.WriteCharX( IndexFragment.gattCharacteristic_char1,dataToWrite );
//			}
//
//			if( strTemp.equals("DC输出(关)") ){
//
//				String newValue =  "04857A00FF";
//				byte[] dataToWrite = Utils.hexStringToBytes(newValue);
//
//				IndexFragment.WriteCharX( IndexFragment.gattCharacteristic_char1,dataToWrite );
//			}
//
//			if( strTemp.equals("蜂鸣器(开)") ){
//
//				String newValue =  "049669DB24";
//
//				byte[] dataToWrite =  Utils.hexStringToBytes(newValue);
//
//				IndexFragment.WriteCharX( IndexFragment.gattCharacteristic_char1, dataToWrite );
//			}
//
//			if( strTemp.equals("蜂鸣器(关)") ){
//
//				String newValue =  "04857A00FF";
//				byte[] dataToWrite = Utils.hexStringToBytes(newValue);
//
//				IndexFragment.WriteCharX( IndexFragment.gattCharacteristic_char1,dataToWrite );
//			}

            if (strTemp.equals("读氙气灯(档位)")) {

                String newValue = "0433CC31CE";
                byte[] dataToWrite = HexUtil.hexStringToBytes(newValue);

                BleManager.WriteCharX(BleManager.gattCharacteristic_write, dataToWrite);

            }

            if (strTemp.equals("读氙气灯(电压)")) {
                String newValue2 = "0433CC42BD";
                byte[] dataToWrite2 = HexUtil.hexStringToBytes(newValue2);

                BleManager.WriteCharX(BleManager.gattCharacteristic_write, dataToWrite2);
            }

            if (strTemp.equals("车灯状态")) {

                String newValue = "0433CC53AC";
                byte[] dataToWrite = HexUtil.hexStringToBytes(newValue);

                BleManager.WriteCharX(BleManager.gattCharacteristic_write, dataToWrite);
            }

            if (strTemp.equals("氙气灯(开)")) {

                String newValue = "0455AA31CE";
                byte[] dataToWrite = HexUtil.hexStringToBytes(newValue);

                BleManager.WriteCharX(BleManager.gattCharacteristic_write, dataToWrite);
            }

            if (strTemp.equals("档位设置")) {

                Intent intent = new Intent(getApplicationContext(), Stalls_Activity.class);

                startActivity(intent);

            }

            if (strTemp.equals("氙气灯(关)")) {

                String newValue = "0455AA00FF";
                byte[] dataToWrite = HexUtil.hexStringToBytes(newValue);

                BleManager.WriteCharX(BleManager.gattCharacteristic_write, dataToWrite);
            }

            if (strTemp.equals("归家(30秒)")) {

                String newValue = "0477881EE1";
                byte[] dataToWrite = HexUtil.hexStringToBytes(newValue);

                BleManager.WriteCharX(BleManager.gattCharacteristic_write, dataToWrite);
            }

            if (strTemp.equals("归家(60秒)")) {

                String newValue = "0477883CC3";
                byte[] dataToWrite = HexUtil.hexStringToBytes(newValue);

                BleManager.WriteCharX(BleManager.gattCharacteristic_write, dataToWrite);
            }

            if (strTemp.equals("归家(90秒)")) {

                String newValue = "0477885AA5";
                byte[] dataToWrite = HexUtil.hexStringToBytes(newValue);


                BleManager.WriteCharX(BleManager.gattCharacteristic_write, dataToWrite);
            }

            // 25
            if (strTemp.equals("归家(120秒)")) {

                String newValue = "0477887887";
                byte[] dataToWrite = HexUtil.hexStringToBytes(newValue);

                BleManager.WriteCharX(BleManager.gattCharacteristic_write, dataToWrite);
            }

        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String strXqdDd = "";
            String strXqdDdy = "";
            if (action.equals(ACTION_NOTIFI)) {

                Log.v("MyLog", "rssi广播里面收到notificatin数值");
                tv_receive.setText("");
                strTemp = intent.getStringExtra(EXTRA_DATA);

                if (!strTemp.equals("00:00:00:00:00")) {  //过滤掉00:00:00:00:00

                    if (strTemp.equals("EE")) {
                        tv_receive.setText("收到数据->" + strTemp);
                    } else {


                        String substr = strTemp.substring(0, 8);
                        String substr2 = strTemp.substring(9, 14);

                        if (substr.equals("04:A7:58")) {

                            tv_receive.setText("电池电压->" + substr2);
                        }

                        if (substr.equals("06:B8:47")) {

                            tv_receive.setText("电池温度->" + substr2);
                        }

                        if (substr.equals("06:C9:36")) {

                            tv_receive.setText("使用天数->" + substr2);
                        }

                        if (substr.equals("04:99:66")) {// || substr.equals("04:BB:44") ){

                            tv_receive.setText("档位->" + substr2);
                        }

                        if (substr.equals("04:BB:44")) {


                            strXqdDdy = "氙气灯电压->" + substr2;
                            tv_receive.setText("档位" + substr2);
                        }

                        if (substr.equals("04:DD:22")) {

                            if (substr2.equals("01:FE")) {
                                tv_receive.setText("氙气大灯故障:工作电压过高!");
                            }

                            if (substr2.equals("02:FD")) {
                                tv_receive.setText("氙气大灯故障:工作电压过低");
                            }

                            if (substr2.equals("03:FC")) {
                                tv_receive.setText("氙气大灯故障:工作温度过高");
                            }

                            if (substr2.equals("04:FB")) {
                                tv_receive.setText("氙气大灯故障:点灯失败!" + "\n" + "氙气大灯已经关闭!");
                            }
                        }

                        if (substr.equals("04:CC:33")) {

                            if (substr2.equals("31:CE")) {
                                tv_receive.setText("车灯状态: 开");
                            }

                            if (substr2.equals("00:FF")) {
                                tv_receive.setText("车灯状态: 关");
                            }
                        }

                    }
                }
            }

            if (action.equals(ACTION_NAME_RSSI)) {
                int rssi = intent.getIntExtra("RSSI", 0);


                int rssi_avg = 0;
                int distance_cm_min = 10;
                int distance_cm_max_near = 1500;
                int distance_cm_max_middle = 5000;
                int distance_cm_max_far = 10000;
                int near = -72;
                int middle = -80;
                int far = -88;
                double distance = 0.0f;

                if (true) {
                    rssibuffer[rssibufferIndex] = rssi;
                    rssibufferIndex++;

                    if (rssibufferIndex == rssibufferSize) {
                        rssiUsedFalg = true;
                    }

                    rssibufferIndex = rssibufferIndex % rssibufferSize;

                    if (rssiUsedFalg == true) {
                        int rssi_sum = 0;
                        for (int i = 0; i < rssibufferSize; i++) {
                            rssi_sum += rssibuffer[i];
                        }

                        rssi_avg = rssi_sum / rssibufferSize;

                        if (-rssi_avg < 35) {
                            rssi_avg = -35;
                        }

                        if (-rssi_avg < -near) {
                            distance = distance_cm_min + ((-rssi_avg - 35) / (double) (-near - 35)) * distance_cm_max_near;
                        } else if (-rssi_avg < -middle) {
                            distance = distance_cm_min + ((-rssi_avg - 35) / (double) (-middle - 35)) * distance_cm_max_middle;
                        } else {
                            distance = distance_cm_min + ((-rssi_avg - 35) / (double) (-far - 35)) * distance_cm_max_far;
                        }
                    }
                }

                getActionBar().setTitle("RSSI: " + rssi_avg + " dbm" + ", " + "距离: " + (int) distance + " cm");
            } else if (action.equals(GlobalConsts.ACTION_CONNECT_CHANGE)) {
                int status = intent.getIntExtra("CONNECT_STATUC", 0);
                if (status == 0) {
                    getActionBar().setTitle("已断开连接");
                    finish();
                } else {
                    getActionBar().setTitle("已连接");
                }
            }
        }
    };


    public void registerBoradcastReceiver() {

        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(ACTION_NAME_RSSI);
        myIntentFilter.addAction(GlobalConsts.ACTION_CONNECT_CHANGE);
        myIntentFilter.addAction(ACTION_NOTIFI);

        registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    private void UpdateAllParameter() {
        Message msg = new Message();
        msg.what = REFRESH;
        mHandler.sendMessage(msg);
    }

    private void writeParameter() {
        SharedPreferences.Editor sharedata = getSharedPreferences("data", 0).edit();


    }


    private void ReadParameter() {
        SharedPreferences sharedata = getSharedPreferences("data", 0);


    }

    @Override
    protected void onStop() {

        super.onStop();
        writeParameter();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        writeParameter();
    }

    @Override
    public void onClick(View v) {


    }

}
