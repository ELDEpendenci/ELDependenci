package com.ericlam.mc.test.eld;

import com.ericlam.mc.eld.registrations.ListenerRegistry;
import com.ericlam.mc.test.eld.eventlistener.EventListenerRegister;
import com.ericlam.mc.test.eld.eventlistener.PlayerJoinEventListener;
import com.ericlam.mc.test.eld.eventlistener.PlayerQuitEventListener;
import org.bukkit.Material;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class Main {

    public static void main(String[] args) {

    }


    public void registerEvent(EventListenerRegister reg) {
        reg.register(PlayerJoinEvent.class, PlayerJoinEventListener.class);
        reg.register(PlayerQuitEvent.class, PlayerQuitEventListener.class);
        JavaPlugin.getPlugin(JavaPlugin.class).getServer().getPluginManager().registerEvent(
                PlayerJoinEvent.class, new Listener() {
                }, EventPriority.HIGH, (listener, event) -> {

                }, JavaPlugin.getPlugin(JavaPlugin.class));

    }

    public void registerListener(ListenerRegistry reg) {
        reg.listeners(List.of(
                Listener.class,
                Listener.class,
                Listener.class
        ));
    }


}
