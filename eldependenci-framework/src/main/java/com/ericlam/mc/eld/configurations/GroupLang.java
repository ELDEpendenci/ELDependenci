package com.ericlam.mc.eld.configurations;

import com.ericlam.mc.eld.components.GroupLangConfiguration;

import java.util.Optional;

/**
 * 語言文件池，性質大致跟 {@link GroupConfig} 相同，但只提供唯讀操作
 * @param <T> 語言文件池類別
 */
public interface GroupLang<T extends GroupLangConfiguration> {

    /**
     * 根據 語言 id 獲取語言文件實例
     * @param locale 語言 id
     * @return 語言文件實例
     */
    Optional<T> getByLocale(String locale);

    /**
     * 獲取默認語言文件實例
     * @return 默認語言文件實例
     */
    T getDefault();

    /**
     * 根據 語言 id 清除快取
     * @param locale 語言 id
     */
    void fetchById(String locale);

    /**
     * 清除所有快取
     */
    void fetch();


}
