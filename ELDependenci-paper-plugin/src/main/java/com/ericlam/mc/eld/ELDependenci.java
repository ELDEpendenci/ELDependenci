package com.ericlam.mc.eld;

import com.ericlam.mc.eld.annotations.CommandArg;
import com.ericlam.mc.eld.annotations.DynamicArg;
import com.ericlam.mc.eld.configurations.BukkitBeanModifier;
import com.ericlam.mc.eld.implement.ELDConfig;
import com.ericlam.mc.eld.bukkit.ItemInteractListener;
import com.ericlam.mc.eld.commands.*;
import com.ericlam.mc.eld.components.CommandNode;
import com.ericlam.mc.eld.configurations.ELDConfigManager;
import com.ericlam.mc.eld.configurations.MessageYaml;
import com.ericlam.mc.eld.exceptions.ArgumentParseException;
import com.ericlam.mc.eld.implement.ELDMessageConfig;
import com.ericlam.mc.eld.managers.ArgumentManager;
import com.ericlam.mc.eld.managers.ConfigStorage;
import com.ericlam.mc.eld.managers.ItemInteractManager;
import com.ericlam.mc.eld.module.ELDConfigModule;
import com.ericlam.mc.eld.module.ELDLoggingModule;
import com.ericlam.mc.eld.services.ArgParserService;
import com.ericlam.mc.eld.services.ELDConfigPoolService;
import com.ericlam.mc.eld.services.ELDReflectionService;
import com.ericlam.mc.eld.services.logging.ELDLoggingService;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import net.kyori.adventure.text.TextComponent;
import org.apache.commons.codec.binary.Hex;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class ELDependenci extends JavaPlugin implements ELDependenciAPI, Listener, MCPlugin, AddonInstallation, ConfigHandler {

    private final ELDCommonModule module = new ELDCommonModule(this);
    private final ELDPaperModule paperModule = new ELDPaperModule();

    private final Map<JavaPlugin, PaperServiceCollection> collectionMap = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> customInstallation = new ConcurrentHashMap<>();
    private final ELDArgumentManager<CommandSender> argumentManager = new ELDArgumentManager<>();
    private ELDConfigPoolService groupConfigService;
    private ItemInteractListener itemInteractListener;
    private static ELDependenciAPI api;
    private Injector injector;
    // never use dump
    private final ELDConfigManager eldConfigManager = new ELDConfigManager(null, this, this);
    private boolean disabled = false;
    private boolean sharePluginInstance = false;

    private CommandProcessor<CommandSender, CommandNode> commandProcessor;
    private ELDMessageConfig eldMessageConfig;


    static {
        ELDConfigManager.YAML_MAPPER.registerModule(new SimpleModule()
                .setDeserializerModifier(new BukkitBeanModifier.Deserializer())
                .setSerializerModifier(new BukkitBeanModifier.Serializer()));
        ELDConfigManager.JSON_MAPPER.registerModule(new SimpleModule()
                .setDeserializerModifier(new BukkitBeanModifier.Deserializer())
                .setSerializerModifier(new BukkitBeanModifier.Serializer()));
    }

    @Override
    public void onLoad() {
        api = this;
        this.itemInteractListener = new ItemInteractListener(this);
        this.module.bindInstance(ArgParserService.class, argumentManager);
        eldConfigManager.loadConfig(ELDConfig.class);
        eldConfigManager.loadConfig(ELDMessageConfig.class);
        this.eldMessageConfig = eldConfigManager.getConfigAs(ELDMessageConfig.class);
        var eldConfig = eldConfigManager.getConfigAs(ELDConfig.class);
        groupConfigService = new ELDConfigPoolService(eldConfig.fileWalker, this);
        this.module.setDefaultSingleton(eldConfig.defaultSingleton);
        this.sharePluginInstance = eldConfig.sharePluginInstance;
        this.module.addModule(new ELDConfigModule(groupConfigService, new ELDReflectionService()));
        this.module.addModule(new ELDLoggingModule(new ELDLoggingService(eldConfig, Bukkit.getLogger())));
        this.customInstallation(AddonInstallation.class, this);
        this.installModule(paperModule);
    }

    public static ELDependenciAPI getApi() {
        return Optional.ofNullable(api).orElseThrow(() -> new IllegalStateException("ELDependencies has not yet loaded，make sure your plugin.yml has added eld-plugin as depend"));
    }

    public ManagerProvider<?> register(ELDPlugin eld, Consumer<ServiceCollection> injector) {
        if (!(eld instanceof ELDPaperPlugin plugin))
            throw new IllegalStateException("ELDependencies only support ELDPaperPlugin");
        if (collectionMap.containsKey(plugin)) {
            throw new IllegalStateException("the plugin is registered and not allowed to be registered again.");
        }
        var collection = new PaperServiceCollection(module, plugin, customInstallation, this);
        injector.accept(collection);
        if (sharePluginInstance) paperModule.mapPluginInstance(plugin);
        module.bindPluginInstance(plugin.getClass(), plugin);
        collection.configManager.getGroupConfigSet().forEach(gc -> groupConfigService.addTypeMapper(gc, plugin));
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
            this.commandProcessor = new ELDCommandProcessor<>(injector, argumentManager, eldMessageConfig) {
                @Override
                public CommonCommandSender toSender(CommandSender commandSender) {
                    return new CommonCommandSender() {
                        @Override
                        public boolean hasPermission(String permission) {
                            return commandSender.hasPermission(permission);
                        }

                        @Override
                        public void sendMessage(String message) {
                            commandSender.sendMessage(message);
                        }

                        @Override
                        public void sendMessage(String[] messages) {
                            commandSender.sendMessage(messages);
                        }

                        @Override
                        public void sendMessage(TextComponent component) {
                            commandSender.sendMessage(component);
                        }

                        @Override
                        public boolean isPlayer() {
                            return commandSender instanceof Player;
                        }
                    };
                }
            };
            commandProcessor.registerArgAHandle(
                    CommandArg.class,
                    (annotation, type, argumentManager, iterator, sender) -> argumentManager.tryParse(type, annotation.identifier(), iterator, sender),
                    (annotation) -> new ELDCommandArgsHandler.CommonProperties(annotation.order(), annotation.optional(), annotation.labels())
            );
            commandProcessor.registerArgAHandle(
                    DynamicArg.class,
                    (annotation, type, argumentManager, iterator, sender) -> {
                        if (type != Object.class)
                            throw new IllegalStateException("@DynamicArgs must be an Object class.");
                        return argumentManager.multiParse(annotation.types(), iterator, sender);
                    },
                    (annotation) -> new ELDCommandArgsHandler.CommonProperties(annotation.order(), annotation.optional(), annotation.labels())
            );
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error while enabling ELDependenci: ", e);
            getLogger().log(Level.SEVERE, "Disabling plugin...");
            this.disabled = true;
        }
        getServer().getPluginManager().registerEvents(this, this);
        for (JavaPlugin plugin : collectionMap.keySet()) {
            if (plugin.isEnabled()) { // 如果比ELD更早加載完成
                // 強行加載ELD啟用事件
                this.onPluginEnable(new PluginEnableEvent(plugin));
            }
        }
    }

    @EventHandler
    public void onPluginEnable(final PluginEnableEvent e) {
        if (!(e.getPlugin() instanceof ELDPaperPlugin plugin)) return;
        if (ELDServiceCollection.DISABLED.contains(plugin)) {
            plugin.getLogger().log(Level.SEVERE, "此插件由於註冊不完整，已被禁用。");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        var services = collectionMap.get(plugin);
        if (services == null) return; // not eld plugin

        if (disabled) {
            plugin.getLogger().log(Level.SEVERE, "由於 ELDependenci 無法啟動，此插件已被禁用。");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        var configManager = services.configManager;

        configManager.setInjector(injector);

        injector.injectMembers(services.lifeCycleHook);

        services.lifeCycleHook.onEnable(plugin);

        //register command
        if (!services.commands.isEmpty()) {
            plugin.getLogger().info("Registering all of the commands of plugin " + plugin.getName());
            ELDCommandHandler.registers(plugin, services.commands, commandProcessor);
        }

        //register listener
        if (!services.listeners.isEmpty()) {
            plugin.getLogger().info("Registering all of the listeners of plugin " + plugin.getName());
            services.listeners.forEach(listenerCls -> {
                var listener = injector.getInstance(listenerCls);
                plugin.getServer().getPluginManager().registerEvents(listener, plugin);
            });
        }
    }

    @EventHandler
    public void onPluginDisable(final PluginDisableEvent e) {
        if (!(e.getPlugin() instanceof ELDPaperPlugin plugin)) return;
        var services = collectionMap.get(plugin);
        if (services == null) return; // not eld plugin
        if (disabled || ELDServiceCollection.DISABLED.contains(plugin)) return;
        services.lifeCycleHook.onDisable(plugin);

    }

    @Override
    public MessageYaml loadYaml(File file) {
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        return new MessageYaml() {
            @Override
            public boolean contains(String path) {
                return configuration.contains(path);
            }

            @Override
            public String getString(String path) {
                return configuration.getString(path);
            }

            @Override
            public List<String> getStringList(String path) {
                return configuration.getStringList(path);
            }
        };
    }


    private class ELDManagerProvider implements PaperManagerProvider {

        private final ELDServiceCollection collection;

        private ELDManagerProvider(ELDServiceCollection collection) {
            this.collection = collection;
        }

        @Override
        public ConfigStorage getConfigStorage() {
            return collection.configManager;
        }

        @Override
        public ArgumentManager<CommandSender> getArgumentManager() {
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

    @Override
    public void installModule(Module module) {
        this.module.addModule(module);
    }

    @Override
    public <T> void customInstallation(Class<T> regCls, T ins) {
        this.customInstallation.put(regCls, ins);
    }
}
