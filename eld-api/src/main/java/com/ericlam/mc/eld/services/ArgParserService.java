package com.ericlam.mc.eld.services;

import com.ericlam.mc.eld.exceptions.ArgumentParseException;
import org.bukkit.command.CommandSender;

import java.util.Iterator;

public interface ArgParserService {

    <T> T tryParse(Class<T> type, String identifier, Iterator<String> args, CommandSender sender) throws ArgumentParseException;

    <T> T tryParse(Class<T> type, Iterator<String> args, CommandSender sender) throws ArgumentParseException;

}
