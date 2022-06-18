package com.ericlam.mc.eld.commands;

import com.ericlam.mc.eld.HierarchyNode;
import com.ericlam.mc.eld.annotations.CommandArg;
import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.annotations.DynamicArg;
import com.ericlam.mc.eld.annotations.RemainArgs;
import com.ericlam.mc.eld.common.CommonCommandNode;
import com.ericlam.mc.eld.exceptions.ArgumentParseException;
import com.ericlam.mc.eld.implement.ELDMessageConfig;
import com.ericlam.mc.eld.services.ELDReflectionService;
import com.google.inject.Injector;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ELDCommandProcessor<Sender, Command extends CommonCommandNode<Sender>> implements CommandProcessor<Sender, Command> {

    private final Map<Class<? extends Command>, Field[]> nodePlaceholders = new ConcurrentHashMap<>();
    private final ELDCommandArgsHandler<Sender> commandArgsHandler = new ELDCommandArgsHandler<>();

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

    private final Injector injector;
    private final ELDArgumentManager<Sender> parser;
    private final ELDMessageConfig msg;

    public ELDCommandProcessor(Injector injector, ELDArgumentManager<Sender> parser, ELDMessageConfig msg) {
        this.injector = injector;
        this.parser = parser;
        this.msg = msg;


        this.registerArgAHandle(
                CommandArg.class,
                (annotation, type, argumentManager, iterator, sender) -> argumentManager.tryParse(type, annotation.identifier(), iterator, sender),
                (annotation) -> new ELDCommandArgsHandler.CommonProperties(annotation.order(), annotation.optional(), annotation.labels())
        );
        this.registerArgAHandle(
                DynamicArg.class,
                (annotation, type, argumentManager, iterator, sender) -> {
                    if (type != Object.class)
                        throw new IllegalStateException("@DynamicArgs must be an Object class.");
                    return argumentManager.multiParse(annotation.types(), iterator, sender);
                },
                (annotation) -> new ELDCommandArgsHandler.CommonProperties(annotation.order(), annotation.optional(), annotation.labels())
        );

    }


    private Field[] getDeclaredFieldsForNodes(Class<? extends Command> node) {
        if (nodePlaceholders.containsKey(node)) return nodePlaceholders.get(node);
        var fields = ELDReflectionService.getDeclaredFieldsUpToStatic(node, null).toArray(Field[]::new);
        nodePlaceholders.put(node, fields);
        return fields;
    }

    public <A extends Annotation> void registerArgAHandle(Class<A> annotation, CommandArgHandler<A, Sender> handle, Function<A, ELDCommandArgsHandler.CommonProperties> propertyGetter) {
        commandArgsHandler.registerArgAHandle(annotation, handle, propertyGetter);
    }

    @Override
    public void invokeCommand(Sender commandSender, HierarchyNode<? extends Command> node, LinkedList<String> strings) {
        var commander = node.current.getAnnotation(Commander.class);
        var sender = toSender(commandSender);
        if (!commander.permission().isEmpty() && !sender.hasPermission(commander.permission())) {
            sender.sendMessage(msg.getLang().get("no-permission"));
            return;
        }

        if (strings.size() > 0) {
            for (HierarchyNode<? extends Command> n : node.nodes) {
                var cmder = n.current.getAnnotation(Commander.class);
                if (labelMatch(cmder, strings.get(0))) {
                    var passArg = new LinkedList<>(strings);
                    passArg.remove(0);
                    invokeCommand(commandSender, n, passArg);
                    return;
                }
            }
        }

        if (!node.nodes.isEmpty()) {
            ComponentBuilder md5Builder = new ComponentBuilder();
            md5Builder.append(msg.getLang().getPure("command-header", getHeader(node))).append("\n");
            for (BaseComponent[] components : generateHelpLines(node.nodes)) {
                md5Builder.append(components).append("\n");
            }
            sender.sendMessage(md5Builder.create());
            return;
        }

        if (commander.playerOnly() && !sender.isPlayer()) {
            sender.sendMessage(msg.getLang().get("not-player"));
            return;
        }

        var commandNode = injector.getInstance(node.current);
        var placeholders = getPlaceholders(node);
        var remainArgsOpt = Arrays.stream(getDeclaredFieldsForNodes(node.current)).filter(f -> f.isAnnotationPresent(RemainArgs.class)).findFirst();
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
                    var instance = argHandler.argHandle(type, parser, iterator, commandSender);
                    placeholder.setAccessible(true);
                    placeholder.set(commandNode, instance);
                } catch (NoSuchElementException e) {
                    if (!properties.optional()) {
                        sender.sendMessage(msg.getLang().get("no-args").replace("<args>", toPlaceholderStrings(placeholders)));
                        return;
                    }
                }

            }
            if (remainArgsOpt.isPresent()) {
                var f = remainArgsOpt.get();
                boolean isStringList = false;
                if (f.getGenericType() instanceof ParameterizedType t) {
                    isStringList = t.getRawType() == List.class && t.getActualTypeArguments()[0] == String.class;
                }
                if (!isStringList) throw new IllegalStateException("@RemainArgs must be List<String>。");
                var list = new ArrayList<String>();
                iterator.forEachRemaining(list::add);
                f.setAccessible(true);
                f.set(commandNode, list);
            }
            commandNode.execute(commandSender);
        } catch (Exception e) {
            if (e instanceof ArgumentParseException) {
                sender.sendMessage(msg.getLang().getPrefix() + e.getMessage());
                return;
            }
            Exception real = e;
            while (real.getCause() != null && real.getCause() instanceof Exception ex) {
                real = ex;
            }
            var message = real.getMessage() != null ? real.getMessage() : real.getClass().getSimpleName();
            msg.getLang().getList("error").stream().map(s -> s.replace("<message>", message)).forEach(sender::sendMessage);
            e.printStackTrace();
        }
    }

    @Override
    public List<String> invokeTabComplete(Sender sender, HierarchyNode<? extends Command> node, List<String> args) {
        var commander = node.current.getAnnotation(Commander.class);
        var commandSender = toSender(sender);
        if (!commander.permission().isEmpty() && !commandSender.hasPermission(commander.permission())) {
            return null;
        }

        if (args.size() > 0) {
            for (HierarchyNode<? extends Command> n : node.nodes) {
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

    public abstract CommonCommandSender toSender(Sender sender);


    public boolean labelMatch(Commander commander, String label) {
        var alias = new LinkedList<>(List.of(commander.alias()));
        alias.add(commander.name());
        return alias.stream().anyMatch(s -> s.equalsIgnoreCase(label));
    }

    private String getHeader(HierarchyNode<? extends Command> node){
        final var cmd = node.current.getAnnotation(Commander.class);
        final var builder = new StringBuilder(cmd.name());
        var topNoe = node;
        while (topNoe.parent != null){
            topNoe = topNoe.parent;
            var topCmd = topNoe.current.getAnnotation(Commander.class);
            builder.insert(0, topCmd.name() + " ");
        }
        return builder.toString();
    }

    private <T extends Command> BaseComponent[][] generateHelpLines(Set<HierarchyNode<T>> nodes) {
        return nodes.stream().map(this::getHelpComponent).toArray(BaseComponent[][]::new);
    }

    private List<Field> getPlaceholders(HierarchyNode<? extends Command> node) {
        return Arrays.stream(getDeclaredFieldsForNodes(node.current))
                .filter(commandArgsHandler::isCommandArg)
                .sorted(commandArgsHandler::sortArgsField)
                .collect(Collectors.toList());
    }

    private String toPlaceholderStrings(List<Field> placeholders) {
        if (placeholders.isEmpty()) return "";
        return " " + placeholders.stream().map(commandArgsHandler::getPlaceholderLabel).collect(Collectors.joining(" "));
    }

    private BaseComponent[] getHelpComponent(final HierarchyNode<? extends Command> node) {
        final var cmd = node.current.getAnnotation(Commander.class);
        final var builder = new StringBuilder(cmd.name());
        var topCmd = cmd;
        var topNode = node;
        while (topNode.parent != null) {
            topNode = topNode.parent;
            topCmd = topNode.current.getAnnotation(Commander.class);
            builder.insert(0, topCmd.name() + " ");
        }
        var placeholders = getPlaceholders(node);
        var placeholderStrings = toPlaceholderStrings(placeholders);
        var execute = String.format("/%s %s", builder, placeholderStrings);
        var line = msg.getLang().getPure("command-help-line", builder.toString(), placeholderStrings, cmd.description());
        ComponentBuilder md5Builder = new ComponentBuilder();

        return md5Builder
                .append(line)
                .event(
                        new ClickEvent(
                                placeholders.isEmpty() ? ClickEvent.Action.RUN_COMMAND : ClickEvent.Action.SUGGEST_COMMAND,
                                execute
                        )
                ).event(
                        new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new Text(msg.getLang().getPure("command-help-hover", cmd.description()))
                        )
                ).create();
    }
}
