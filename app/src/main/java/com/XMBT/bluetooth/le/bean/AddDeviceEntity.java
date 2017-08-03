package com.XMBT.bluetooth.le.bean;

import java.io.Serializable;

/**
 * 设备实体类
 */

public class AddDeviceEntity implements Serializable{

    /**
     * 设备名字
     */
    String deviceName;
    /**
     * 标题
     */
    String title;
    /**
     * 图片
     */
    int img;
    /**
     * 状态
     */
    int status;

    public AddDeviceEntity() {}

    public AddDeviceEntity(String deviceName, int img, String title, int status) {
        this.deviceName = deviceName;
        this.img = img;
        this.title = title;
        this.status = status;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "AddDeviceEntity{" +
                "deviceName='" + deviceName + '\'' +
                ", title='" + title + '\'' +
                ", img=" + img +
                ", status=" + status +
                '}';
    }
}
