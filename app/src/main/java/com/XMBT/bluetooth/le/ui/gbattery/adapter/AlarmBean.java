package com.XMBT.bluetooth.le.ui.gbattery.adapter;

import java.util.List;

/**
 * Created by lzz on 2017/8/28.
 */

public class AlarmBean {


    /**
     * success : true
     * errorCode : 200
     * errorDescribe :
     * rows : [{"id":"94f088d7-8d8c-49f0-a100- 0ff2ba29b776","fullName":"VM02C1242345","speed":0.44,"classify":10,"lon":113.1843512,"lat":23.0985459,"ptime":"2017/8/26  8:45:35","Notea":""},{"id":"9febfcf8-67cd-4469-8c3c- 9b2fdca45c05","fullName":"VM02C1242345","speed":6.41,"classify":10,"lon":113.2367822,"lat":23.1342726,"ptime":"2017/8/25  16:51:29","Notea":""},{"id":"f50fee7d-e1c8-477b-8bb0- 2edb5a6333b4","fullName":"VM02C1242345","speed":1,"classify":10,"lon":113.1840712,"lat":23.0979871,"ptime":"2017/8/24  10:23:22","Notea":""}]
     */

    public String success;
    public String errorCode;
    public String errorDescribe;
    public List<RowsBean> rows;


    public List<RowsBean> getRows() {
        return rows;
    }

    public void setRows(List<RowsBean> rows) {
        this.rows = rows;
    }

    public static class RowsBean {
        /**
         * id : 94f088d7-8d8c-49f0-a100- 0ff2ba29b776
         * fullName : VM02C1242345
         * speed : 0.44
         * classify : 10
         * lon : 113.1843512
         * lat : 23.0985459
         * ptime : 2017/8/26  8:45:35
         * Notea :
         */

        public String id;
        public String fullName;
        public double speed;
        public int classify;
        public double lon;
        public double lat;
        public String ptime;
        public String Notea;


    }
}
