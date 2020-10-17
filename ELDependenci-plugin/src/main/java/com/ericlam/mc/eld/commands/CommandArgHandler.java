package com.ericlam.mc.eld.commands;

import com.ericlam.mc.eld.exceptions.ArgumentParseException;
import org.bukkit.command.CommandSender;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.NoSuchElementException;

@FunctionalInterface
public interface CommandArgHandler<A extends Annotation> {

    Object argHandle(A annotation, Class<?> type, ELDArgumentManager argumentManager, Iterator<String> iterator, CommandSender sender) throws NoSuchElementException, ArgumentParseException;

}
