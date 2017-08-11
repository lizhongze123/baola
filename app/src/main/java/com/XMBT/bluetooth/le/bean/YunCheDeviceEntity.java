package com.XMBT.bluetooth.le.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class YunCheDeviceEntity implements Serializable {

    /**
     * {"success":"true",
     * "errorCode":"200",
     * "errorDescribe":"",
     *
     * "data":[
     * {"Id":"c8c8388a-c263-49a3-a401-2d2af5e62f23",
     * "FullName":"VM02C0681320",
     * "Macid":"6170681320",
     * "PlateNumber":null,
     * "EquipmentStatus":0,
     * "EquipmentBetteryVType":1}]}
     */

    @SerializedName("Id")
    public String id;

    @SerializedName("FullName")
    public String fullname;

    @SerializedName("Macid")
    public String macid;

    @SerializedName("PlateNumber")
    public String platenumber;

    @SerializedName("EquipmentStatus")
    public int equipmentStatus;

    @SerializedName("EquipmentBetteryVType")
    public int equipmentBatteryVType;

    public int img;

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

}
