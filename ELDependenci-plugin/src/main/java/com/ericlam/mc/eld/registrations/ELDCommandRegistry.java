package com.ericlam.mc.eld.registrations;

import com.ericlam.mc.eld.HierarchyNode;
import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.components.CommandNode;
import com.google.common.collect.ImmutableSet;
import io.netty.util.internal.ConcurrentSet;

import java.util.Set;
import java.util.function.Consumer;

public final class ELDCommandRegistry implements CommandRegistry {

    private final Set<HierarchyNode> nodes;

    public ELDCommandRegistry() {
        this(new ConcurrentSet<>());
    }

    public ELDCommandRegistry(Set<HierarchyNode> nodes) {
        this.nodes = nodes;
    }

    @Override
    public void command(Class<? extends CommandNode> node, Consumer<CommandRegistry> child) {
        if (!node.isAnnotationPresent(Commander.class)) throw new IllegalStateException(node+" 缺少 @Commander 標註");
        var n = new HierarchyNode(node);
        child.accept(new SubCommandRegistry(n));
        nodes.add(n);
    }

    @Override
    public void command(Class<? extends CommandNode> node) {
        nodes.add(new HierarchyNode(node));
    }

    public ImmutableSet<HierarchyNode> getNodes() {
        return ImmutableSet.copyOf(nodes);
    }

    private static class SubCommandRegistry implements CommandRegistry{

        private final HierarchyNode node;

        private SubCommandRegistry(HierarchyNode node) {
            this.node = node;
        }

        @Override
        public void command(Class<? extends CommandNode> node, Consumer<CommandRegistry> child) {
            var n = new HierarchyNode(this.node, node);
            child.accept(new SubCommandRegistry(n));
            this.node.addNode(n);
        }

        @Override
        public void command(Class<? extends CommandNode> node) {
            this.node.addNode(new HierarchyNode(this.node, node));
        }
    }
}
