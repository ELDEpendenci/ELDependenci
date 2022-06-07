package com.ericlam.mc.eld;

import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.commands.*;
import com.ericlam.mc.eld.bungee.CommandNode;
import com.ericlam.mc.eld.exceptions.ArgumentParseException;
import com.ericlam.mc.eld.implement.ELDMessageConfig;
import com.ericlam.mc.eld.listener.LifeCycleListener;
import com.ericlam.mc.eld.module.ELDPluginModule;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ELDependenci extends Plugin implements Registration<Plugin, Listener, CommandSender, CommandNode>, MCPlugin {

    private static ELDependenciAPI api;

    public static ELDependenciAPI getApi() {
        return Optional.ofNullable(api).orElseThrow(() -> new IllegalStateException("ELDependencies has not yet loaded，make sure your plugin.yml has added eld-plugin as depend"));
    }

    private final BungeeConfigHandler bungeeConfigHandler = new BungeeConfigHandler();
    private final BungeePluginModule pluginModule = new BungeePluginModule();

    private final ELDependenciCore<Plugin, CommandSender, Listener, CommandNode> elDependenciCore = new ELDependenciCore<>(this);


    @Override
    public void onLoad() {
        elDependenciCore.onMainLoad();
        api = elDependenciCore;
    }

    @Override
    public void onEnable() {
        elDependenciCore.onMainEnable(this);
        var argumentManager = elDependenciCore.argumentManager;
        var messageConfig = elDependenciCore.eldMessageConfig;
        this.registerParser(argumentManager, messageConfig);
    }

    public void registerParser(ELDArgumentManager<CommandSender> argumentManager, ELDMessageConfig messageConfig){
        // get ServerInfo
        argumentManager.registerParser(ServerInfo.class, (arg, sender, parser) -> {
            var name = arg.next();
            var server = getProxy().getServerInfo(name);
            if (server == null) {
                throw new ArgumentParseException(messageConfig.getLang().getPure("server-not-exist", name));
            }
            return server;
        });
        // get ProxiedPlayer
        argumentManager.registerParser(ProxiedPlayer.class, (arg, sender, parser) -> {
            var name = arg.next();
            var player = getProxy().getPlayer(name);
            if (player == null) {
                throw new ArgumentParseException(messageConfig.getLang().getPure("player-not-online", name));
            }
            return player;
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
                commandSender.sendMessage(TextComponent.fromLegacyText(message));
            }

            @Override
            public void sendMessage(String[] messages) {
                ComponentBuilder builder = new ComponentBuilder();
                for (String message : messages) {
                    builder.append(message);
                }
                commandSender.sendMessage(builder.create());
            }

            @Override
            public void sendMessage(BaseComponent[] component) {
                commandSender.sendMessage(component);
            }


            @Override
            public boolean isPlayer() {
                return commandSender instanceof ProxiedPlayer;
            }
        };
    }

    @Override
    public void registerCommand(Plugin plugin, Set<HierarchyNode<? extends CommandNode>> commands, CommandProcessor<CommandSender, CommandNode> processor) {
        var executor = new BungeeCommandHandler(commands, processor);
        for (HierarchyNode<? extends CommandNode> node : commands) {
            var commander = node.current.getAnnotation(Commander.class);
            var cmd = new BungeecordCommand(commander, executor);
            getProxy().getPluginManager().registerCommand(plugin, cmd);
        }
    }

    @Override
    public ConfigHandler getConfigHandler() {
        return bungeeConfigHandler;
    }

    @Override
    public MCPlugin getPlugin() {
        return this;
    }

    @Override
    public ELDPluginModule<Plugin> getPluginModule() {
        return pluginModule;
    }

    @Override
    public CommonManagerProvider<CommandSender, CommandNode, Listener, Plugin> toManagerProvider(ELDServiceCollection<CommandNode, Listener, Plugin> collection, ELDArgumentManager<CommandSender> argumentManager) {
        return new ELDBungeeManagerProvider(collection, argumentManager);
    }

    @Override
    public ELDServiceCollection<CommandNode, Listener, Plugin> toServiceCollection(ELDCommonModule module, MCPlugin plugin, Map<Class<?>, Object> customInstallation, ConfigHandler handler) {
        return new BungeeServiceCollection(module, plugin, customInstallation, handler);
    }

    @Override
    public Plugin toRealPlugin(ELDPlugin plugin) {
        if (!(plugin instanceof ELDBungeePlugin jplugin))
            throw new IllegalStateException("plugin is not ELDBungeePlugin");
        return jplugin;
    }

    @Override
    public void registerLifeCycleListener(Plugin plugin, LifeCycleListener<Plugin> listener, Set<Plugin> keySet) {
        var pluginListener = new BungeeLifeCycleListener(listener);
        plugin.getProxy().getPluginManager().registerListener(this, pluginListener);
    }

    @Override
    public void disablePlugin(Plugin target) {
        // bungeecord has no option to force disable plugin
    }

    @Override
    public void registerEvents(Plugin plugin, Listener listener) {
        plugin.getProxy().getPluginManager().registerListener(plugin, listener);
    }

    @Override
    public void saveResource(String path) {
        if (!getDataFolder().exists() && getDataFolder().mkdirs()) {
            getLogger().info("Created Plugin Folder for " + getName());
        }
        var target = new File(getDataFolder(), path);
        var ins = this.getResourceAsStream(path);
        try {
            Files.copy(ins, target.toPath());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot copy file: " + path, e);
        }
    }

    @Override
    public String getName() {
        return this.getDescription().getName();
    }
}
