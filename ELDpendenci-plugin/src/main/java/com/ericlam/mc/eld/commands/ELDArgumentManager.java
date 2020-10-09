package com.ericlam.mc.eld.commands;

import com.ericlam.mc.eld.exceptions.ArgumentParseException;
import com.ericlam.mc.eld.managers.ArgParser;
import com.ericlam.mc.eld.managers.ArgumentManager;
import org.bukkit.command.CommandSender;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ELDArgumentManager implements ArgumentManager {

    private final Map<Class<?>, Map<String, ArgParser<?>>> parserMap = new ConcurrentHashMap<>();

    @Override
    public <T> void registerParser(Class<T> parser, ArgParser<T> getter) {
        this.registerParser(parser, "default", getter);
    }

    @Override
    public <T> void registerParser(Class<T> parser, String identifier, ArgParser<T> getter) {
        this.parserMap.putIfAbsent(parser, new ConcurrentHashMap<>());
        this.parserMap.get(parser).put(identifier, getter);
    }

    public <T> T tryParse(Class<T> type, String identifier, Iterator<String> args, CommandSender sender) throws ArgumentParseException {
        var getter = Optional.ofNullable(this.parserMap.get(type)).map(p -> p.get(identifier)).orElseThrow(() -> new IllegalStateException("找不到 " + type + " 的參數轉換器。"));
        return type.cast(getter.parse(args, sender));
    }

}
