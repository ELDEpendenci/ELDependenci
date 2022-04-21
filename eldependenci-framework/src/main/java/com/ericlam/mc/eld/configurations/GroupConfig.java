package com.ericlam.mc.eld.configurations;

import com.ericlam.mc.eld.components.GroupConfiguration;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 文件池。內置快取功能，需要使用 fetch 方法來清除快取
 * @param <T> 文件池類別
 */
public interface GroupConfig<T extends GroupConfiguration> {

    /**
     *
     * @return 所有文件實例
     */
    List<T> findAll();

    /**
     * 過濾路徑
     * @param filter 自定義過濾
     * @return 所有符合特定條件的實例
     */
    List<T> findAll(Predicate<Path> filter);

    /**
     * 獲取指定頁數內的所有文件實例
     * @param pageRequest 頁面請求
     * @return 頁面
     */
    Page<T> findAll(PageRequest pageRequest);

    /**
     * 根據 id 尋找文件實例
     * @param id 標識 id
     * @return 文件實例
     */
    Optional<T> findById(String id);

    /**
     * 保存一個文件，標識 id 不能為 null
     * @param config 文件實例
     */
    void save(T config);

    /**
     * 透過 id 刪除文件
     * @param id 標識 id
     * @return 刪除成功
     */
    boolean deleteById(String id);

    /**
     * 獲取文件池總數量
     * @return 數量
     */
    long totalSize();

    /**
     * 刪除文件
     * @param config 文件實例，id 不能為 null
     * @return 刪除成功
     */
    boolean delete(T config);

    /**
     * 清楚所有快取
     */
    void fetch();

    /**
     * 清楚指定文件的快取
     * @param id 標識 id
     */
    void fetchById(String id);

}
