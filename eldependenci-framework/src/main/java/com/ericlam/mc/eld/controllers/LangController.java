package com.ericlam.mc.eld.controllers;

import java.util.List;

/**
 * 訊息文件控制器
 */
public interface LangController {

    /**
     * 獲取訊息
     * @param node 節點
     * @return 包含前綴的訊息
     */
    String get(String node);

    /**
     * 獲取訊息，連帶訊息參數 {0} {1} {2}...
     * @param node 節點
     * @param args 訊息參數
     * @return 包含前綴的訊息
     */
    String get(String node, Object... args);

    /**
     * 獲取無前綴的訊息
     * @param node 節點
     * @return 無前綴訊息
     */
    String getPure(String node);

    /**
     * 獲取無前綴的訊息，連帶訊息參數 {0} {1} {2}...
     * @param node 節點
     * @param args 訊息參數
     * @return 無前綴訊息
     */
    String getPure(String node, Object... args);

    /**
     * 獲取訊息列表
     * @param node 節點
     * @return 含前綴訊息列表
     */
    List<String> getList(String node);


    /**
     * 獲取無前綴訊息列表
     * @param node 節點
     * @return 無前綴訊息列表
     */
    List<String> getPureList(String node);

    /**
     * 獲取前綴
     * @return 前綴
     */
    String getPrefix();

}
