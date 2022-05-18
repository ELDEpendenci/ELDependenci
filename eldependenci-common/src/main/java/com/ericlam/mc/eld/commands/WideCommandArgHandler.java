package com.ericlam.mc.eld.commands;

import com.ericlam.mc.eld.exceptions.ArgumentParseException;
import com.ericlam.mc.eld.managers.ArgumentManager;

import java.util.Iterator;
import java.util.NoSuchElementException;

@FunctionalInterface
public interface WideCommandArgHandler<CommandSender> {

    Object argHandle(Class<?> type, ELDArgumentManager<CommandSender> argumentManager, Iterator<String> iterator, CommandSender sender) throws NoSuchElementException, ArgumentParseException;

}
