package com.ericlam.mc.eld;

import com.ericlam.mc.eld.bukkit.ELDConfig;
import com.ericlam.mc.eld.bukkit.ELDMessageConfig;
import com.ericlam.mc.eld.bukkit.ItemInteractListener;
import com.ericlam.mc.eld.commands.ELDArgumentManager;
import com.ericlam.mc.eld.commands.ELDCommandHandler;
import com.ericlam.mc.eld.configurations.ELDConfigManager;
import com.ericlam.mc.eld.exceptions.ArgumentParseException;
import com.ericlam.mc.eld.listeners.ELDEventListeners;
import com.ericlam.mc.eld.managers.ArgumentManager;
import com.ericlam.mc.eld.managers.ConfigStorage;
import com.ericlam.mc.eld.managers.ItemInteractManager;
import com.ericlam.mc.eld.services.*;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.craftbukkit.libs.org.apache.commons.codec.binary.Hex;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class ELDependenci extends JavaPlugin implements ELDependenciAPI, Listener {

    private final ELDModule module = new ELDModule(this);
    private final Map<JavaPlugin, ELDServiceCollection> collectionMap = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> customInstallation = new ConcurrentHashMap<>();
    private final ELDArgumentManager argumentManager = new ELDArgumentManager();
    private ELDConfigPoolService configPoolService;
    private ELDLangPoolService langPoolService;
    private ItemInteractListener itemInteractListener;
    private static ELDependenciAPI api;
    private Injector injector;
    // never use dump
    private final ELDConfigManager eldConfigManager = new ELDConfigManager(null, this);
    private boolean disabled = false;
    private boolean sharePluginInstance = false;
    private ELDMessageConfig eldMessageConfig;

    @Override
    public void onLoad() {
        api = this;
        this.itemInteractListener = new ItemInteractListener(this);
        this.module.bindInstance(ArgParserService.class, argumentManager);
        eldConfigManager.loadConfig(ELDConfig.class);
        eldConfigManager.loadConfig(ELDMessageConfig.class);
        this.eldMessageConfig = eldConfigManager.getConfigAs(ELDMessageConfig.class);
        ELDCommandHandler.setMsg(eldMessageConfig);
        var eldConfig = eldConfigManager.getConfigAs(ELDConfig.class);
        this.module.setDefaultSingleton(eldConfig.defaultSingleton);
        this.sharePluginInstance = eldConfig.sharePluginInstance;
    }

    public static ELDependenciAPI getApi() {
        return Optional.ofNullable(api).orElseThrow(() -> new IllegalStateException("ELDependencies has not yet loaded，make sure your plugin.yml has added eld-plugin as depend"));
    }

    public ManagerProvider register(ELDBukkit plugin, Consumer<ServiceCollection> injector) {
        if (collectionMap.containsKey(plugin)) {
            throw new IllegalStateException("the plugin is registered and not allowed to be registered again.");
        }
        var collection = new ELDServiceCollection(module, plugin, customInstallation);
        injector.accept(collection);
        if (sharePluginInstance) module.mapPluginInstance(plugin);
        module.bindPluginInstance(plugin.getClass(), plugin);
        collection.configManager.dumpAll();
        this.collectionMap.put(plugin, collection);
        return new ELDManagerProvider(collection);
    }

    @Override
    public <T> T exposeService(Class<T> serviceCls) {
        return injector.getInstance(serviceCls);
    }

    @Override
    public void onEnable() {
        try {
            registerParser();
            getServer().getPluginManager().registerEvents(itemInteractListener, this);
            this.injector = Guice.createInjector(module);
            configPoolService = (ELDConfigPoolService) injector.getInstance(ConfigPoolService.class);
            langPoolService = (ELDLangPoolService) injector.getInstance(LanguagePoolService.class);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error while enabling ELDependenci: ", e);
            getLogger().log(Level.SEVERE, "Disabling plugin...");
            this.disabled = true;
        }
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPluginEnable(final PluginEnableEvent e) {
        if (!(e.getPlugin() instanceof JavaPlugin)) return;
        var plugin = (JavaPlugin) e.getPlugin();
        var services = collectionMap.get(plugin);
        if (services == null) return; // not eld plugin

        if (disabled) {
            plugin.getLogger().log(Level.SEVERE, "This plugin is disabled due to the disabled of ELDependenci");
            return;
        }

        var configManager = services.configManager;

        configPoolService.dumpAll(configManager.getConfigPoolMap()); // insert all config pool
        configManager.getConfigPoolMap().keySet().forEach(type -> configPoolService.addGroupConfigLoader(type, configManager::loadOneGroupConfig));// insert config group loader

        langPoolService.dumpAll(configManager.getLangPoolMap()); // insert all lang config pool
        configManager.getLangPoolMap().keySet().forEach(type -> langPoolService.addGroupConfigLoader(type, configManager::loadOneLangConfig)); // insert lang group config loader

        configManager.setInjector(injector);

        injector.injectMembers(services.lifeCycleHook);

        services.lifeCycleHook.onEnable(plugin);

        //register command
        if (!services.commands.isEmpty()) {
            plugin.getLogger().info("Registering all of the commands of plugin " + plugin.getName());
            ELDCommandHandler.registers(plugin, services.commands, injector, argumentManager);
        }

        //register listener
        if (!services.listeners.isEmpty() || !services.eldListeners.isEmpty()) {
            plugin.getLogger().info("Registering all of the listeners of plugin " + plugin.getName());
            services.listeners.forEach(listenerCls -> {
                var listener = injector.getInstance(listenerCls);
                plugin.getServer().getPluginManager().registerEvents(listener, plugin);
            });

            if (!services.eldListeners.isEmpty()) {
                var eventListeners = new ELDEventListeners();
                services.eldListeners.forEach(eldListenerCls -> {
                    var eldListener = injector.getInstance(eldListenerCls);
                    eldListener.defineNodes(eventListeners);
                });
                var pluginManager = plugin.getServer().getPluginManager();
                eventListeners.getSubscribersMap().forEach((eventCls, subscribersList) -> {
                    subscribersList.stream()
                            .collect(Collectors.groupingByConcurrent(ELDEventListeners.ELDEventSubscriber::getPriority))
                            .forEach(((priority, subscribers) -> {
                                var anonymousListener = new Listener() {
                                };
                                pluginManager.registerEvent(eventCls, anonymousListener, priority, (listener, event) -> {
                                    subscribers.forEach(sub -> sub.invoke(event));
                                }, plugin);
                            }));
                });
            }
        }
    }

    @EventHandler
    public void onPluginDisable(final PluginDisableEvent e) {
        if (!(e.getPlugin() instanceof JavaPlugin)) return;
        var plugin = (JavaPlugin) e.getPlugin();
        var services = collectionMap.get(plugin);
        if (services == null) return; // not eld plugin
        if (disabled) return;
        services.lifeCycleHook.onDisable(plugin);

    }


    private class ELDManagerProvider implements ManagerProvider {

        private final ELDServiceCollection collection;

        private ELDManagerProvider(ELDServiceCollection collection) {
            this.collection = collection;
        }

        @Override
        public ConfigStorage getConfigStorage() {
            return Optional.ofNullable(collection.configManager).orElseThrow(() -> new IllegalStateException("plugin not registered"));
        }

        @Override
        public ArgumentManager getArgumentManager() {
            return argumentManager;
        }

        @Override
        public ItemInteractManager getItemInteractManager() {
            return itemInteractListener;
        }
    }

    private void registerParser() {
        argumentManager.registerParser(Integer.class, (args, sender, parser) -> {
            var num = args.next();
            try {
                return Integer.parseInt(num);
            } catch (NumberFormatException e) {
                throw new ArgumentParseException(eldMessageConfig.getConvertError("int", num));
            }
        });
        argumentManager.registerParser(Double.class, (args, sender, parser) -> {
            var num = args.next();
            try {
                return Double.parseDouble(num);
            } catch (NumberFormatException e) {
                throw new ArgumentParseException(eldMessageConfig.getConvertError("double", num));
            }
        });

        argumentManager.registerParser(Long.class, (args, sender, parser) -> {
            var num = args.next();
            try {
                return Long.parseLong(num);
            } catch (NumberFormatException e) {
                throw new ArgumentParseException(eldMessageConfig.getConvertError("long", num));
            }
        });

        argumentManager.registerParser(Byte.class, (args, sender, parser) -> {
            var num = args.next();
            try {
                return Byte.parseByte(num);
            } catch (NumberFormatException e) {
                throw new ArgumentParseException(eldMessageConfig.getConvertError("byte", num));
            }
        });

        argumentManager.registerParser(Short.class, (args, sender, parser) -> {
            var num = args.next();
            try {
                return Short.parseShort(num);
            } catch (NumberFormatException e) {
                throw new ArgumentParseException(eldMessageConfig.getConvertError("short", num));
            }
        });

        argumentManager.registerParser(Float.class, (args, sender, parser) -> {
            var num = args.next();
            try {
                return Float.parseFloat(num);
            } catch (NumberFormatException e) {
                throw new ArgumentParseException(eldMessageConfig.getConvertError("float", num));
            }
        });

        argumentManager.registerParser(Character.class, (args, sender, parser) -> args.next().charAt(0));
        argumentManager.registerParser(Boolean.class, (args, sender, parser) -> Boolean.parseBoolean(args.next()));
        argumentManager.registerParser(String.class, (args, sender, parser) -> args.next());
        argumentManager.registerParser(Player.class, (args, sender, parser) -> {
            var player = Bukkit.getPlayer(args.next());
            if (player == null) {
                throw new ArgumentParseException(eldMessageConfig.getConvertError("player-not-online"));
            }
            return player;
        });

        argumentManager.registerParser(OfflinePlayer.class, (args, sender, parser) -> {
            var uuid = Bukkit.getPlayerUniqueId(args.next());
            if (uuid == null) {
                throw new ArgumentParseException(eldMessageConfig.getConvertError("player-not-exist"));
            }
            return Bukkit.getOfflinePlayer(uuid);
        });

        argumentManager.registerParser(Location.class, (args, sender, parser) -> {
            World world;
            if (!(sender instanceof Player)) {
                world = Bukkit.getWorld(args.next());
            } else {
                world = ((Player) sender).getWorld();
            }
            if (world == null) {
                throw new ArgumentParseException(eldMessageConfig.getConvertError("unknown-world"));
            }
            var x = parser.tryParse(Double.class, args, sender);
            var y = parser.tryParse(Double.class, args, sender);
            var z = parser.tryParse(Double.class, args, sender);
            return new Location(world, x, y, z);
        });

        argumentManager.registerParser(UUID.class, (args, sender, parser) -> {
            try {
                return UUID.fromString(args.next());
            } catch (IllegalArgumentException e) {
                throw new ArgumentParseException(eldMessageConfig.getConvertError("error-uuid", e.getMessage()));
            }
        });


        // named parser

        argumentManager.registerParser(String.class, "message", (args, sender, parser) -> {
            var builder = new StringBuilder();
            args.forEachRemaining(s -> builder.append(s).append(" "));
            return builder.toString();
        });
        argumentManager.registerParser(String.class, "sha-256", (arg, sender, parser) -> {
            var str = arg.next();
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                var b = digest.digest(str.getBytes());
                return Hex.encodeHexString(b);
            } catch (NoSuchAlgorithmException e) {
                throw new ArgumentParseException(e.getMessage());
            }
        });

    }
}
