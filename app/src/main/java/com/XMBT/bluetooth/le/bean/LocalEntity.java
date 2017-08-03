package com.XMBT.bluetooth.le.bean;

import java.io.Serializable;

/**
 * Created by haowenlee on 2017/4/18.
 */
public class LocalEntity implements Serializable {
    long sys_time;
    String user_name;
    double jingdu;
    double weidu;
    double ljingdu;
    double lweidu;
    long datetime;
    long heart_time;
    int su;
    String status;
    int hangxiang;
    String sim_id;
    String user_id;
    String sale_type;
    String iconType;
    long server_time;
    String product_type;
    long expire_date;
    String group_id;
    String statusnumber;
    double eletric;

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

    public long getSys_time() {
        return sys_time;
    }

    public void setSys_time(long sys_time) {
        this.sys_time = sys_time;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public double getJingdu() {
        return jingdu;
    }

    public void setJingdu(double jingdu) {
        this.jingdu = jingdu;
    }

    public double getWeidu() {
        return weidu;
    }

    public void setWeidu(double weidu) {
        this.weidu = weidu;
    }

    public double getLjingdu() {
        return ljingdu;
    }

    public void setLjingdu(double ljingdu) {
        this.ljingdu = ljingdu;
    }

    public double getLweidu() {
        return lweidu;
    }

    public void setLweidu(double lweidu) {
        this.lweidu = lweidu;
    }

    public long getDatetime() {
        return datetime;
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }

    public long getHeart_time() {
        return heart_time;
    }

    public void setHeart_time(long heart_time) {
        this.heart_time = heart_time;
    }

    public int getSu() {
        return su;
    }

    public void setSu(int su) {
        this.su = su;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getHangxiang() {
        return hangxiang;
    }

    public void setHangxiang(int hangxiang) {
        this.hangxiang = hangxiang;
    }

    public String getSim_id() {
        return sim_id;
    }

    public void setSim_id(String sim_id) {
        this.sim_id = sim_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getSale_type() {
        return sale_type;
    }

    public void setSale_type(String sale_type) {
        this.sale_type = sale_type;
    }

    public String getIconType() {
        return iconType;
    }

    public void setIconType(String iconType) {
        this.iconType = iconType;
    }

    public long getServer_time() {
        return server_time;
    }

    public void setServer_time(long server_time) {
        this.server_time = server_time;
    }

    public String getProduct_type() {
        return product_type;
    }

    public void setProduct_type(String product_type) {
        this.product_type = product_type;
    }

    public long getExpire_date() {
        return expire_date;
    }

    public void setExpire_date(long expire_date) {
        this.expire_date = expire_date;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getStatusnumber() {
        return statusnumber;
    }

    public void setStatusnumber(String statusnumber) {
        this.statusnumber = statusnumber;
    }

    public double getEletric() {
        return eletric;
    }

    public void setEletric(double eletric) {
        this.eletric = eletric;
    }
}
