package com.ericlam.mc.eld;

import com.ericlam.mc.eld.components.Configuration;
import com.ericlam.mc.eld.components.GroupConfiguration;
import com.ericlam.mc.eld.components.Overridable;

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
    <T, L extends T> ServiceCollection bindService(Class<T> service, Class<L> implementation);


    /**
     * 覆蓋服務
     * @param service 可覆蓋的服務類 (interface)
     * @param implementation 新的實作
     * @param <T> 可覆蓋的服務
     * @param <L> 實作
     * @return this
     */
    <T extends Overridable, L extends T> ServiceCollection overrideService(Class<T> service, Class<L> implementation);


    /**
     * 註冊服務, 如果已被註冊，則添加新的實作
     * <p></p>
     * 使用依賴注入時，請使用 {@code Set<[Service]> } 進行註冊
     * @param service 服務類 (interface)
     * @param implementation 新的實作
     * @param <T> 服務
     * @param <L> 實作
     * @return this
     */
    <T, L extends T> ServiceCollection addService(Class<T> service, Class<L> implementation);


    /**
     * 註冊含有多重實作方式的服務, 如果先前已有註冊，將直接添加新的實作
     * <p></p>
     * 使用依賴注入時，請使用 {@code Map<String, [Service]> } 進行註冊
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

    /**
     * 新增文件池
     * @param config 文件
     * @param <T> 映射物件類
     * @return this
     */
    <T extends GroupConfiguration> ServiceCollection addGroupConfiguration(Class<T> config);

}
