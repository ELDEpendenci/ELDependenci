package com.ericlam.mc.eld.commands;

import net.kyori.adventure.text.TextComponent;

public interface CommonCommandSender {

    boolean hasPermission(String permission);

    void sendMessage(String message);

    void sendMessage(String[] messages);

    void sendMessage(TextComponent component);


    boolean isPlayer();


}
