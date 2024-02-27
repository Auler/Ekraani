package com.lf.app;

/**
 * 工具
 * @author auler
 * @date 2024-2-26
 */
public class Utils {

    /**
     * 获取时间 00:00:00
     * @param time
     * @return
     */
    public static String getHMS(long time) {
        long hour = time / 3600000;
        long minute = (time % 3600000) / 60000;
        long second = (time % 60000) / 1000;
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }
}
