package com.ericlam.mc.eld.exceptions;

import net.md_5.bungee.api.ChatColor;

public class ArgumentParseException extends Exception {

    public ArgumentParseException(String message) {
        super(ChatColor.translateAlternateColorCodes('&', message));
    }
}
