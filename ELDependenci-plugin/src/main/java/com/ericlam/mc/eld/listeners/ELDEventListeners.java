package com.ericlam.mc.eld.listeners;

import com.ericlam.mc.eld.components.EventListeners;
import com.google.common.collect.ImmutableMap;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ELDEventListeners implements EventListeners {

    private final Map<Class<? extends Event>, List<ELDEventSubscriber<? extends Event>>> subscribersMap = new ConcurrentHashMap<>();

    public Map<Class<? extends Event>, List<ELDEventSubscriber<? extends Event>>> getSubscribersMap() {
        return ImmutableMap.copyOf(subscribersMap);
    }

    @Override
    public <E extends Event> EventSubscriber<E> listen(Class<E> eventClass) {
        var subscriber = new ELDEventSubscriber<E>();
        this.subscribersMap.putIfAbsent(eventClass, new ArrayList<>());
        this.subscribersMap.get(eventClass).add(subscriber);
        return subscriber;
    }

    public static class ELDEventSubscriber<E extends Event> implements EventSubscriber<E> {

        private int times = -1;
        private EventPriority priority = EventPriority.NORMAL;
        private final List<Function<E, Boolean>> filters = new ArrayList<>();
        private Consumer<E> handler = e -> {
        };
        private ELDEventFiltered<E> filtered = null;

        @Override
        public EventSubscriber<E> expireAfter(int times) {
            this.times = times;
            return this;
        }

        @Override
        public EventSubscriber<E> priority(EventPriority priority) {
            this.priority = priority;
            return this;
        }

        @Override
        public EventSubscriber<E> filter(Function<E, Boolean> filter) {
            this.filters.add(filter);
            return this;
        }

        @Override
        public EventFiltered<E> fork() {
            filtered = new ELDEventFiltered<>();
            return filtered;
        }

        public EventPriority getPriority() {
            return priority;
        }

        @Override
        public void handle(Consumer<E> handler) {
            this.handler = handler;
        }


        public void invoke(Event wideEvent) {
            E event;
            try{
                event = (E) wideEvent;
            }catch (ClassCastException ignored){
                return;
            }
            if (times == 0) return;
            boolean passFilter = true;
            for (Function<E, Boolean> filter : filters) {
                passFilter = filter.apply(event);
            }
            if (!passFilter) {
              if (filtered != null){
                  filtered.falseHandler.accept(event);
              }
              return;
            }

            if (filtered != null){
                filtered.trueHandler.accept(event);
            }else{
                handler.accept(event);
            }

            if (times > 0){
                times--;
            }
        }


    }

    private static class ELDEventFiltered<E extends Event> implements EventFiltered<E> {

        private Consumer<E> trueHandler = e -> {
        };
        private Consumer<E> falseHandler = e -> {
        };

        @Override
        public EventFiltered<E> ifTrue(Consumer<E> handler) {
            this.trueHandler = handler;
            return this;
        }

        @Override
        public void ifFalse(Consumer<E> handler) {
            this.falseHandler = handler;
        }
    }
}
