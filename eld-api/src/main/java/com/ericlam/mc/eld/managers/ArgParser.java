package com.ericlam.mc.eld.managers;

import com.ericlam.mc.eld.exceptions.ArgumentParseException;
import org.bukkit.command.CommandSender;

import java.util.Iterator;

@FunctionalInterface
public interface ArgParser<T> {

    T parse(Iterator<String> args, CommandSender sender) throws ArgumentParseException;

}
