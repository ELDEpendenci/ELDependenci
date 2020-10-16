package com.ericlam.mc.eld;

import com.ericlam.mc.eld.components.Configuration;

import java.util.Map;

/**
 * 服務註冊器
 */
public interface ServiceCollection {

    /**
     * 註冊單例
     * @param singleton 單例
     * @return this
     */
    ServiceCollection addSingleton(Class<?> singleton);

    /**
     * 註冊服務
     * @param service 服務類 (interface)
     * @param implementation 該服務的實作類
     * @param <T> 服務
     * @param <L> 實作
     * @return this
     */
    <T, L extends T> ServiceCollection addService(Class<T> service, Class<L> implementation);

    /**
     * 註冊含有多重實作方式的服務
     * @param service 服務類 (interface)
     * @param implementations 包含標識id的多重實作類比
     * @param <T> 服務
     * @return this
     */
    <T> ServiceCollection addServices(Class<T> service, Map<String, Class<? extends T>> implementations);

    /**
     * 新增文件配置
     * @param config 文件映射物件類
     * @param <T> 映射物件類
     * @return this
     */
    <T extends Configuration> ServiceCollection addConfiguration(Class<T> config);

}
