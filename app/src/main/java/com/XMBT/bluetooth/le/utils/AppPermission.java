package com.XMBT.bluetooth.le.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 新的权限机制更好的保护了用户的隐私，Google将权限分为两类，一类是Normal Permissions，这类权限一般不涉及用户隐私，是不需要用户进行授权的，比如手机震动、访问网络等；另一类是Dangerous Permission，一般是涉及到用户隐私的，需要用户进行授权，比如读取sdcard、访问通讯录等。
 */
public class AppPermission {

    public static final int PERMISSION_REQUEST_CODE = 101;
    /**
     * 傻瓜化，获取所有配置的权限，再找出需要动态申请的权限，进行申请
     *
     * @param activity
     * @return true：进行了申请，在
     * {@link ActivityCompat.OnRequestPermissionsResultCallback#onRequestPermissionsResult(
     * int, String[], int[])}里的REQUEST_CODE={@link #PERMISSION_REQUEST_CODE}接收结果;
     * false：没有需要申请的权限
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean requestAllUnGrantedPermission(Activity activity) {
        String[] permissions = getAllPermissions(activity);
        if (permissions != null) {
            List<String> filterPermissions = filterSameGroup(permissions);
            if (filterPermissions != null && filterPermissions.size() > 0) {
                return requestUnGrantedPermission(activity, filterPermissions);
            }
        }
        return false;
    }


    /**
     * 如果你确切知道应用用了哪些危险权限组，可以直接使用这个
     *
     * @param activity
     * @param permissions
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean requestUnGrantedPermission(Activity activity, List<String> permissions) {
        List<String> unGrantedPermissions = findUnGrantedPermissions(activity, permissions);
        if(unGrantedPermissions != null && unGrantedPermissions.size() > 0) {
            requestPermissions(activity, unGrantedPermissions.toArray(new String[unGrantedPermissions.size()]));
            return true;
        }else{
            return false;
        }
    }

    /**
     * 过滤出未准许的权限
     * @param context
     * @param permissions
     * @return
     */
    @TargetApi(Build.VERSION_CODES.M)
    private static List<String> findUnGrantedPermissions(Context context, List<String> permissions) {
        List<String> unGrantedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                unGrantedPermissions.add(permission);
            }
        }
        return unGrantedPermissions;
    }

    /**
     * 请求权限，弹出用户确认框
     * @param context
     * @param permissions
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static void requestPermissions(Activity context, String[] permissions) {
        ActivityCompat.requestPermissions(context,permissions, PERMISSION_REQUEST_CODE);
    }

    /**
     * 过滤掉同组的权限（权限申请只需要申请组内的其中一个，同组成员共同权限）
     * @param permissions
     * @return
     */
    public static List<String> filterSameGroup(String[] permissions) {
        List<String> filter = new ArrayList<>();
        List<Group> groups = new ArrayList<>();
        for (String permission : permissions) {
            Group group = Group.whichGroup(permission);
            if (group != null && !groups.contains(group)) {
                groups.add(group);
                filter.add(permission);
            }
        }
        return filter;
    }

    public static String[] getAllPermissions(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            return packageInfo.requestedPermissions;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Dangerous Permissions Groups
     * 危险权限组
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static enum Group {
        CONTACTS(Manifest.permission_group.CONTACTS,
                new String[]{
                        Manifest.permission.WRITE_CONTACTS,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.GET_ACCOUNTS
                }),

        PHONE(Manifest.permission_group.PHONE,
                new String[]{
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.WRITE_CALL_LOG,
                        Manifest.permission.USE_SIP,
                        Manifest.permission.PROCESS_OUTGOING_CALLS,
                        Manifest.permission.ADD_VOICEMAIL
                }),
        CALENDAR(Manifest.permission_group.CALENDAR,
                new String[]{
                        Manifest.permission.READ_CALENDAR,
                        Manifest.permission.WRITE_CALENDAR
                }),
        CAMERA(Manifest.permission_group.CAMERA,
                new String[]{
                        Manifest.permission.CAMERA
                }),
        SENSORS(Manifest.permission_group.SENSORS,
                new String[]{
                        Manifest.permission.BODY_SENSORS
                }),
        LOCATION(Manifest.permission_group.LOCATION,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }),
        STORAGE(Manifest.permission_group.STORAGE,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }),
        MICROPHONE(Manifest.permission_group.MICROPHONE,
                new String[]{
                        Manifest.permission.RECORD_AUDIO
                }),
        SMS(Manifest.permission_group.SMS,
                new String[]{
                        Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_WAP_PUSH,
                        Manifest.permission.RECEIVE_MMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.BROADCAST_SMS
                }),;

        public String groupName;
        public ArrayList<String> members;

        Group(String groupName, String[] members) {
            this.groupName = groupName;
            this.members = new ArrayList<>();
            this.members.addAll(Arrays.asList(members));
        }

        public static Group whichGroup(String permission) {
            Group[] values = values();
            for (Group group : values) {
                if (group.contain(permission)) {
                    return group;
                }
            }
            return null;
        }

        public static Group parseGroup(String groupName) {
            Group[] values = values();
            for (Group group : values) {
                if (group.groupName.equals(groupName)) {
                    return group;
                }
            }
            return null;
        }

        public boolean contain(String permission) {
            return this.members.contains(permission);
        }
    }
}
