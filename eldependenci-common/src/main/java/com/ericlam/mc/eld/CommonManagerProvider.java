package com.ericlam.mc.eld;

import com.ericlam.mc.eld.common.CommonCommandNode;
import com.ericlam.mc.eld.managers.ArgumentManager;
import com.ericlam.mc.eld.managers.ConfigStorage;

public abstract class CommonManagerProvider<Sender, CommandNode extends CommonCommandNode<Sender>, Listener, Plugin> implements ManagerProvider<Sender> {

    private final ELDServiceCollection<CommandNode, Listener, Plugin> serviceCollection;
    private final ArgumentManager<Sender> argumentManager;

    public CommonManagerProvider(ELDServiceCollection<CommandNode, Listener, Plugin> serviceCollection, ArgumentManager<Sender> argumentManager) {
        this.serviceCollection = serviceCollection;
        this.argumentManager= argumentManager;
    }

    @Override
    public ConfigStorage getConfigStorage() {
        return serviceCollection.configManager;
    }

    @Override
    public ArgumentManager<Sender> getArgumentManager() {
        return argumentManager;
    }
}
