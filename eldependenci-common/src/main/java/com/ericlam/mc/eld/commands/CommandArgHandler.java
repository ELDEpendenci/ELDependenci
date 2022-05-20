package com.ericlam.mc.eld.commands;

import com.ericlam.mc.eld.exceptions.ArgumentParseException;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.NoSuchElementException;

@FunctionalInterface
public interface CommandArgHandler<A extends Annotation, Sender> {

    Object argHandle(A annotation, Class<?> type, ELDArgumentManager<Sender> argumentManager, Iterator<String> iterator, Sender sender) throws NoSuchElementException, ArgumentParseException;

}
