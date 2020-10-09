package com.ericlam.mc.eld.commands;

import com.ericlam.mc.eld.HierarchyNode;
import com.ericlam.mc.eld.annotations.CommandArg;
import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.annotations.RemainArgs;
import com.ericlam.mc.eld.exceptions.ArgumentParseException;
import com.google.inject.Injector;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

public class ELDCommandHandler implements CommandExecutor, TabCompleter{

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
        this.commandNodes = commandNodes;
        this.injector = injector;
        this.parser = argumentManager;
    }


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] strings) {
        for (HierarchyNode node : commandNodes) {
            if (labelMatch(node.current.getAnnotation(Commander.class), label)){
                invokeCommand(commandSender, node, new ArrayList<>(List.of(strings)));
            }
        }
        return true;
    }

    private void invokeCommand(CommandSender sender, HierarchyNode node, List<String> strings){
        var commander = node.current.getAnnotation(Commander.class);

        if (!commander.permission().isEmpty() && !sender.hasPermission(commander.permission())){
            sender.sendMessage("no permission");
            return;
        }

        if (strings.size() > 0){
            for (HierarchyNode n : node.nodes) {
                var cmder = n.current.getAnnotation(Commander.class);
                if (labelMatch(cmder, strings.get(0))){
                    var passArg = new LinkedList<>(strings);
                    passArg.remove(0);
                    invokeCommand(sender, n, passArg);
                    return;
                }
            }
        }else if (!node.nodes.isEmpty()){
            sender.sendMessage(generateHelpLines(node.nodes));
            return;
        }

        if (commander.playerOnly() && !(sender instanceof Player)){
            sender.sendMessage("you are not player");
            return;
        }

        var commandNode = injector.getInstance(node.current);
        var placeholders = getPlaceholders(node);
        var remainArgsOpt = Arrays.stream(node.current.getDeclaredFields()).filter(f -> f.isAnnotationPresent(RemainArgs.class)).findFirst();
        try{
            var iterator = strings.iterator();
            for (Field placeholder : placeholders) {
                var type = placeholder.getType();
                if (type.isPrimitive()){
                    type = primitiveClass.get(type);
                }
                var arg = placeholder.getAnnotation(CommandArg.class);
                var identifier = arg.identifier();
                try{
                    var instance = parser.tryParse(type, identifier, iterator, sender);
                    placeholder.setAccessible(true);
                    placeholder.set(commandNode, instance);
                }catch (NoSuchElementException e){
                    if (!arg.optional()) {
                        sender.sendMessage("參數不足:"+toPlaceholderStrings(placeholders));
                        return;
                    }
                }
            }
            if (remainArgsOpt.isPresent()){
                var f = remainArgsOpt.get();
                boolean isStringList = false;
                if (f.getGenericType() instanceof ParameterizedType){
                    var t = (ParameterizedType)f.getGenericType();
                    isStringList = t.getRawType() == List.class && t.getActualTypeArguments()[0] == String.class;
                }
                if (!isStringList) throw new IllegalStateException("@RemainArgs 標註的變數必須為 List<String>。");
                var list = new ArrayList<String>();
                iterator.forEachRemaining(list::add);
                f.setAccessible(true);
                f.set(commandNode, list);
            }
            commandNode.execute(sender);
        }catch (IllegalAccessException e) {
            sender.sendMessage("ERROR: "+e.getMessage());
            sender.sendMessage("請通知本服插件師");
            e.printStackTrace();
        }catch (ArgumentParseException e) {
            sender.sendMessage(e.getMessage());
        }
    }

    private boolean labelMatch(Commander commander, String label){
        var alias = new LinkedList<>(List.of(commander.alias()));
        alias.add(commander.name());
        return alias.stream().anyMatch(s -> s.equalsIgnoreCase(label));
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] strings) {
        for (HierarchyNode node : commandNodes) {
            if (labelMatch(node.current.getAnnotation(Commander.class), label)){
                return invokeTabComplete(commandSender, node, new ArrayList<>(List.of(strings)));
            }
        }
        return null;
    }

    private List<String> invokeTabComplete(CommandSender sender, HierarchyNode node, List<String> args){
        var commander = node.current.getAnnotation(Commander.class);

        if (!commander.permission().isEmpty() && !sender.hasPermission(commander.permission())){
            return null;
        }

        if (args.size() > 0){
            for (HierarchyNode n : node.nodes) {
                var cmder = n.current.getAnnotation(Commander.class);
                if (labelMatch(cmder, args.get(0))){
                    var passArg = new LinkedList<>(args);
                    passArg.remove(0);
                    return invokeTabComplete(sender, n, passArg);
                }
            }
        }else if (!node.nodes.isEmpty()){
            return node.nodes.stream().map(n -> n.current.getAnnotation(Commander.class).name()).collect(Collectors.toList());
        }

        var commandNode = injector.getInstance(node.current);
        return commandNode.tabComplete(sender, args);
    }

    private String[] generateHelpLines(Set<HierarchyNode> nodes){
        return nodes.stream().map(this::getHelpLine).toArray(String[]::new);
    }

    private List<Field> getPlaceholders(HierarchyNode node){
        return Arrays.stream(node.current.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(CommandArg.class))
                .sorted((f1, f2) -> {
                    var anno1 = f1.getAnnotation(CommandArg.class);
                    var anno2 = f2.getAnnotation(CommandArg.class);
                    return Integer.compare(anno1.order(), anno2.order()) * Boolean.compare(anno1.optional(), anno2.optional());
                })
                .collect(Collectors.toList());
    }

    private String toPlaceholderStrings(List<Field> placeholders){
        if (placeholders.isEmpty()) return "";
        return " " + placeholders.stream()
                .map(f -> {
                    var arg = f.getAnnotation(CommandArg.class);
                    String label;
                    if (arg.labels().length > 0){
                        label = String.join(", ");
                    }else{
                        label = f.getName();
                    }
                    return arg.optional() ? "["+label+"]" : "<"+label+">";
                }).collect(Collectors.joining(" "));
    }

    private String getHelpLine(final HierarchyNode node){
        final var cmd = node.current.getAnnotation(Commander.class);
        final var builder = new StringBuilder(cmd.name());
        var topCmd = cmd;
        var topNode = node;
        while (topNode.parent != null){
            topNode = topNode.parent;
            topCmd = topNode.current.getAnnotation(Commander.class);
            builder.insert(0, topCmd.name() + " ");
        }
        return "/" + builder.toString() + toPlaceholderStrings(getPlaceholders(node)) + " - " + cmd.description();
    }

    public static void registers(JavaPlugin plugin, Set<HierarchyNode> commands, Injector injector, ELDArgumentManager manager){
        var executor = new ELDCommandHandler(commands, injector, manager);
        plugin.getLogger().info("正在註冊插件 "+plugin.getName()+" 的所有指令...");
        commands.forEach(hir -> {
            var cmd = hir.current.getAnnotation(Commander.class);
            var pluginCommand = plugin.getCommand(cmd.name());
            if (pluginCommand == null) {
                plugin.getLogger().warning("指令 " + cmd.name() + " 尚未於 plugin.yml 註冊。");
                return;
            }
            pluginCommand.setAliases(List.of(cmd.alias()));
            pluginCommand.setDescription(cmd.description());
            pluginCommand.setExecutor(executor);
            pluginCommand.setTabCompleter(executor);
        });
    }


}
