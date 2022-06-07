package com.ericlam.mc.eld;

import com.ericlam.mc.eld.bukkit.CommandNode;
import com.ericlam.mc.eld.managers.ArgumentManager;
import com.ericlam.mc.eld.managers.ItemInteractManager;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class ELDBukkitManagerProvider extends CommonManagerProvider<CommandSender, CommandNode, Listener, JavaPlugin> implements BukkitManagerProvider {

    private final ItemInteractManager itemInteractManager;

    public ELDBukkitManagerProvider(
            ELDServiceCollection<CommandNode, Listener, JavaPlugin> serviceCollection,
            ArgumentManager<CommandSender> argumentManager,
            ItemInteractManager itemInteractManager
    ) {
        super(serviceCollection, argumentManager);
        this.itemInteractManager = itemInteractManager;

    }

    @Override
    public ItemInteractManager getItemInteractManager() {
        return itemInteractManager;
    }
}
