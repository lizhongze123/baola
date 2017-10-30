package com.XMBT.bluetooth.le.view.datePicker.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @ClassName ConvertUtils
 * @Description 数据类型转换、单位转换
 */
public class ConvertUtils {
    /**
     * The constant GB.
     */
    public static final long GB = 1073741824;

    /**
     * The constant MB.
     */
    public static final long MB = 1048576;

    /**
     * The constant KB.
     */
    public static final long KB = 1024;

    /**
     * To int int.
     *
     * @param obj the obj
     * @return the int
     */
    public static int toInt(Object obj) {
        try {
            return Integer.parseInt(obj.toString());
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * To int int.
     *
     * @param bytes the bytes
     * @return the int
     */
    public static int toInt(byte[] bytes) {
        int result = 0;
        byte abyte;
        for (int i = 0; i < bytes.length; i++) {
            abyte = bytes[i];
            result += (abyte & 0xFF) << (8 * i);
        }
        return result;
    }

    /**
     * To short int.
     *
     * @param first  the first
     * @param second the second
     * @return the int
     */
    public static int toShort(byte first, byte second) {
        return (first << 8) + (second & 0xFF);
    }

    /**
     * To long long.
     *
     * @param obj the obj
     * @return the long
     */
    public static long toLong(Object obj) {
        try {
            return Long.parseLong(obj.toString());
        } catch (Exception e) {
            return -1L;
        }
    }

    /**
     * To float float.
     *
     * @param obj the obj
     * @return the float
     */
    public static float toFloat(Object obj) {
        try {
            return Float.parseFloat(obj.toString());
        } catch (Exception e) {
            return -1f;
        }
    }

    /**
     * int占4字节
     *
     * @param i the
     * @return byte [ ]
     */
    public static byte[] toByteArray(int i) {
        // byte[] bytes = new byte[4];
        // bytes[0] = (byte) (0xff & i);
        // bytes[1] = (byte) ((0xff00 & i) >> 8);
        // bytes[2] = (byte) ((0xff0000 & i) >> 16);
        // bytes[3] = (byte) ((0xff000000 & i) >> 24);
        // return bytes;
        return ByteBuffer.allocate(4).putInt(i).array();
    }

    /**
     * To byte array byte [ ].
     *
     * @param hexData the hex data
     * @param isHex   the is hex
     * @return the byte [ ]
     */
    public static byte[] toByteArray(String hexData, boolean isHex) {
        if (hexData == null || hexData.equals("")) {
            return null;
        }
        if (!isHex) {
            return hexData.getBytes();
        }
        hexData = hexData.replaceAll("\\s+", "");
        String hexDigits = "0123456789ABCDEF";
        ByteArrayOutputStream baos = new ByteArrayOutputStream(
                hexData.length() / 2);
        // 将每2位16进制整数组装成一个字节
        for (int i = 0; i < hexData.length(); i += 2) {
            baos.write((hexDigits.indexOf(hexData.charAt(i)) << 4 | hexDigits
                    .indexOf(hexData.charAt(i + 1))));
        }
        byte[] bytes = baos.toByteArray();
        try {
            baos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }

    /**
     * To hex string string.
     *
     * @param str the str
     * @return the string
     */
    public static String toHexString(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        StringBuilder buffer = new StringBuilder();
        byte[] bytes = str.getBytes();
        for (byte aByte : bytes) {
            buffer.append(Integer.toHexString(Integer.valueOf(0xFF & aByte)
                    .intValue()));
            buffer.append(" ");
        }
        return buffer.toString();
    }

    /**
     * To hex string string.
     *
     * @param bytes the bytes
     * @return the string
     */
    public static String toHexString(byte... bytes) {
        char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f'};
        // 参见：http://www.oschina.net/code/snippet_116768_9019
        char[] buffer = new char[bytes.length * 2];
        for (int i = 0, j = 0; i < bytes.length; ++i) {
            int u = bytes[i] < 0 ? bytes[i] + 256 : bytes[i];// 转无符号整型
            buffer[j++] = DIGITS[u >>> 4];
            buffer[j++] = DIGITS[u & 0xf];
        }
        return new String(buffer);
    }

    /**
     * To hex string string.
     *
     * @param num the num
     * @return the string
     */
    public static String toHexString(int num) {
        String hexString = Integer.toHexString(num);
        // LogUtils.debug(String.format("%d to hex string is %s", num,
        // hexString));
        return hexString;
    }

    /**
     * To binary string string.
     *
     * @param bytes the bytes
     * @return the string
     */
    public static String toBinaryString(byte... bytes) {
        char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f'};
        // 参见：http://www.oschina.net/code/snippet_116768_9019
        char[] buffer = new char[bytes.length * 8];
        for (int i = 0, j = 0; i < bytes.length; ++i) {
            int u = bytes[i] < 0 ? bytes[i] + 256 : bytes[i];// 转无符号整型
            buffer[j++] = DIGITS[(u >>> 7) & 0x1];
            buffer[j++] = DIGITS[(u >>> 6) & 0x1];
            buffer[j++] = DIGITS[(u >>> 5) & 0x1];
            buffer[j++] = DIGITS[(u >>> 4) & 0x1];
            buffer[j++] = DIGITS[(u >>> 3) & 0x1];
            buffer[j++] = DIGITS[(u >>> 2) & 0x1];
            buffer[j++] = DIGITS[(u >>> 1) & 0x1];
            buffer[j++] = DIGITS[u & 0x1];
        }
        return new String(buffer);
    }

    /**
     * To binary string string.
     *
     * @param num the num
     * @return the string
     */
    public static String toBinaryString(int num) {
        String binaryString = Integer.toBinaryString(num);
        // LogUtils.debug(String.format("%d to binary string is %s", num,
        // binaryString));
        return binaryString;
    }

    /**
     * 转换为6位十六进制颜色代码，不含“#”
     *
     * @param color the color
     * @return string string
     */
    public static String toColorString(int color) {
        return toColorString(color, false);
    }

    /**
     * 转换为6位十六进制颜色代码，不含“#”
     *
     * @param color        the color
     * @param includeAlpha the include alpha
     * @return string string
     */
    public static String toColorString(int color, boolean includeAlpha) {
        String alpha = Integer.toHexString(Color.alpha(color));
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));
        if (alpha.length() == 1) {
            alpha = "0" + alpha;
        }
        if (red.length() == 1) {
            red = "0" + red;
        }
        if (green.length() == 1) {
            green = "0" + green;
        }
        if (blue.length() == 1) {
            blue = "0" + blue;
        }
        String colorString;
        if (includeAlpha) {
            colorString = alpha + red + green + blue;
            // LogUtils.debug(String.format("%d to color string is %s", color,
            // colorString));
        } else {
            colorString = red + green + blue;
            // LogUtils.debug(String.format("%d to color string is %s%s%s%s, exclude alpha is %s",
            // color, alpha, red, green, blue, colorString));
        }
        return colorString;
    }

    /**
     * 将指定的日期转换为一定格式的字符串
     *
     * @param date   the date
     * @param format the format
     * @return string string
     */
    public static String toDateString(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CHINA);
        return sdf.format(date);
    }

