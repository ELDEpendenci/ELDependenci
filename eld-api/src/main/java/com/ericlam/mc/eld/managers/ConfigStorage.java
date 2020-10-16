package com.ericlam.mc.eld.managers;

import com.ericlam.mc.eld.components.Configuration;

/**
 * 配置文件儲存器
 */
public interface ConfigStorage {

    /**
     * 獲取映射物件
     * @param config 文件映射物件類別
     * @param <T> 映射物件類別
     * @return 文件映射物件
     */
    <T extends Configuration> T getConfigAs(Class<T> config);

}
