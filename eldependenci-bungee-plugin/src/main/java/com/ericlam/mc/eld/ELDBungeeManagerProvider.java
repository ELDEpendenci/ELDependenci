package com.ericlam.mc.eld;

import com.ericlam.mc.eld.components.BungeeCommand;
import com.ericlam.mc.eld.managers.ArgumentManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

public class ELDBungeeManagerProvider extends CommonManagerProvider<CommandSender, BungeeCommand, Listener, Plugin> implements BungeeManageProvider {

    public ELDBungeeManagerProvider(ELDServiceCollection<BungeeCommand, Listener, Plugin> serviceCollection, ArgumentManager<CommandSender> argumentManager) {
        super(serviceCollection, argumentManager);
    }

}
