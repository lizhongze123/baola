package com.XMBT.bluetooth.le;

import java.io.Serializable;

/**
 * Created by haowenlee on 2017/4/10.
 */
public class YunCheDeviceEntity implements Serializable {
    private String id;
    private String fullname;
    private String macid;
    private String platenumber;
    private int equipmentStatus;
    private int equipmentBatteryVType;
    private int img;

    public YunCheDeviceEntity() {
    }

    public YunCheDeviceEntity(String id, String fullname, String macid, String platenumber, int equipmentStatus, int equipmentBatteryVType, int img) {
        this.id = id;
        this.fullname = fullname;
        this.macid = macid;
        this.platenumber = platenumber;
        this.equipmentStatus = equipmentStatus;
        this.equipmentBatteryVType = equipmentBatteryVType;
        this.img = img;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getMacid() {
        return macid;
    }

    public void setMacid(String macid) {
        this.macid = macid;
    }

    public String getPlatenumber() {
        return platenumber;
    }

    public void setPlatenumber(String platenumber) {
        this.platenumber = platenumber;
    }

    public int getEquipmentStatus() {
        return equipmentStatus;
    }

    public void setEquipmentStatus(int equipmentStatus) {
        this.equipmentStatus = equipmentStatus;
    }

    public int getEquipmentBatteryVType() {
        return equipmentBatteryVType;
    }

    public void setEquipmentBatteryVType(int equipmentBatteryVType) {
        this.equipmentBatteryVType = equipmentBatteryVType;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }
}
