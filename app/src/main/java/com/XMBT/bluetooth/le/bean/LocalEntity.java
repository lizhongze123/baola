package com.XMBT.bluetooth.le.bean;

import java.io.Serializable;

public class LocalEntity implements Serializable {

    public long sys_time;
    public String user_name;
    public double jingdu;
    public double weidu;
    public double ljingdu;
    public double lweidu;
    public long datetime;
    public long heart_time;
    public int su;
    public String status;
    public int hangxiang;
    public String sim_id;
    public String user_id;
    public String sale_type;
    public String iconType;
    public long server_time;
    public String product_type;
    public long expire_date;
    public String group_id;
    public String statusnumber;
    public double eletric;

    public LocalEntity() {
    }

    public LocalEntity(long sys_time, String user_name, double jingdu, double weidu, double ljingdu, double lweidu, long datetime, long heart_time, int su, int hangxiang, String sim_id, String user_id, String iconType, String sale_type, String status, long server_time, String product_type, long expire_date, String group_id, String statusnumber, double eletric) {
        this.sys_time = sys_time;
        this.user_name = user_name;
        this.jingdu = jingdu;
        this.weidu = weidu;
        this.ljingdu = ljingdu;
        this.lweidu = lweidu;
        this.datetime = datetime;
        this.heart_time = heart_time;
        this.su = su;
        this.hangxiang = hangxiang;
        this.sim_id = sim_id;
        this.user_id = user_id;
        this.iconType = iconType;
        this.sale_type = sale_type;
        this.status = status;
        this.server_time = server_time;
        this.product_type = product_type;
        this.expire_date = expire_date;
        this.group_id = group_id;
        this.statusnumber = statusnumber;
        this.eletric = eletric;
    }
}
