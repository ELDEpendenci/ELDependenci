package com.ericlam.mc.eld;

import com.ericlam.mc.eld.commands.CommandProcessor;
import com.ericlam.mc.eld.commands.CommonCommandSender;
import com.ericlam.mc.eld.commands.ELDArgumentManager;
import com.ericlam.mc.eld.common.CommonCommandNode;
import com.ericlam.mc.eld.implement.ELDMessageConfig;
import com.ericlam.mc.eld.listener.LifeCycleListener;
import com.ericlam.mc.eld.module.ELDPluginModule;
import com.google.inject.Injector;

import java.util.Map;
import java.util.Set;

public interface Registration<Plugin, Listener, CommandSender, CommandNode extends CommonCommandNode<CommandSender>> {

    CommonCommandSender toCommandSender(CommandSender commandSender);

    void registerCommand(Plugin plugin, Set<HierarchyNode<CommandNode>> commands, CommandProcessor<CommandSender, CommandNode> processor);

    ConfigHandler getConfigHandler();

    MCPlugin getPlugin();

    ELDPluginModule<Plugin> getPluginModule();

    CommonManagerProvider<CommandSender, CommandNode, Listener, Plugin> toManagerProvider(ELDServiceCollection<CommandNode, Listener, Plugin> collection, ELDArgumentManager<CommandSender> argumentManager);

    ELDServiceCollection<CommandNode, Listener, Plugin> toServiceCollection(ELDCommonModule module, MCPlugin plugin, Map<Class<?>, Object> customInstallation, ConfigHandler handler);

    Plugin toRealPlugin(ELDPlugin plugin);

    void registerLifeCycleListener(Plugin plugin, LifeCycleListener<Plugin> listener, Set<Plugin> keySet);

    void disablePlugin(Plugin target);

    void registerEvents(Plugin plugin, Listener listener);


}
