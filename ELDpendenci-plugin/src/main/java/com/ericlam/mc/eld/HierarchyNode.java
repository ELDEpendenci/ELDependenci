package com.ericlam.mc.eld;

import com.ericlam.mc.eld.components.CommandNode;
import io.netty.util.internal.ConcurrentSet;

import java.util.Set;

public final class HierarchyNode {

    public HierarchyNode parent;
    public final Class<? extends CommandNode> current;
    public final Set<HierarchyNode> nodes = new ConcurrentSet<>();

    public HierarchyNode(HierarchyNode parent,
                         Class<? extends CommandNode> current) {
        this.parent = parent;
        this.current = current;
    }

    public HierarchyNode(Class<? extends CommandNode> current) {
        this(null, current);
    }

    public void addNode(HierarchyNode node) {
        this.nodes.add(node);
    }
}
