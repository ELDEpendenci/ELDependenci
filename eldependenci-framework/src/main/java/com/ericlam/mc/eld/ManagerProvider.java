package com.ericlam.mc.eld;

import com.ericlam.mc.eld.managers.ArgumentManager;
import com.ericlam.mc.eld.managers.ConfigStorage;

/**
 * 管理器提供
 * @param <Sender> 平台所屬指令發送者類型
 */
public interface ManagerProvider<Sender> {

    /**
     * 文件配置儲存器
     *
     * @return 文件配置儲存器
     */
    ConfigStorage getConfigStorage();

    /**
     * 參數解析管理器
     *
     * @return 參數解析管理器
     */
    ArgumentManager<Sender> getArgumentManager();


}
