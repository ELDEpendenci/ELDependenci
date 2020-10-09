package com.ericlam.mc.eld.managers;

public interface ArgumentManager {

    <T> void registerParser(Class<T> parser, ArgParser<T> getter);

    <T> void registerParser(Class<T> parser, String identifier, ArgParser<T> getter);

}
