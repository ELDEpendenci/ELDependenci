package com.ericlam.mc.eld;

import com.ericlam.mc.eld.managers.ItemInteractManager;
import org.bukkit.command.CommandSender;

public interface PaperManagerProvider extends ManagerProvider<CommandSender>  {


    /**
     * 物品交互管理器
     * @return 物品交互管理器
     */
    ItemInteractManager getItemInteractManager();


}
