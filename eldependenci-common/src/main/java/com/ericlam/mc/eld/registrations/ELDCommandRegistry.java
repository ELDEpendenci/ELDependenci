package com.ericlam.mc.eld.registrations;

import com.ericlam.mc.eld.HierarchyNode;
import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.common.CommonCommandNode;
import com.ericlam.mc.eld.registration.CommandRegistry;
import com.google.common.collect.ImmutableSet;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

public final class ELDCommandRegistry<T extends CommonCommandNode<?>> implements CommandRegistry<T> {


    private final Set<HierarchyNode<T>> nodes;

    public ELDCommandRegistry() {
        this(new LinkedHashSet<>());
    }

    public ELDCommandRegistry(Set<HierarchyNode<T>> nodes) {
        this.nodes = nodes;
    }

    @Override
    public void command(Class<T> node, Consumer<CommandRegistry<T>> child) {
        if (!node.isAnnotationPresent(Commander.class))
            throw new IllegalStateException(node + " is lack of @Commander annotation");
        var n = new HierarchyNode<>(node);
        child.accept(new SubCommandRegistry<>(n));
        nodes.add(n);
    }

    @Override
    public void command(Class<T> node) {
        nodes.add(new HierarchyNode<>(node));
    }

    public ImmutableSet<HierarchyNode<T>> getNodes() {
        return ImmutableSet.copyOf(nodes);
    }


    private record SubCommandRegistry<T extends CommonCommandNode<?>>(
            HierarchyNode<T> node) implements CommandRegistry<T> {

        @Override
        public void command(Class<T> node, Consumer<CommandRegistry<T>> child) {
            var n = new HierarchyNode<>(this.node, node);
            child.accept(new SubCommandRegistry<>(n));
            this.node.addNode(n);
        }

        @Override
        public void command(Class<T> node) {
            this.node.addNode(new HierarchyNode<>(this.node, node));
        }
    }
}
