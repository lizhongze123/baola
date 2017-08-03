package com.XMBT.bluetooth.le.bean;

import java.io.Serializable;

/**
 * Created by haowenlee on 2017/2/1.
 */
public class User implements Serializable {
    String id;
    String mds;
    String grade;

    public User() {
    }

    public User(String id, String grade, String mds) {
        this.id = id;
        this.grade = grade;
        this.mds = mds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMds() {
        return mds;
    }

    public void setMds(String mds) {
        this.mds = mds;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
}
