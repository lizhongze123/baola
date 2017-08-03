/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.XMBT.bluetooth.le.bean;

import android.bluetooth.BluetoothDevice;

import com.XMBT.bluetooth.le.utils.Utils;

import java.io.Serializable;


public class iBeaconClass {

	private final static String TAG = "iBeaconClass";

	static public class iBeacon implements Serializable{
		public String name;
		public int major;
		public int minor;
		public String proximityUuid;
		public String bluetoothAddress;
		public int txPower;
		public int rssi;
		public boolean isIbeacon;
	}

	public static iBeacon fromScanData(BluetoothDevice device, int rssi,byte[] scanData) {

		int startByte = 2;
		boolean patternFound = false;

		while (startByte <= 5) {
			if (((int) scanData[startByte + 2] & 0xff) == 0x02 	&& ((int) scanData[startByte + 3] & 0xff) == 0x15) {
				patternFound = true;
				break;
			} 
			else if ( ((int) scanData[startByte] & 0xff) == 0x2d
					&& ((int) scanData[startByte + 1] & 0xff) == 0x24
					&& ((int) scanData[startByte + 2] & 0xff) == 0xbf
					&& ((int) scanData[startByte + 3] & 0xff) == 0x16) {
				iBeacon iBeacon = new iBeacon();
				iBeacon.major = 0;
				iBeacon.minor = 0;
				iBeacon.proximityUuid = "00000000-0000-0000-0000-000000000000";
				iBeacon.txPower = -55;
				iBeacon.isIbeacon = patternFound;
				return iBeacon;
			} 
			else if (((int) scanData[startByte] & 0xff) == 0xad
					&& ((int) scanData[startByte + 1] & 0xff) == 0x77
					&& ((int) scanData[startByte + 2] & 0xff) == 0x00
					&& ((int) scanData[startByte + 3] & 0xff) == 0xc6) {

				iBeacon iBeacon = new iBeacon();
				iBeacon.major = 0;
				iBeacon.minor = 0;
				iBeacon.proximityUuid = "00000000-0000-0000-0000-000000000000";
				iBeacon.txPower = -55;
				iBeacon.isIbeacon = patternFound;
				return iBeacon;
			}
			startByte++;
		}

		if (patternFound == false) {

			if (device != null) {
				iBeacon iBeacon = new iBeacon();

				iBeacon.major = 0;
				iBeacon.minor = 0;
				iBeacon.proximityUuid = Utils.bytesToHexString(scanData);
				iBeacon.txPower = 0;
				iBeacon.rssi = rssi;
				iBeacon.bluetoothAddress = device.getAddress();
				iBeacon.name = device.getName();
				iBeacon.isIbeacon = patternFound;
				return iBeacon;
			}
			return null;
		}

		iBeacon iBeacon = new iBeacon();

		iBeacon.major = (scanData[startByte + 20] & 0xff) * 0x100 + (scanData[startByte + 21] & 0xff);
		iBeacon.minor = (scanData[startByte + 22] & 0xff) * 0x100 + (scanData[startByte + 23] & 0xff);
		iBeacon.txPower = (int) scanData[startByte + 24]; // this one is signed
		iBeacon.rssi = rssi;

		byte[] proximityUuidBytes = new byte[16];
		System.arraycopy(scanData, startByte + 4, proximityUuidBytes, 0, 16);
		String hexString = bytesToHexString(proximityUuidBytes);
		StringBuilder sb = new StringBuilder();
		sb.append(hexString.substring(0, 8));
		sb.append("-");
		sb.append(hexString.substring(8, 12));
		sb.append("-");
		sb.append(hexString.substring(12, 16));
		sb.append("-");
		sb.append(hexString.substring(16, 20));
		sb.append("-");
		sb.append(hexString.substring(20, 32));
		iBeacon.proximityUuid = sb.toString();

		if (device != null) {
			iBeacon.bluetoothAddress = device.getAddress();
			iBeacon.name = device.getName();
		}

		iBeacon.isIbeacon = patternFound;
		return iBeacon;
	}

	private static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}
}
