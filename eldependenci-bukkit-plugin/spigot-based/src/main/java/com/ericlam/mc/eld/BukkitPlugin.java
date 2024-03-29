package com.ericlam.mc.eld;

import com.ericlam.mc.eld.bukkit.CommandNode;
import com.ericlam.mc.eld.commands.CommonCommandSender;
import com.ericlam.mc.eld.commands.ELDArgumentManager;
import com.ericlam.mc.eld.configurations.BukkitBeanModifier;
import com.ericlam.mc.eld.configurations.ELDConfigManager;
import com.ericlam.mc.eld.exceptions.ArgumentParseException;
import com.ericlam.mc.eld.implement.ELDMessageConfig;
import com.ericlam.mc.eld.listener.LifeCycleListener;
import com.ericlam.mc.eld.managers.ItemInteractManager;
import com.ericlam.mc.eld.guice.ELDPluginModule;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Set;

public abstract class BukkitPlugin extends JavaPlugin implements Registration<JavaPlugin, Listener, CommandSender, CommandNode>, MCPlugin {
    protected final BukkitModule bukkitModule = new BukkitModule();
    protected final BukkitConfigHandler configHandler = new BukkitConfigHandler();
    protected final ItemInteractListener itemInteractManager = new ItemInteractListener(this);
    protected final ELDependenciCore<JavaPlugin, CommandSender, Listener, CommandNode> elDependenciCore = new ELDependenciCore<>(this);

    static {
        ELDConfigManager.JSON_MAPPER
                .registerModule(new SimpleModule()
                        .setDeserializerModifier(new BukkitBeanModifier.Deserializer())
                        .setSerializerModifier(new BukkitBeanModifier.Serializer()));
        ELDConfigManager.YAML_MAPPER
                .registerModule(new SimpleModule()
                        .setDeserializerModifier(new BukkitBeanModifier.Deserializer())
                        .setSerializerModifier(new BukkitBeanModifier.Serializer()));
    }

    @Override
    public void onLoad() {
        this.elDependenciCore.onMainLoad();
        this.elDependenciCore.baseModule.bindInstance(ItemInteractManager.class, itemInteractManager);
    }

    @Override
    public void onEnable() {
        this.elDependenciCore.onMainEnable(this);
        var argumentManager = elDependenciCore.argumentManager;
        var eldMessageConfig = elDependenciCore.eldMessageConfig;
        this.registerParser(argumentManager, eldMessageConfig);
        getServer().getPluginManager().registerEvents(itemInteractManager, this);
    }

    @Override
    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    @Override
    public MCPlugin getPlugin() {
        return this;
    }

    @Override
    public ELDPluginModule<JavaPlugin> getPluginModule() {
        return bukkitModule;
    }

    @Override
    public CommonManagerProvider<CommandSender, CommandNode, Listener, JavaPlugin> toManagerProvider(ELDServiceCollection<CommandNode, Listener, JavaPlugin> collection,
                                                                                                     ELDArgumentManager<CommandSender> argumentManager) {
        return new ELDBukkitManagerProvider(collection, argumentManager, itemInteractManager);
    }

    @Override
    public ELDServiceCollection<CommandNode, Listener, JavaPlugin> toServiceCollection(ELDCommonModule module, MCPlugin plugin, Map<Class<?>, Object> customInstallation, ConfigHandler handler) {
        return new BukkitServiceCollection(module, plugin, customInstallation, handler);
    }

    @Override
    public JavaPlugin toRealPlugin(ELDPlugin plugin) {
        if (!(plugin instanceof ELDBukkitPlugin jplugin))
            throw new IllegalStateException("plugin is not ELDBukkitPlugin");
        return jplugin;
    }

    @Override
    public void registerLifeCycleListener(JavaPlugin javaPlugin, LifeCycleListener<JavaPlugin> listener, Set<JavaPlugin> keySet) {
        var pluginListener = new BukkitLifeCycleListener(listener);
        getServer().getPluginManager().registerEvents(pluginListener, this);
        for (JavaPlugin plugin : keySet) {
            if (plugin.isEnabled()) { // 如果比ELD更早加載完成
                // 強行加載ELD啟用事件
                listener.onPluginEnable(plugin);
            }
        }
    }

    @Override
    public void disablePlugin(JavaPlugin target) {
        target.getServer().getPluginManager().disablePlugin(target);
    }

    @Override
    public void registerEvents(JavaPlugin javaPlugin, Listener listener) {
        getServer().getPluginManager().registerEvents(listener, javaPlugin);
    }

    protected void registerParser(ELDArgumentManager<CommandSender> argumentManager, ELDMessageConfig eldMessageConfig) {
        argumentManager.registerParser(Player.class, (args, sender, parser) -> {
            var player = Bukkit.getPlayer(args.next());
            if (player == null) {
                throw new ArgumentParseException(eldMessageConfig.getConvertError("player-not-online"));
            }
            return player;
        });
        argumentManager.registerParser(OfflinePlayer.class, (args, sender, parser) -> Bukkit.getOfflinePlayer(args.next()));
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

    }

    @Override
    public CommonCommandSender toCommandSender(CommandSender commandSender) {
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
            public void sendMessage(BaseComponent[] component) {
                commandSender.spigot().sendMessage(component);
            }


            @Override
            public boolean isPlayer() {
                return commandSender instanceof Player;
            }
        };
    }

    @Override
    public void saveResource(String path) {
        saveResource(path, true);
    }
}
