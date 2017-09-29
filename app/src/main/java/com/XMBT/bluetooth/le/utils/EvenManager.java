package com.XMBT.bluetooth.le.utils;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by lzz on 2017/9/26.
 */

public class EvenManager {

        public static void register(Object object) {
            if (!EventBus.getDefault().isRegistered(object)) {
                EventBus.getDefault().register(object);
            }
        }

        public static void unregister(Object object) {
            if (EventBus.getDefault().isRegistered(object)) {
                EventBus.getDefault().unregister(object);
            }
        }

        public static void sendEvent(Object object) {
            EventBus.getDefault().post(object);
        }

}
