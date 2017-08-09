package com.XMBT.bluetooth.le.bean;

import java.io.Serializable;

public class User implements Serializable {

    /**
     * {"id":"5b872734-750f-4823-a049-5b70fc3277e2",
     * "success":"true",
     * "mds":"47e9d7761b184466af313939e37f35c8",
     * "LoginType":"ENTERPRISE",
     * "grade":8,
     * "msg":"登录成功",
     * "errorCode":200}
     */

    public String id;
    public String mds;
    public String grade;

    public User() {
    }

    public User(String id, String grade, String mds) {
        this.id = id;
        this.grade = grade;
        this.mds = mds;
    }

}
