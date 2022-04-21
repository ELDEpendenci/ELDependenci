package com.ericlam.mc.eld;

import com.ericlam.mc.eld.managers.ArgumentManager;
import com.ericlam.mc.eld.managers.ConfigStorage;
import com.ericlam.mc.eld.managers.ItemInteractManager;

/**
 * 管理器提供
 */
public interface ManagerProvider {

    /**
     * 文件配置儲存器
     * @return 文件配置儲存器
     */
    ConfigStorage getConfigStorage();

    /**
     * 參數解析管理器
     * @return 參數解析管理器
     */
    ArgumentManager getArgumentManager();

    /**
     * 物品交互管理器
     * @return 物品交互管理器
     */
    ItemInteractManager getItemInteractManager();

}
