package com.XMBT.bluetooth.le.view.LineChart;


public class ItemBean {

    private int value;
    private String tag;
    private long msec;

    public ItemBean(int value, String tag, long msec) {
        this.value = value;
        this.tag = tag;
        this.msec = msec;
    }

    public long getMsec() {
        return msec;
    }

    public void setMsec(long msec) {
        this.msec = msec;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ItemBean{" +
                "value=" + value +
                ", tag='" + tag + '\'' +
                '}';
    }
}
