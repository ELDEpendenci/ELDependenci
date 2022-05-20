package com.ericlam.mc.eld;

import com.ericlam.mc.eld.components.CommandNode;
import com.ericlam.mc.eld.managers.ArgumentManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

public class ELDBungeeManagerProvider extends CommonManagerProvider<CommandSender, CommandNode, Listener, Plugin> implements BungeeManageProvider {

    public ELDBungeeManagerProvider(ELDServiceCollection<CommandNode, Listener, Plugin> serviceCollection, ArgumentManager<CommandSender> argumentManager) {
        super(serviceCollection, argumentManager);
    }

}
