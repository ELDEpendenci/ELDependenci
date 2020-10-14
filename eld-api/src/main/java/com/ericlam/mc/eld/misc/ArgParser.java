package com.ericlam.mc.eld.misc;

import com.ericlam.mc.eld.exceptions.ArgumentParseException;
import com.ericlam.mc.eld.services.ArgParserService;
import org.bukkit.command.CommandSender;

import java.util.Iterator;

@FunctionalInterface
public interface ArgParser<T> {

    T parse(Iterator<String> args, CommandSender sender, ArgParserService parser) throws ArgumentParseException;

}
