package com.ericlam.mc.eld.services;


import com.ericlam.mc.eld.misc.DebugLogger;

/**
 * 日誌輸出服務
 */
public interface LoggingService {

    /**
     * 獲取日誌輸出
     * @param cls 物件類別
     * @return debug 日誌輸出
     */
    DebugLogger getLogger(Class<?> cls);

    /**
     * 獲取日誌輸出
     * @param name 日誌名稱
     * @return debug 日誌輸出
     */
    DebugLogger getLogger(String name);


}
