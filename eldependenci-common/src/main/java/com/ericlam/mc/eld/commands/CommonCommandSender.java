package com.ericlam.mc.eld.commands;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public interface CommonCommandSender {

    boolean hasPermission(String permission);

    void sendMessage(String message);

    void sendMessage(String[] messages);

    void sendMessage(BaseComponent[] component);


    boolean isPlayer();


}
