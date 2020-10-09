package com.ericlam.mc.eld;

import com.ericlam.mc.eld.commands.ELDArgumentManager;
import com.ericlam.mc.eld.commands.ELDCommandHandler;
import com.ericlam.mc.eld.configurations.ConfigStorage;
import com.ericlam.mc.eld.managers.ArgumentManager;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ELDependenci extends JavaPlugin implements ELDependenciAPI {

    private final ELDModule module = new ELDModule(this);
    private final Map<JavaPlugin, ELDServiceCollection> collectionMap = new ConcurrentHashMap<>();
    private final ELDArgumentManager argumentManager = new ELDArgumentManager();
    private static ELDependenciAPI api;

    @Override
    public void onLoad() {
        api = this;
    }

    public static ELDependenciAPI getApi() {
        return api;
    }

    public ManagerProvider register(JavaPlugin plugin, Consumer<ServiceCollection> injector) {
        var collection = new ELDServiceCollection(module, plugin);
        injector.accept(collection);
        collection.configManager.dumpAll();
        this.collectionMap.put(plugin, collection);
        return new ELDManagerProvider(collection);
    }

    @Override
    public void onEnable() {
        Bukkit.getScheduler().runTask(this, () -> {
            Injector injector = Guice.createInjector(module);
            collectionMap.forEach((plugin, services) -> {
                services.configManager.setInjector(injector);
                ELDCommandHandler.registers(plugin, services.commands, injector, argumentManager);
            });
        });
    }

    private class ELDManagerProvider implements ManagerProvider{

        private final ELDServiceCollection collection;

        private ELDManagerProvider(ELDServiceCollection collection) {
            this.collection = collection;
        }

        @Override
        public ConfigStorage getConfigStorage() {
            return Optional.ofNullable(collection.configManager).orElseThrow(() -> new IllegalStateException("插件未註冊"));
        }

        @Override
        public ArgumentManager getArgumentManager() {
            return argumentManager;
        }
    }
}
