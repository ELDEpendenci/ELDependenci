package com.ericlam.mc.eld.hook;

import com.ericlam.mc.eld.*;
import com.ericlam.mc.eld.commands.CommandProcessor;
import com.ericlam.mc.eld.commands.ELDArgumentManager;
import com.ericlam.mc.eld.configurations.ELDConfigManager;
import com.ericlam.mc.eld.exceptions.ArgumentParseException;
import com.ericlam.mc.eld.implement.ELDConfig;
import com.ericlam.mc.eld.implement.ELDMessageConfig;
import com.ericlam.mc.eld.managers.ArgumentManager;
import com.ericlam.mc.eld.managers.ConfigStorage;
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
import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;

public abstract class LifeCycleDispatcher implements ELDependenciAPI, AddonInstallation, MCPlugin {


    /*
    protected final ELDCommonModule module = new ELDCommonModule(this);
    protected final ELDBukkitModule bukkitModule = new ELDBukkitModule();
    protected final BukkitConfigHandler configHandler = new BukkitConfigHandler();
    protected final Map<JavaPlugin, BukkitServiceCollection> collectionMap = new ConcurrentHashMap<>();
    protected final Map<Class<?>, Object> customInstallation = new ConcurrentHashMap<>();
    protected final ELDArgumentManager<CommandSender> argumentManager = new ELDArgumentManager<>();
    protected ELDConfigPoolService groupConfigService;
    protected ItemInteractListener itemInteractListener;
    protected static ELDependenciAPI api;
    protected Injector injector;
    // never use dump
    protected final ELDConfigManager eldConfigManager = new ELDConfigManager(null, this, configHandler);
    protected boolean sharePluginInstance = false;
    protected ELDMessageConfig eldMessageConfig;


    static {
        ELDConfigManager.YAML_MAPPER.registerModule(new SimpleModule()
                .setDeserializerModifier(new BukkitBeanModifier.Deserializer())
                .setSerializerModifier(new BukkitBeanModifier.Serializer()));
        ELDConfigManager.JSON_MAPPER.registerModule(new SimpleModule()
                .setDeserializerModifier(new BukkitBeanModifier.Deserializer())
                .setSerializerModifier(new BukkitBeanModifier.Serializer()));
    }

    public abstract CommandProcessor<CommandSender, CommandNode> getCommandProcessor();

    public abstract CommandRegister getCommandRegister();

    @Override
    public void onLoad() {
        api = this;
        this.itemInteractListener = new ItemInteractListener(this);
        this.module.bindInstance(ArgParserService.class, argumentManager);
        eldConfigManager.loadConfig(ELDConfig.class);
        eldConfigManager.loadConfig(ELDMessageConfig.class);
        this.eldMessageConfig = eldConfigManager.getConfigAs(ELDMessageConfig.class);
        var eldConfig = eldConfigManager.getConfigAs(ELDConfig.class);
        groupConfigService = new ELDConfigPoolService(eldConfig.fileWalker, configHandler);
        this.module.setDefaultSingleton(eldConfig.defaultSingleton);
        this.sharePluginInstance = eldConfig.sharePluginInstance;
        this.module.addModule(new ELDConfigModule(groupConfigService, new ELDReflectionService()));
        this.module.addModule(new ELDLoggingModule(new ELDLoggingService(eldConfig, Bukkit.getLogger())));
        this.customInstallation(AddonInstallation.class, this);
        this.installModule(bukkitModule);
    }

    public static ELDependenciAPI getApi() {
        return Optional.ofNullable(api).orElseThrow(() -> new IllegalStateException("ELDependencies has not yet loaded，make sure your plugin.yml has added eld-plugin as depend"));
    }

    public ManagerProvider<?> register(ELDPlugin eld, Consumer<ServiceCollection> injector) {
        if (!(eld instanceof ELDBukkitPlugin plugin))
            throw new IllegalStateException("ELDependencies only support ELDPaperPlugin");
        if (collectionMap.containsKey(plugin)) {
            throw new IllegalStateException("the plugin is registered and not allowed to be registered again.");
        }
        var collection = new BukkitServiceCollection(module, plugin, customInstallation, configHandler);
        injector.accept(collection);
        if (sharePluginInstance) bukkitModule.mapPluginInstance(plugin);
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

        var commandProcessor = getCommandProcessor();
        var commandRegister = getCommandRegister();

        var lifeCycleListener = new PluginLifeCycleListener(collectionMap, injector, commandProcessor, commandRegister);

        try {
            getServer().getPluginManager().registerEvents(lifeCycleListener, this);
            registerParser();
            getServer().getPluginManager().registerEvents(itemInteractListener, this);
            this.injector = Guice.createInjector(module);

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error while enabling ELDependenci: ", e);
            getLogger().log(Level.SEVERE, "Disabling plugin...");
            lifeCycleListener.setDisabled(true);
        }
        getServer().getPluginManager().registerEvents(this, this);
        for (JavaPlugin plugin : collectionMap.keySet()) {
            if (plugin.isEnabled()) { // 如果比ELD更早加載完成
                // 強行加載ELD啟用事件
                lifeCycleListener.onPluginEnable(new PluginEnableEvent(plugin));
            }
        }
    }



    @Override
    public void installModule(Module module) {
        this.module.addModule(module);
    }

    @Override
    public <T> void customInstallation(Class<T> regCls, T ins) {
        this.customInstallation.put(regCls, ins);
    }

     */
}
