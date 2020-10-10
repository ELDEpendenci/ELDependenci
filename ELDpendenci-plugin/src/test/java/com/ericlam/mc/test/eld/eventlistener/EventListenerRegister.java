package com.ericlam.mc.test.eld.eventlistener;

import org.bukkit.event.Event;

public interface EventListenerRegister {

    <E extends Event> void register(Class<E> eventCls, Class<? extends EventListener2<E>> listener2Class);

}
