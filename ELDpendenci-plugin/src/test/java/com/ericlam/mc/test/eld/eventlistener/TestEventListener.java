package com.ericlam.mc.test.eld.eventlistener;

import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerKickEvent;

public class TestEventListener {


    public void defineNote(EventListener listener){

        listener.listen(PlayerKickEvent.class, sub -> {

            sub.priority(EventPriority.MONITOR);
            sub.ignoreCancel(true);

            sub.filter(e -> e.getReason().equals("fuck"))
                    .fork()
                    .ifFalse(this::onReasonIsFuck);

            sub.filter(e -> e.getPlayer().getName().equals("shit"))
                    .fork()
                    .ifTrue(this::onPlayerIsShit)
                    .ifFalse(this::onPlayerIsNotShit);

            sub.handle(this::onPlayerKickEvent);
        });

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
