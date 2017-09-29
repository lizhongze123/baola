package com.XMBT.bluetooth.le.event;

/**
 * Created by lzz on 2017/9/26.
 */

public class NotifyEvent {

    public Object value;

    public int tag;

    public NotifyEvent(Object value, int tag){
        this.tag = tag;
        this.value = value;
    }

    public NotifyEvent(int tag){
        this.tag = tag;
    }

    public NotifyEvent(){
    }
}
