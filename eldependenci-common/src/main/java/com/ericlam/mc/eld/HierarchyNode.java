package com.ericlam.mc.eld;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class HierarchyNode<T> {

    public HierarchyNode<T> parent;
    public final Class<? extends T> current;
    public final Set<HierarchyNode<T>> nodes = ConcurrentHashMap.newKeySet();

    public HierarchyNode(HierarchyNode<T> parent,
                         Class<? extends T> current) {
        this.parent = parent;
        this.current = current;
    }

    public HierarchyNode(Class<? extends T> current) {
        this(null, current);
    }

    public void addNode(HierarchyNode<T> node) {
        this.nodes.add(node);
    }
}
