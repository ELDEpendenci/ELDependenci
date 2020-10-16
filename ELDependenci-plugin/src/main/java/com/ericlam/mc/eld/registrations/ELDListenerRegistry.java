package com.ericlam.mc.eld.registrations;

import com.ericlam.mc.eld.components.ELDListener;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class ELDListenerRegistry implements ListenerRegistry{

    private final Set<Class<? extends Listener>> bukkitListenerClass = new HashSet<>();
    private final Set<Class<? extends ELDListener>> eldListenerClass = new HashSet<>();

    @Override
    public <T extends Listener> void listeners(Collection<Class<T>> listener) {
        bukkitListenerClass.addAll(listener);
    }

    @Override
    public <T extends ELDListener> void ELDListeners(Collection<Class<T>> listener) {
        eldListenerClass.addAll(listener);
    }

    public Set<Class<? extends ELDListener>> getEldListenerClass() {
        return eldListenerClass;
    }

    public Set<Class<? extends Listener>> getBukkitListenerClass() {
        return bukkitListenerClass;
    }
}
