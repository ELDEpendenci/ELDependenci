package com.ericlam.mc.eld.components;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

import java.util.function.Consumer;
import java.util.function.Function;

public interface EventListeners {

    <E extends Event> EventSubscriber<E> listen(Class<E> eventClass);

    interface EventSubscriber<E extends Event> {

        Function<Cancellable, Boolean> IGNORE_CANCELLED = Cancellable::isCancelled;

        EventSubscriber<E> expireAfter(int times);

        EventSubscriber<E> priority(EventPriority priority);

        EventSubscriber<E> filter(Function<E, Boolean> filter);

        EventFiltered<E> fork();

        void handle(Consumer<E> handler);

    }

    interface EventFiltered<E extends Event> {

        EventFiltered<E> ifTrue(Consumer<E> handler);

        void ifFalse(Consumer<E> handler);
    }

}


