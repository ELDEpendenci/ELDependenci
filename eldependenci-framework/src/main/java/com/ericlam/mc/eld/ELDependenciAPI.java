package com.ericlam.mc.eld;

import java.util.function.Consumer;

/**
 * ELDependenci API
 */
public interface ELDependenciAPI {

    /**
     * 不應手動註冊，而應繼承 {@link ELDPlugin} 取代註冊
     *
     * @param plugin   eld 插件
     * @param injector 註冊服務
     * @return 管理器提供
     */
    ManagerProvider<?> register(ELDPlugin plugin, Consumer<ServiceCollection> injector);


    /**
     * 對外提供接口服務。主要用於框架以外的插件想要存取本插件的服務的時候
     * @param serviceCls 服務接口
     * @param <T> 服務類
     * @return 服務, 如無則報錯
     */
    <T> T exposeService(Class<T> serviceCls);

}
