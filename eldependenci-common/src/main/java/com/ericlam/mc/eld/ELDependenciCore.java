package com.ericlam.mc.eld;

import com.ericlam.mc.eld.commands.CommandProcessor;
import com.ericlam.mc.eld.commands.CommonCommandSender;
import com.ericlam.mc.eld.commands.ELDArgumentManager;
import com.ericlam.mc.eld.commands.ELDCommandProcessor;
import com.ericlam.mc.eld.common.CommonCommandNode;
import com.ericlam.mc.eld.configurations.ELDConfigManager;
import com.ericlam.mc.eld.exceptions.ArgumentParseException;
import com.ericlam.mc.eld.implement.ELDConfig;
import com.ericlam.mc.eld.implement.ELDMessageConfig;
import com.ericlam.mc.eld.listener.LifeCycleListener;
import com.ericlam.mc.eld.managers.ArgumentManager;
import com.ericlam.mc.eld.managers.ConfigStorage;
import com.ericlam.mc.eld.module.ELDConfigModule;
import com.ericlam.mc.eld.module.ELDLoggingModule;
import com.ericlam.mc.eld.module.ELDPluginModule;
import com.ericlam.mc.eld.services.ArgParserService;
import com.ericlam.mc.eld.services.ELDConfigPoolService;
import com.ericlam.mc.eld.services.ELDReflectionService;
import com.ericlam.mc.eld.services.logging.ELDLoggingService;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;

public class ELDependenciCore<Plugin, CommandSender, Listener, CommandNode extends CommonCommandNode<CommandSender>> implements AddonInstallation, ELDependenciAPI, LifeCycleListener<Plugin> {

    protected final ELDArgumentManager<CommandSender> argumentManager = new ELDArgumentManager<>();

    protected final ConfigHandler configHandler;

    protected final Registration<Plugin, Listener, CommandSender, CommandNode> registration;

    protected final MCPlugin mcPlugin;

    protected final ELDPluginModule<Plugin> eldPluginModule;

    protected final ELDCommonModule module;

    protected final Map<Plugin, ? extends ELDServiceCollection<CommandNode, Listener, Plugin>> collectionMap = new ConcurrentHashMap<>();

    protected final Map<Class<?>, Object> customInstallation = new ConcurrentHashMap<>();

    protected final ELDConfigManager localConfigManager;

    protected CommandProcessor<CommandSender, CommandNode> commandProcessor;

    protected Injector injector;

    protected ELDConfigPoolService groupConfigService;

    protected boolean sharePluginInstance = false;

    protected boolean disabled = false;

    protected ELDMessageConfig eldMessageConfig;

    protected ELDConfig coreConfig;

    public ELDependenciCore(Registration<Plugin, Listener, CommandSender, CommandNode> registration) {
        this.registration = registration;
        this.configHandler = registration.getConfigHandler();
        this.mcPlugin = registration.getPlugin();
        this.eldPluginModule = registration.getPluginModule();
        this.module = new ELDCommonModule(this.mcPlugin);
        this.localConfigManager = new ELDConfigManager(null, mcPlugin, configHandler);
    }

    public void onMainLoad() {
        this.module.bindInstance(ArgParserService.class, argumentManager);
        localConfigManager.loadConfig(ELDConfig.class);
        localConfigManager.loadConfig(ELDMessageConfig.class);
        this.eldMessageConfig = localConfigManager.getConfigAs(ELDMessageConfig.class);
        this.coreConfig = localConfigManager.getConfigAs(ELDConfig.class);
        groupConfigService = new ELDConfigPoolService(coreConfig.fileWalker, configHandler);
        this.module.setDefaultSingleton(coreConfig.defaultSingleton);
        this.sharePluginInstance = coreConfig.sharePluginInstance;
        this.module.addModule(new ELDConfigModule(groupConfigService, new ELDReflectionService()));
        this.module.addModule(new ELDLoggingModule(new ELDLoggingService(coreConfig, this.mcPlugin.getLogger())));
        this.customInstallation(AddonInstallation.class, this);
        this.installModule(eldPluginModule);
    }