    /**
     * 将当前的日期转换为一定格式的字符串
     *
     * @param format the format
     * @return string string
     */
    public static String toDateString(String format) {
        return toDateString(Calendar.getInstance(Locale.CHINA).getTime(),
                format);
    }

    /**
     * 将指定的日期字符串转换为日期时间
     *
     * @param dateStr 如：2014-04-08 23:02
     * @return date date
     */
    public static Date toDate(String dateStr) {
        return MyDateUtils.parseDate(dateStr);
    }

    /**
     * 将指定的日期字符串转换为时间戳
     *
     * @param dateStr 如：2014-04-08 23:02
     * @return long long
     */
    public static long toTimemillis(String dateStr) {
        return toDate(dateStr).getTime();
    }

    /**
     * To slash string string.
     *
     * @param str the str
     * @return the string
     */
    public static String toSlashString(String str) {
        String result = "";
        char[] chars = str.toCharArray();
        for (char chr : chars) {
            if (chr == '"' || chr == '\'' || chr == '\\') {
                result += "\\";// 符合"、'、\这三个符号的前面加一个\
            }
            result += chr;
        }
        return result;
    }

    /**
     * To array t [ ].
     *
     * @param <T>  the type parameter
     * @param list the list
     * @return the t [ ]
     */
    public static <T> T[] toArray(List<T> list) {
        // noinspection unchecked
        return (T[]) list.toArray();
    }

    /**
     * To list list.
     *
     * @param <T>   the type parameter
     * @param array the array
     * @return the list
     */
    public static <T> List<T> toList(T[] array) {
        return Arrays.asList(array);
    }

    /**
     * To string string.
     *
     * @param objects the objects
     * @return the string
     */
    public static String toString(Object[] objects) {
        return Arrays.deepToString(objects);
    }

    /**
     * To string string.
     *
     * @param objects the objects
     * @param tag     the tag
     * @return the string
     */
    public static String toString(Object[] objects, String tag) {
        StringBuilder sb = new StringBuilder();
        for (Object object : objects) {
            sb.append(object);
            sb.append(tag);
        }
        return sb.toString();
    }

    /**
     * To byte array byte [ ].
     *
     * @param is the is
     * @return the byte [ ]
     */
    public static byte[] toByteArray(InputStream is) {
        if (is == null) {
            return null;
        }
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buff = new byte[100];
            int rc = 0;
            while ((rc = is.read(buff, 0, 100)) > 0) {
                os.write(buff, 0, rc);
            }
            byte[] bytes = os.toByteArray();
            os.close();
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * To byte array byte [ ].
     *
     * @param bitmap the bitmap
     * @return the byte [ ]
     */
    public static byte[] toByteArray(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // 将Bitmap压缩成PNG编码，质量为100%存储，除了PNG还有很多常见格式，如jpeg等。
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
        byte[] bytes = os.toByteArray();
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    /**
     * dp转换为px
     *
     * @param context the context
     * @param dpValue the dp value
     * @return int int
     */
    public static int toPx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        int pxValue = (int) (dpValue * scale + 0.5f);
        // LogUtils.debug(dpValue + " dp == " + pxValue + " px");
        return pxValue;
    }

    /**
     * To px int.
     *
     * @param dpValue the dp value
     * @return the int
     */
    public static int toPx(float dpValue) {
        Resources resources = Resources.getSystem();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpValue, resources.getDisplayMetrics());
        return (int) px;
    }

    /**
     * px转换为dp
     *
     * @param context the context
     * @param pxValue the px value
     * @return int int
     */
    public static int toDp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        int dpValue = (int) (pxValue / scale + 0.5f);
        // LogUtils.debug(pxValue + " px == " + dpValue + " dp");
        return dpValue;
    }

    /**
     * px转换为sp
     *
     * @param context the context
     * @param pxValue the px value
     * @return int int
     */
    public static int toSp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        int spValue = (int) (pxValue / fontScale + 0.5f);
        // LogUtils.debug(pxValue + " px == " + spValue + " sp");
        return spValue;
    }

}
