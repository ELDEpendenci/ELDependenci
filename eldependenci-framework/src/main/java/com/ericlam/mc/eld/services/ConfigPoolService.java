package com.ericlam.mc.eld.services;

import com.ericlam.mc.eld.components.GroupConfiguration;
import com.ericlam.mc.eld.components.GroupLangConfiguration;
import com.ericlam.mc.eld.configurations.GroupConfig;
import com.ericlam.mc.eld.configurations.GroupLang;

/**
 * 文件池服務
 */
public interface ConfigPoolService {

    /**
     * 獲取 指定類型 的文件池
     * @param type 指定類型
     * @param <T> 文件組類別
     * @return 文件池
     */
    <T extends GroupConfiguration> GroupConfig<T> getGroupConfig(Class<T> type);

    /**
     * 獲取 指定類型 的語言文件池
     * @param type 指定類型
     * @param <T> 語言文件池類別
     * @return 語言文件池
     */
    <T extends GroupLangConfiguration> GroupLang<T> getGroupLang(Class<T> type);

}
