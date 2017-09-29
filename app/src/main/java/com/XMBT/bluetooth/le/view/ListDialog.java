package com.XMBT.bluetooth.le.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.bean.iBeaconClass;
import com.XMBT.bluetooth.le.consts.GlobalConsts;

import java.util.ArrayList;
import java.util.List;


public class ListDialog extends PopupWindow{

    private Context mContext;
    private LayoutInflater inflater;
    private View rootView;
    private ListView lv;
    private ListDialogAdapter mAdapter;
    private List<iBeaconClass.iBeacon> dataList = new ArrayList<>();

    public ListDialog(Context context, ItemClickCallback callback){
        this.mContext = context;
        this.mCallback = callback;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        initPopupWindow();
    }

    private void initPopupWindow() {

        rootView = inflater.inflate(R.layout.dialog_lv, null, false);
        lv = (ListView) rootView.findViewById(R.id.lv);
        mAdapter = new ListDialogAdapter(dataList);
        lv.setAdapter(mAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mCallback != null){
                    mCallback.callback(dataList.get(position), position);
                }
                dismiss();
            }
        });
        setContentView(rootView);

        DisplayMetrics dm = new DisplayMetrics();
        ((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
        setWidth((int) (dm.widthPixels * 0.7));
        setHeight((int) (dm.heightPixels * 0.55));
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(0));
        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_OUTSIDE){
                    dismiss();
                    return true;
                }
                return false;
            }
        });

    }

    public void changeData(List<iBeaconClass.iBeacon> dataList){
        this.dataList.clear();
        this.dataList.addAll(dataList);
        mAdapter.notifyDataSetChanged();
    }

    public void show(View view){
        showAtLocation(view, Gravity.CENTER,0,0);
        backgroundAlpha(0.5f);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        backgroundAlpha(1.0f);
    }

    private void backgroundAlpha(float v) {
        WindowManager.LayoutParams lp = ((Activity)mContext).getWindow().getAttributes();
        lp.alpha = v;
        ((Activity)mContext).getWindow().setAttributes(lp);
    }

    private ItemClickCallback mCallback;

    public interface ItemClickCallback{
        void callback(iBeaconClass.iBeacon bean, int position);
    }

    class ListDialogAdapter extends BaseAdapter{

        private List<iBeaconClass.iBeacon> dataList;

        ListDialogAdapter(List<iBeaconClass.iBeacon> dataList){
            this.dataList = dataList;
        }

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            return dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if(convertView == null){
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_dialog_list, null);
                viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_deviceName);
                viewHolder.tvAddress = (TextView) convertView.findViewById(R.id.tv_deviceAddress);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if(TextUtils.isEmpty(dataList.get(position).name)){
                viewHolder.tvName.setText("未知设备");
            }else{
                if(dataList.get(position).name.equals(GlobalConsts.POWER)){
                    viewHolder.tvName.setText(GlobalConsts.POWER_CN);
                }else if(dataList.get(position).name.equals(GlobalConsts.LIGHTING)){
                    viewHolder.tvName.setText(GlobalConsts.LIGHTING_CN);
                }else if(dataList.get(position).name.equals(GlobalConsts.BATTERY)){
                    viewHolder.tvName.setText(GlobalConsts.BATTERY_CN);
                }else if(dataList.get(position).name.equals(GlobalConsts.GPS_BATTERY)){
                    viewHolder.tvName.setText(GlobalConsts.GPS_BATTERY_CN);
                }else{
                    viewHolder.tvName.setText(dataList.get(position).name);
                }
            }
            viewHolder.tvAddress.setText(dataList.get(position).bluetoothAddress);
            return convertView;
        }
    }

    class ViewHolder{
        TextView tvName;
        TextView tvAddress;
    }

}