    public void onMainEnable(Plugin plugin) {
        try {
            this.registerParser(argumentManager, eldMessageConfig);
            this.injector = Guice.createInjector(module);
        } catch (Exception e) {
            mcPlugin.getLogger().log(Level.SEVERE, "Error while enabling ELDependenci: ", e);
            mcPlugin.getLogger().log(Level.SEVERE, "Disabling plugin...");
            this.disabled = true;
        }
        registration.registerLifeCycleListener(plugin, this, collectionMap.keySet());
        this.commandProcessor = new ELDCommandProcessor<>(injector, argumentManager, eldMessageConfig) {
            @Override
            public CommonCommandSender toSender(CommandSender commandSender) {
                return registration.toCommandSender(commandSender);
            }
        };
    }


    @Override
    public void onPluginEnable(Plugin realPlugin) {
        if (!(realPlugin instanceof ELDPlugin plugin)) return;
        if (ELDServiceCollection.DISABLED.contains(plugin)) {
            plugin.getLogger().log(Level.SEVERE, "此插件由於註冊不完整，已被禁用。");
            registration.disablePlugin(realPlugin);
            return;
        }

        var services = collectionMap.get(realPlugin);
        if (services == null) return; // not eld plugin

        if (disabled) {
            plugin.getLogger().log(Level.SEVERE, "由於 ELDependenci 無法啟動，此插件已被禁用。");
            registration.disablePlugin(realPlugin);
            return;
        }

        var configManager = services.configManager;
        configManager.setInjector(injector);

        injector.injectMembers(services.lifeCycleHook);

        services.lifeCycleHook.onEnable(realPlugin);

        //register command
        if (!services.commands.isEmpty()) {
            plugin.getLogger().info("Registering all of the commands of plugin " + plugin.getName());
            registration.registerCommand(realPlugin, services.commands, commandProcessor);
        }

        //register listener
        if (!services.listeners.isEmpty()) {
            plugin.getLogger().info("Registering all of the listeners of plugin " + plugin.getName());
            services.listeners.forEach(listenerCls -> {
                var listener = injector.getInstance(listenerCls);
                registration.registerEvents(realPlugin, listener);
            });
        }
    }

    // internal registration for basic types
    protected void registerParser(ELDArgumentManager<CommandSender> argumentManager, ELDMessageConfig eldMessageConfig) {
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
    public void onPluginDisable(Plugin realPlugin) {
        if (!(realPlugin instanceof MCPlugin plugin)) return;
        var services = collectionMap.get(realPlugin);
        if (services == null) return; // not eld plugin
        if (disabled || ELDServiceCollection.DISABLED.contains(plugin)) return;
        services.lifeCycleHook.onDisable(realPlugin);
    }

    @Override
    public void installModule(Module module) {
        this.module.addModule(module);
    }

    @Override
    public <T> void customInstallation(Class<T> regCls, T ins) {
        this.customInstallation.put(regCls, ins);
    }


    @Override
    public ManagerProvider<?> register(ELDPlugin eld, Consumer<ServiceCollection> injector) {
        var plugin = registration.toRealPlugin(eld);
        if (collectionMap.containsKey(plugin)) {
            throw new IllegalStateException("the plugin is registered and not allowed to be registered again.");
        }
        var collection = registration.toServiceCollection(module, eld, customInstallation, configHandler);
        injector.accept(collection);
        if (sharePluginInstance) eldPluginModule.mapPluginInstance(plugin);
        module.bindPluginInstance(eld.getClass(), eld);
        collection.configManager.getGroupConfigSet().forEach(gc -> groupConfigService.addTypeMapper(gc, eld));
        collection.configManager.dumpAll();
        return registration.toManagerProvider(collection, argumentManager);
    }

    @Override
    public <T> T exposeService(Class<T> serviceCls) {
        return injector.getInstance(serviceCls);
    }
}
