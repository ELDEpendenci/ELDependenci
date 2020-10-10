package com.ericlam.mc.eld.registrations;

import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ELDListenerRegistry implements ListenerRegistry{

    private final Set<Class<? extends Listener>> bukkitListenerClass = new HashSet<>();

    @Override
    public <T extends Listener> void listeners(Collection<Class<T>> listener) {
        bukkitListenerClass.addAll(listener);
    }


    public Set<Class<? extends Listener>> getBukkitListenerClass() {
        return bukkitListenerClass;
    }
}
