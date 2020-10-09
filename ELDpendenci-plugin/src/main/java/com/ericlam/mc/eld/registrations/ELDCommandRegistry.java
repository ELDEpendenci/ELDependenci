package com.ericlam.mc.eld.registrations;

import com.ericlam.mc.eld.ELDModule;
import com.ericlam.mc.eld.HierarchyNode;
import com.ericlam.mc.eld.components.CommandNode;
import com.google.common.collect.ImmutableSet;
import io.netty.util.internal.ConcurrentSet;

import java.util.Set;
import java.util.function.Consumer;

public class ELDCommandRegistry implements CommandRegistry {

    private final Set<HierarchyNode> nodes;
    private final ELDModule module;

    public ELDCommandRegistry(ELDModule module) {
        this(module, new ConcurrentSet<>());
    }

    public ELDCommandRegistry(ELDModule module, Set<HierarchyNode> nodes) {
        this.module = module;
        this.nodes = nodes;
    }

    @Override
    public void command(Class<? extends CommandNode> node, Consumer<CommandRegistry> child) {
        module.bindCommand(node);
        var n = new HierarchyNode(node);
        child.accept(new SubCommandRegistry(n));
        nodes.add(n);
    }

    @Override
    public void command(Class<? extends CommandNode> node) {
        module.bindCommand(node);
        nodes.add(new HierarchyNode(node));
    }

    public ImmutableSet<HierarchyNode> getNodes() {
        return ImmutableSet.copyOf(nodes);
    }

    private class SubCommandRegistry implements CommandRegistry{

        private final HierarchyNode node;

        private SubCommandRegistry(HierarchyNode node) {
            this.node = node;
        }

        @Override
        public void command(Class<? extends CommandNode> node, Consumer<CommandRegistry> child) {
            module.bindCommand(node);
            var n = new HierarchyNode(this.node, node);
            child.accept(new SubCommandRegistry(n));
            this.node.addNote(n);
        }

        @Override
        public void command(Class<? extends CommandNode> node) {
            module.bindCommand(node);
            this.node.addNote(new HierarchyNode(this.node, node));
        }
    }
}
