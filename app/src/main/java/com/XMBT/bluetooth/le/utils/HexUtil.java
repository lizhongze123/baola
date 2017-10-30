package com.XMBT.bluetooth.le.utils;

import java.math.BigInteger;
import java.util.Arrays;

public class HexUtil {


    /**
     * 将byte[]数组转化为8、10、16等各种进制，例如byte[0x11,0x20]→4384，约等于1120（16进制）→4384，radix代表进制
     *
     * @param bytes
     * @param radix
     * @return
     */
    public static String bytesToAllHex(byte[] bytes, int radix) {
        return new BigInteger(1, bytes).toString(radix);// 这里的1代表正数
    }

    /**
     * 将String的十六进制原封不动转化为byte的十六进制，例如7e20→new byte[]{0x7e，0x20}
     *
     * @return
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        //补0 274 -> 0274
        StringBuilder stringBuilder = new StringBuilder("");
        if (hexString.length() % 2 != 0) {
            stringBuilder.append(0);
        }
        stringBuilder.append(hexString);
        hexString = stringBuilder.toString();
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    public static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * 数组切割
     *
     * @param data
     * @return
     */
    public static byte[] bytesCut(byte[] data, int start, int end) {
        byte[] idData;
        idData = Arrays.copyOfRange(data, start, end);
        return idData;
    }


    /**
     * 数组反转
     */
    public static byte[] bytesCollections(byte[] data) {
        for (int a = 0; a < data.length / 2; a++) {
            //数组反转 高低位
            byte temp = data[a];
            data[a] = data[data.length - 1 - a];
            data[data.length - 1 - a] = temp;
        }
        return data;
    }


    /**
     * 字符串反转
     */
    public static String stringCollections(String str) {

        String s2 = "";
        char[] cs = str.toCharArray();
        for (int i = cs.length - 1; i >= 0; i--) {
            s2 = s2 + cs[i];
        }
        LogUtils.e(s2);
        return s2;
    }

    /**
     * 将字符转为16进制字节数组
     */
    public static byte[] str2bytes(String str) {
        byte[] data = new byte[200];
        data = str.getBytes();
        final StringBuilder stringBuilder = new StringBuilder(data.length);
        for (byte byteChar : data) {
            stringBuilder.append(String.format("%02X ", byteChar));
        }
        return data;
    }


    /**
     * 将十六进制的byte[]原封不动的转化为string，
     * 并且每个byte之间用空格分开，如byte[]{0x7e，0x80,0x11,0x20}→7e 80 11 20，,可用于log打印
     *
     * @param data
     * @return
     */
    public static StringBuilder byte2HexStr(byte[] data) {

        if (data != null && data.length > 0) {
            StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data) {
                stringBuilder.append(String.format("%02X ", byteChar));
            }
            return stringBuilder;
        }
        return null;
    }


    public static String byteStringCollections(String value) {
        int len = value.length();
        char[] hexChars = value.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len - 1; i = i + 2) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.insert(0, hexChars[i]);
            stringBuilder.append(hexChars[i + 1]);
            sb.insert(0, stringBuilder.toString());
        }
        return sb.toString();
    }

}