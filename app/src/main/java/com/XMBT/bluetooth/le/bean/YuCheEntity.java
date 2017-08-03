package com.XMBT.bluetooth.le.bean;

import java.io.Serializable;

/**
 * Created by haowenlee on 2017/4/8.
 */
public class YuCheEntity implements Serializable {
    private int pic;
    private String title;

    public YuCheEntity() {
    }

    public YuCheEntity(int pic, String title) {
        this.pic = pic;
        this.title = title;
    }

    public int getPic() {
        return pic;
    }

    public void setPic(int pic) {
        this.pic = pic;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
