package com.ericlam.mc.test.eld;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

import java.util.function.Consumer;
import java.util.function.Function;

public interface EventSubscriber<E extends Event> {

    EventSubscriber<E> priority(EventPriority priority);

    EventSubscriber<E> ignoreCancel(boolean ignore);

    EventSubscriber<E> filter(Function<E, Boolean> filter);

    EventFiltered<E> fork();

    void handle(Consumer<E> handler);


    interface EventFiltered<E extends Event> {

        EventFiltered<E> ifTrue(Consumer<E> handle);

        void ifFalse(Consumer<E> handle);
    }
}
