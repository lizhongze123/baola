package com.XMBT.bluetooth.le.http;

import java.util.List;

/**
 * Created by lzz on 2017/8/9.
 */

public class ApiResponseBean<T> {

    /**
     * {"success":"false","errorCode":"500","errorDescribe":"Fail","data":[]}
     * success : false
     * errorCode : 500
     * errorDescribe : Fail
     * data : []
     */

    public String success;
    public String errorCode;
    public String errorDescribe;
    public T data;

}
