package com.ericlam.mc.eld.commands;

import com.ericlam.mc.eld.exceptions.ArgumentParseException;
import org.bukkit.command.CommandSender;

import java.util.Iterator;
import java.util.NoSuchElementException;

@FunctionalInterface
public interface WideCommandArgHandler {

    Object argHandle(Class<?> type, ELDArgumentManager argumentManager, Iterator<String> iterator, CommandSender sender) throws NoSuchElementException, ArgumentParseException;

}
