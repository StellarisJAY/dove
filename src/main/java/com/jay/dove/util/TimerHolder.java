package com.jay.dove.util;

import io.netty.util.HashedWheelTimer;

/**
 * <p>
 *  Singleton HashedWheelTimer
 * </p>
 *
 * @author Jay
 * @date 2022/01/13 14:38
 */
public class TimerHolder {
    static HashedWheelTimer INSTANCE = new HashedWheelTimer();

    public static HashedWheelTimer getTimer(){
        return INSTANCE;
    }
}
