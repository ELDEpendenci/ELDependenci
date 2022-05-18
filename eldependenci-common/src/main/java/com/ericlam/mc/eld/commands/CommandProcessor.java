package com.ericlam.mc.eld.commands;

import com.ericlam.mc.eld.HierarchyNode;
import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.components.CommonCommandNode;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public interface CommandProcessor<CommandSender, CommandNode extends CommonCommandNode<CommandSender>> {

    <A extends Annotation> void registerArgAHandle(Class<A> annotation, CommandArgHandler<A, CommandSender> handle, Function<A, ELDCommandArgsHandler.CommonProperties> propertyGetter);

    void invokeCommand(CommandSender sender, HierarchyNode<CommandNode> node, LinkedList<String> strings);

    List<String> invokeTabComplete(CommandSender sender, HierarchyNode<CommandNode> node, List<String> args);

    boolean labelMatch(Commander commander, String label);

}
