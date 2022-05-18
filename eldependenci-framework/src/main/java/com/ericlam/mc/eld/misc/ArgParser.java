package com.ericlam.mc.eld.misc;

import com.ericlam.mc.eld.exceptions.ArgumentParseException;
import com.ericlam.mc.eld.services.ArgParserService;

import java.util.Iterator;

@FunctionalInterface
public interface ArgParser<T, Sender> {

    T parse(Iterator<String> args, Sender sender, ArgParserService<Sender> parser) throws ArgumentParseException;

}
