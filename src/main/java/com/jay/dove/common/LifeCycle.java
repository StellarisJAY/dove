package com.jay.dove.common;

/**
 * <p>
 *  Common lifecycle
 * </p>
 *
 * @author Jay
 * @date 2022/01/07 15:50
 */
public interface LifeCycle {
    /**
     * start
     */
    void startup();

    /**
     * stop
     */
    void shutdown();

    /**
     * is started
     * @return boolean
     */
    boolean isStarted();
}
