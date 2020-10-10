package com.ericlam.mc.eld.commands;

import com.ericlam.mc.eld.exceptions.ArgumentParseException;
import com.ericlam.mc.eld.misc.ArgParser;
import com.ericlam.mc.eld.managers.ArgumentManager;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
        if (!type.isEnum()) {
            var getter = Optional.ofNullable(this.parserMap.get(type)).map(p -> p.get(identifier)).orElseThrow(() -> new IllegalStateException("找不到 " + type + " 的參數轉換器。"));
            try{
                return type.cast(getter.parse(args, sender));
            }catch (ClassCastException e){
                throw new ArgumentParseException("形態轉換失敗或參數為空值: "+type.getSimpleName());
            }
        } else {
            return parseEnum(type, args);
        }

    }

    private <T> T parseEnum(Class<T> enumType, Iterator<String> args) throws ArgumentParseException {
        final var value = args.next();
        for (T constant : enumType.getEnumConstants()) {
            if (((Enum<?>)constant).name().equalsIgnoreCase(value)) {
                return constant;
            }
        }
        var str = Arrays.stream(enumType.getEnumConstants()).limit(20).map(e -> ((Enum<?>)e).name()).collect(Collectors.joining(", "));
        throw new ArgumentParseException("未知的變數，可用變數: ".concat(str));
    }

}
