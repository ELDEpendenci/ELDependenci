package com.ericlam.mc.eld.registrations;

import com.ericlam.mc.eld.components.ELDListener;
import org.bukkit.event.Listener;

import java.util.Collection;

public interface ListenerRegistry {

    <T extends Listener> void listeners(Collection<Class<T>> listener);


    <T extends ELDListener> void ELDListeners(Collection<Class<T>> listener);

}
