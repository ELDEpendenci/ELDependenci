package com.ericlam.mc.eld.commands;

import com.ericlam.mc.eld.exceptions.ArgumentParseException;
import com.ericlam.mc.eld.managers.ArgumentManager;
import com.ericlam.mc.eld.misc.ArgParser;
import com.ericlam.mc.eld.services.ArgParserService;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class ELDArgumentManager<Sender> implements ArgumentManager<Sender>, ArgParserService<Sender> {

    private final Map<Class<?>, Map<String, ArgParser<?, Sender>>> parserMap = new ConcurrentHashMap<>();

    @Override
    public <T> void registerParser(Class<T> parser, ArgParser<T, Sender> getter) {
        this.registerParser(parser, "default", getter);
    }

    @Override
    public <T> void registerParser(Class<T> parser, String identifier, ArgParser<T, Sender> getter) {
        this.parserMap.putIfAbsent(parser, new ConcurrentHashMap<>());
        this.parserMap.get(parser).put(identifier, getter);
    }

    @Override
    public <T> T tryParse(Class<T> type, String identifier, Iterator<String> args, Sender sender) throws ArgumentParseException {
        if (!type.isEnum()) {
            var getter = Optional.ofNullable(this.parserMap.get(type)).map(p -> p.get(identifier)).orElseThrow(() -> new IllegalStateException("找不到 " + type + " 的參數轉換器。"));
            try {
                return type.cast(getter.parse(args, sender, this));
            } catch (ClassCastException e) {
                throw new ArgumentParseException("形態轉換失敗或參數為空值: " + type.getSimpleName());
            }
        } else {
            return parseEnum(type, args);
        }

    }

    @Override
    public <T> T tryParse(Class<T> type, Iterator<String> args, Sender sender) throws ArgumentParseException {
        return this.tryParse(type, "default", args, sender);
    }

    public Object multiParse(Class<?>[] types, Iterator<String> iterator, Sender sender) throws ArgumentParseException {
        if (!(iterator instanceof ArgIterator argIterator))
            throw new IllegalStateException("MultiParsing 必須使用 ArgIterator");
        Class<?> suitableType = null;
        for (Class<?> t : types) {
            var testIterator = argIterator.cloneIterator(true);
            try {
                tryParse(t, testIterator, sender);
                suitableType = t;
                break;
            } catch (ArgumentParseException ignored) {
                // not suitable type, next
            }
        }
        if (suitableType == null)
            throw new ArgumentParseException("沒有合適的參數形態: " + Arrays.stream(types).map(Class::getSimpleName).collect(Collectors.joining(", ")));
        return tryParse(suitableType, iterator, sender);
    }

    private <T> T parseEnum(Class<T> enumType, Iterator<String> args) throws ArgumentParseException {
        final var value = args.next();
        for (T constant : enumType.getEnumConstants()) {
            if (((Enum<?>) constant).name().equalsIgnoreCase(value)) {
                return constant;
            }
        }
        var str = Arrays.stream(enumType.getEnumConstants()).limit(20).map(e -> ((Enum<?>) e).name()).collect(Collectors.joining(", "));
        throw new ArgumentParseException("未知的變數，可用變數: ".concat(str.concat(enumType.getEnumConstants().length > 20 ? "..." : "")));
    }

}
