package com.ericlam.mc.test.eld.eventlistener;

import org.bukkit.event.Event;

import java.util.function.Consumer;

public interface EventListener {

    <E extends Event> void listen(Class<E> event, Consumer<EventSubscriber<E>> subscriber);
}
