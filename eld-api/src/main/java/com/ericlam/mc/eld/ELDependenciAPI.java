package com.ericlam.mc.eld;

import java.util.function.Consumer;

/**
 * ELDependenci API
 */
public interface ELDependenciAPI {

    /**
     * 不應手動註冊，而應繼承 {@link ELDBukkitPlugin} 取代註冊
     * @param plugin eld 插件
     * @param injector 註冊服務
     * @return 管理器提供
     */
    ManagerProvider register(ELDBukkitPlugin plugin, Consumer<ServiceCollection> injector);

}
