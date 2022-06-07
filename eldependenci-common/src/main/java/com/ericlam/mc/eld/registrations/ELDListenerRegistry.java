package com.ericlam.mc.eld.registrations;

import com.ericlam.mc.eld.registration.ListenerRegistry;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class ELDListenerRegistry<Listener> implements ListenerRegistry<Listener> {
    private final Set<Class<? extends Listener>> listenersCls = new HashSet<>();

    @Override
    public void listeners(Collection<Class<? extends Listener>> listener) {
        listenersCls.addAll(listener);
    }


    public Set<Class<? extends Listener>> getListenersCls() {
        return listenersCls;
    }
}
