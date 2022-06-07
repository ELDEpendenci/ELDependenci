package com.ericlam.mc.eld;

import java.io.File;
import java.util.logging.Logger;

/**
 * 平台插件，用於不同平台的擴展
 */
public interface MCPlugin {

    /**
     * 該平台插件所使用的 Logger
     * @return Logger
     */
    Logger getLogger();

    /**
     * 該平台插件所在的資料夾
     * @return 資料夾
     */
    File getDataFolder();

    /**
     * 取得該平台插件的名稱
     * @return 名稱
     */
    String getName();

    /**
     * 該平台插件的複製資源方式
     *
     * @param path 資源路徑
     */
    void saveResource(String path);
}
