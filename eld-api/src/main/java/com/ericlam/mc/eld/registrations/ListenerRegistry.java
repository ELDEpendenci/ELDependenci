package com.ericlam.mc.eld.registrations;

import org.bukkit.event.Listener;

import java.util.Collection;

public interface ListenerRegistry {

    <T extends Listener> void listeners(Collection<Class<T>> listener);

}
