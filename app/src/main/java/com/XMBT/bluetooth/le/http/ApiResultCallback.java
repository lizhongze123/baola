package com.XMBT.bluetooth.le.http;

import com.XMBT.bluetooth.le.utils.LogUtils;
import com.google.gson.Gson;
import com.lzy.okgo.callback.AbsCallback;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by lzz on 2017/8/9.
 */

public abstract class ApiResultCallback<T> extends AbsCallback<ApiResponseBean<T>> {

    private Gson gson;
    private Type entityClass;

    public abstract void onSuccessResponse(T data);

    public abstract void onFailure(String errorCode);

    public abstract void onFinish();

    public ApiResultCallback(){
        Type genType = getClass().getGenericSuperclass();
        if(genType instanceof ParameterizedType){
            Type[] types = ((ParameterizedType) genType).getActualTypeArguments();
            entityClass = types[0];
            gson = new Gson();
        }
    }

    @Override
    public ApiResponseBean convertSuccess(Response response) throws Exception {
        String str = response.body().string();
        LogUtils.e(str);
        return gson.fromJson(str, ApiResponseBean.class);
    }

    @Override
    public void onSuccess(ApiResponseBean tApiResponseBean, Call call, Response response) {
        if(tApiResponseBean.errorCode.equals("200")){
            T t = gson.fromJson(gson.toJson(tApiResponseBean.data), entityClass);
            onSuccessResponse(t);
        }else{
            //业务错误
            onFailure(tApiResponseBean.errorCode);
        }
        onFinish();
    }

    @Override
    public void onError(Call call, Response response, Exception e) {
        onFailure("-1");
        onFinish();
    }
}
