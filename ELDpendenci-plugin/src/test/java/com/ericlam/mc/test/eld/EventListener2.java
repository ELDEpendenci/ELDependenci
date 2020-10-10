package com.ericlam.mc.test.eld;

import org.bukkit.event.Event;

public interface EventListener2<E extends Event> {

    void subscribe(EventSubscriber<E> subscriber);
}
