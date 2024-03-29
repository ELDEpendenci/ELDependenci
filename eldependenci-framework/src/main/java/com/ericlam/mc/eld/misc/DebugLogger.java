package com.ericlam.mc.eld.misc;

import java.util.logging.Logger;

/**
 * 可用於 debug 的日誌輸出
 */
public interface DebugLogger {

    /**
     * debug 輸出
     * @param message 訊息
     * @param args 訊息參數 MessageFormat
     */
    void debug(String message, Object... args);

    /**
     * debug 輸出
     * @param throwable 報錯物件
     * @param message 訊息
     * @param args 訊息參數 MessageFormat
     */
    void debug(Throwable throwable, String message, Object... args);


    /**
     * debug 輸出
     * @param message 訊息
     * @param args 訊息參數 String.format
     */
    void debugF(String message, Object... args);

    /**
     * debug 輸出
     * @param throwable 報錯物件
     * @param message 訊息
     * @param args 訊息參數 String.format
     */
    void debugF(Throwable throwable, String message, Object... args);

    /**
     * debug 輸出
     * @param throwable 報錯物件
     */
    void debug(Throwable throwable);

    /**
     * info 輸出
     * @param message 訊息
     * @param args 訊息參數 MessageFormat
     */
    void info(String message, Object... args);

    /**
     * info 輸出
     * @param throwable 報錯物件
     * @param message 訊息
     * @param args 訊息參數 MessageFormat
     */
    void info(Throwable throwable, String message, Object... args);


    /**
     * info 輸出
     * @param message 訊息
     * @param args 訊息參數 String.format
     */
    void infoF(String message, Object... args);

    /**
     * info 輸出
     * @param throwable 報錯物件
     * @param message 訊息
     * @param args 訊息參數 String.format
     */
    void infoF(Throwable throwable, String message, Object... args);

    /**
     * info 輸出
     * @param throwable 報錯物件
     */
    void info(Throwable throwable);

    /**
     * warn 輸出
     * @param message 訊息
     * @param args 訊息參數 MessageFormat
     */
    void warn(String message, Object... args);

    /**
     * warn 輸出
     * @param throwable 報錯物件
     * @param message 訊息
     * @param args 訊息參數 MessageFormat
     */
    void warn(Throwable throwable, String message, Object... args);


    /**
     * warn 輸出
     * @param message 訊息
     * @param args 訊息參數 String.format
     */
    void warnF(String message, Object... args);

    /**
     * warn 輸出
     * @param throwable 報錯物件
     * @param message 訊息
     * @param args 訊息參數 String.format
     */
    void warnF(Throwable throwable, String message, Object... args);

    /**
     * warn 輸出
     * @param throwable 報錯物件
     */
    void warn(Throwable throwable);

    /**
     * 獲取真正的 Logger
     * @return Logger
     */
    Logger getRealLogger();

}
