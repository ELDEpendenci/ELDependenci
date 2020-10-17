package com.ericlam.mc.eld.commands;

import com.ericlam.mc.eld.HierarchyNode;
import com.ericlam.mc.eld.annotations.CommandArg;
import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.annotations.DynamicArg;
import com.ericlam.mc.eld.annotations.RemainArgs;
import com.ericlam.mc.eld.bukkit.ELDMessageConfig;
import com.ericlam.mc.eld.exceptions.ArgumentParseException;
import com.google.inject.Injector;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ELDCommandHandler implements CommandExecutor, TabCompleter {

    private static final ELDCommandArgsHandler commandArgsHandler = new ELDCommandArgsHandler();
    private static ELDMessageConfig msg;

    public static void setMsg(ELDMessageConfig msg) {
        ELDCommandHandler.msg = msg;
    }

    public static <A extends Annotation> void registerArgAHandle(Class<A> annotation, CommandArgHandler<A> handle, Function<A, ELDCommandArgsHandler.CommonProperties> propertyGetter) {
        commandArgsHandler.registerArgAHandle(annotation, handle, propertyGetter);
    }

    static {
        registerArgAHandle(
                CommandArg.class,
                (annotation, type, argumentManager, iterator, sender) -> argumentManager.tryParse(type, annotation.identifier(), iterator, sender),
                (annotation) -> new ELDCommandArgsHandler.CommonProperties(annotation.order(), annotation.optional(), annotation.labels())
        );

        registerArgAHandle(
                DynamicArg.class,
                (annotation, type, argumentManager, iterator, sender) -> {
                    if (type != Object.class) throw new IllegalStateException("@DynamicArgs 標註的變數必須為 Object class.");
                    return argumentManager.multiParse(annotation.types(), iterator, sender);
                },
                (annotation) -> new ELDCommandArgsHandler.CommonProperties(annotation.order(), annotation.optional(), annotation.labels())
        );
    }

    private final Injector injector;
    private final Set<HierarchyNode> commandNodes;
    private final ELDArgumentManager parser;
    private final Map<Class<?>, Class<?>> primitiveClass = Map.of(
            int.class, Integer.class,
            double.class, Double.class,
            long.class, Long.class,
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            short.class, Short.class,
            float.class, Float.class,
            char.class, Character.class
    );

    private ELDCommandHandler(Set<HierarchyNode> commandNodes, Injector injector, ELDArgumentManager argumentManager) {
        if (msg == null) throw new IllegalStateException("msg config 尚未被初始化");
        this.commandNodes = commandNodes;
        this.injector = injector;
        this.parser = argumentManager;
    }


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] strings) {
        for (HierarchyNode node : commandNodes) {
            if (labelMatch(node.current.getAnnotation(Commander.class), label)) {
                invokeCommand(commandSender, node, new LinkedList<>(List.of(strings)));
            }
        }
        return true;
    }

    private void invokeCommand(CommandSender sender, HierarchyNode node, LinkedList<String> strings) {
        var commander = node.current.getAnnotation(Commander.class);

        if (!commander.permission().isEmpty() && !sender.hasPermission(commander.permission())) {
            sender.sendMessage(msg.getLang().get("no-permission"));
            return;
        }

        if (strings.size() > 0) {
            for (HierarchyNode n : node.nodes) {
                var cmder = n.current.getAnnotation(Commander.class);
                if (labelMatch(cmder, strings.get(0))) {
                    var passArg = new LinkedList<>(strings);
                    passArg.remove(0);
                    invokeCommand(sender, n, passArg);
                    return;
                }
            }
        } else if (!node.nodes.isEmpty()) {
            sender.sendMessage(generateHelpLines(node.nodes));
            return;
        }

        if (commander.playerOnly() && !(sender instanceof Player)) {
            sender.sendMessage(msg.getLang().get("not-player"));
            return;
        }

        var commandNode = injector.getInstance(node.current);
        var placeholders = getPlaceholders(node);
        var remainArgsOpt = Arrays.stream(node.current.getDeclaredFields()).filter(f -> f.isAnnotationPresent(RemainArgs.class)).findFirst();
        try {
            var iterator = new ArgIterator(strings);
            for (Field placeholder : placeholders) {
                var type = placeholder.getType();
                if (type.isPrimitive()) {
                    type = primitiveClass.get(type);
                }

                var entry = commandArgsHandler.getArgsEntry(placeholder);
                var properties = entry.getValue();
                var argHandler = entry.getKey();

                try {
                    var instance = argHandler.argHandle(type, parser, iterator, sender);
                    placeholder.setAccessible(true);
                    placeholder.set(commandNode, instance);
                } catch (NoSuchElementException e) {
                    if (!properties.optional) {
                        sender.sendMessage(msg.getLang().get("no-args").replace("<args>", toPlaceholderStrings(placeholders)));
                        return;
                    }
                }

            }
            if (remainArgsOpt.isPresent()) {
                var f = remainArgsOpt.get();
                boolean isStringList = false;
                if (f.getGenericType() instanceof ParameterizedType) {
                    var t = (ParameterizedType) f.getGenericType();
                    isStringList = t.getRawType() == List.class && t.getActualTypeArguments()[0] == String.class;
                }
                if (!isStringList) throw new IllegalStateException("@RemainArgs 標註的變數必須為 List<String>。");
                var list = new ArrayList<String>();
                iterator.forEachRemaining(list::add);
                f.setAccessible(true);
                f.set(commandNode, list);
            }
            commandNode.execute(sender);
        } catch (IllegalAccessException e) {
            msg.getLang().getList("error").stream().map(s -> s.replace("<message>", e.getMessage())).forEach(sender::sendMessage);
            e.printStackTrace();
        } catch (ArgumentParseException e) {
            sender.sendMessage(e.getMessage());
        }
    }

    private boolean labelMatch(Commander commander, String label) {
        var alias = new LinkedList<>(List.of(commander.alias()));
        alias.add(commander.name());
        return alias.stream().anyMatch(s -> s.equalsIgnoreCase(label));
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] strings) {
        for (HierarchyNode node : commandNodes) {
            if (labelMatch(node.current.getAnnotation(Commander.class), label)) {
                var result = invokeTabComplete(commandSender, node, new ArrayList<>(List.of(strings)));
                String lastAug = strings[strings.length - 1];
                if (result != null && !lastAug.equals("")) {
                    result.removeIf(tabItem -> !tabItem.startsWith(lastAug));
                }
                return result;

            }
        }
        return null;
    }

    private List<String> invokeTabComplete(CommandSender sender, HierarchyNode node, List<String> args) {
        var commander = node.current.getAnnotation(Commander.class);

        if (!commander.permission().isEmpty() && !sender.hasPermission(commander.permission())) {
            return null;
        }

        if (args.size() > 0) {
            for (HierarchyNode n : node.nodes) {
                var cmder = n.current.getAnnotation(Commander.class);
                if (labelMatch(cmder, args.get(0))) {
                    var passArg = new LinkedList<>(args);
                    passArg.remove(0);
                    return invokeTabComplete(sender, n, passArg);
                }
            }
        }

        if (!node.nodes.isEmpty()) {
            return node.nodes.stream().map(n -> n.current.getAnnotation(Commander.class).name()).collect(Collectors.toList());
        }

        var commandNode = injector.getInstance(node.current);
        return commandNode.tabComplete(sender, args);
    }

    private String[] generateHelpLines(Set<HierarchyNode> nodes) {
        return nodes.stream().map(this::getHelpLine).toArray(String[]::new);
    }

    private List<Field> getPlaceholders(HierarchyNode node) {
        return Arrays.stream(node.current.getDeclaredFields())
                .filter(commandArgsHandler::isCommandArg)
                .sorted(commandArgsHandler::sortArgsField)
                .collect(Collectors.toList());
    }

    private String toPlaceholderStrings(List<Field> placeholders) {
        if (placeholders.isEmpty()) return "";
        return " " + placeholders.stream().map(commandArgsHandler::getPlaceholderLabel).collect(Collectors.joining(" "));
    }

    private String getHelpLine(final HierarchyNode node) {
        final var cmd = node.current.getAnnotation(Commander.class);
        final var builder = new StringBuilder(cmd.name());
        var topCmd = cmd;
        var topNode = node;
        while (topNode.parent != null) {
            topNode = topNode.parent;
            topCmd = topNode.current.getAnnotation(Commander.class);
            builder.insert(0, topCmd.name() + " ");
        }
        return "/" + builder.toString() + toPlaceholderStrings(getPlaceholders(node)) + " - " + cmd.description();
    }

    public static void registers(JavaPlugin plugin, Set<HierarchyNode> commands, Injector injector, ELDArgumentManager manager) {
        var executor = new ELDCommandHandler(commands, injector, manager);
        commands.forEach(hir -> {
            var cmd = hir.current.getAnnotation(Commander.class);
            var pluginCommand = Optional.ofNullable(plugin.getCommand(cmd.name())).orElseGet(() -> {
                try {
                    var constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
                    constructor.setAccessible(true);
                    var pluginCmd = constructor.newInstance(cmd.name(), plugin);
                    plugin.getServer().getCommandMap().register(plugin.getDescription().getName(), pluginCmd);
                    return pluginCmd;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            });
            if (pluginCommand == null) {
                plugin.getLogger().warning("指令 " + cmd.name() + " 尚未於 plugin.yml 註冊。並強行註冊失敗。");
                return;
            }
            pluginCommand.setAliases(List.of(cmd.alias()));
            pluginCommand.setDescription(cmd.description());
            pluginCommand.setExecutor(executor);
            pluginCommand.setTabCompleter(executor);

        });
    }


}
