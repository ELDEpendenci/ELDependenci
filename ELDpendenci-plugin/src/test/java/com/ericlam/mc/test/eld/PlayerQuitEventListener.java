package com.ericlam.mc.test.eld;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitEventListener implements EventListener2<PlayerQuitEvent> {

    @Override
    public void subscribe(EventSubscriber<PlayerQuitEvent> subscriber) {
        subscriber.filter(Event::isAsynchronous)
                .fork()
                .ifTrue(e -> e.setQuitMessage("async quit!"))
                .ifFalse(e -> e.setQuitMessage("sync quit!"));
    }

}
