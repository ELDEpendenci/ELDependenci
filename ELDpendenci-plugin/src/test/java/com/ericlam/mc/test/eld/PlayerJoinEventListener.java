package com.ericlam.mc.test.eld;

import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinEventListener implements EventListener2<PlayerJoinEvent> {

    @Override
    public void subscribe(EventSubscriber<PlayerJoinEvent> subscriber) {
        subscriber.ignoreCancel(false);
        subscriber.priority(EventPriority.MONITOR);
        subscriber.filter(e -> e.getPlayer().getName().equals("fuck"))
                .fork()
                .ifTrue(e -> e.getPlayer().sendMessage("fuckoff"))
                .ifFalse(e -> e.getPlayer().sendMessage("hey!"));
    }
}
