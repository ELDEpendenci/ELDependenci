package com.ericlam.mc.eld;

import com.ericlam.mc.eld.managers.ItemInteractManager;
import org.bukkit.command.CommandSender;

/**
 * Bukkit 專用的 管理器提供
 */
public interface BukkitManagerProvider extends ManagerProvider<CommandSender>  {


    /**
     * 物品交互管理器
     * @return 物品交互管理器
     */
    ItemInteractManager getItemInteractManager();


}
