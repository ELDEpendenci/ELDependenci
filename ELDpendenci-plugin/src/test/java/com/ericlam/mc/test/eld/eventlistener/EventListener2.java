package com.ericlam.mc.test.eld.eventlistener;

import org.bukkit.event.Event;

public interface EventListener2<E extends Event> {

    void subscribe(EventSubscriber<E> subscriber);
}
