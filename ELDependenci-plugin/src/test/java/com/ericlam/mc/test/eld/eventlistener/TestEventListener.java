package com.ericlam.mc.test.eld.eventlistener;

import com.ericlam.mc.eld.components.EventListeners;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerKickEvent;

public class TestEventListener {


    public void defineNote(EventListeners listener){

        listener.listen(PlayerKickEvent.class)
                .filter(Cancellable::isCancelled)
                .filter(e -> e.getReason().equalsIgnoreCase("fuck"))
                .fork()
                .ifTrue(this::onReasonIsFuck)
                .ifFalse(this::onReasonIsNotFuck);

        listener.listen(PlayerKickEvent.class)
                .priority(EventPriority.MONITOR)
                .handle(this::onPlayerKickEvent);

    }

    private void onPlayerKickEvent(PlayerKickEvent e){
        e.getPlayer().sendMessage("hey");
    }


    private void onReasonIsFuck(PlayerKickEvent e){
        e.setReason("fuckoff");
    }

    private void onReasonIsNotFuck(PlayerKickEvent e){
        e.setReason("hello");
    }


    private void onPlayerIsShit(PlayerKickEvent e){
        e.setCancelled(false);
        e.setLeaveMessage("bye");
    }


    private void onPlayerIsNotShit(PlayerKickEvent e){
        e.setCancelled(true);
    }


}
