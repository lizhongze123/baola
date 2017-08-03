package com.XMBT.bluetooth.le.ble;

import android.app.Activity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.XMBT.bluetooth.le.R;
import com.XMBT.bluetooth.le.ble.DeviceScanActivity;
import com.XMBT.bluetooth.le.utils.HexUtil;
import com.XMBT.bluetooth.le.utils.Utils;

public class Stalls_Activity extends Activity{

	private int iStalls=15;
	private SeekBar  mStallsSeekBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.stalls_activity);
		getActionBar().setTitle("亮度档位设置");
		
		mStallsSeekBar = (SeekBar) this.findViewById(R.id.seekbar_stalls);
		
		mStallsSeekBar.setOnSeekBarChangeListener( new OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				

				
				String newValue =  "046699";
				String strtemp ="";
				byte[] PwmValue = new byte[2]; 
				
 				int i = seekBar.getProgress();
 				PwmValue[0] = (byte)(((i & 0xFF )* iStalls/15) & 0xff); 
 				PwmValue[1]	= (byte) ~((byte)(((i & 0xFF )* iStalls/15) & 0xff)); 
 				strtemp = Utils.bytesToHexString(PwmValue) ;
 				newValue+=strtemp;
 				byte[] dataToWrite =  HexUtil.hexStringToBytes(newValue);
 				DeviceScanActivity.WriteCharX(DeviceScanActivity.gattCharacteristic_char1, dataToWrite);
 			
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {	}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) { }
			
		});
	}
	
	

}
