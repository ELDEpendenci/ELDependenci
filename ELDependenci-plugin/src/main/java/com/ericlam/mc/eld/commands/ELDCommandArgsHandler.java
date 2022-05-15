package com.ericlam.mc.eld.commands;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public final class ELDCommandArgsHandler {

    private final Map<Class<? extends Annotation>, ArgsHandler> argsHandler = new LinkedHashMap<>();

    public <A extends Annotation> void registerArgAHandle(Class<A> annotation,
                                                          CommandArgHandler<A> handle,
                                                          Function<A, CommonProperties> propertyGetter) {
        argsHandler.putIfAbsent(annotation,
                new ArgsHandler(
                        (annotation1, type, argumentManager, iterator, sender) -> handle.argHandle(annotation.cast(annotation1), type, argumentManager, iterator, sender),
                        annotation1 -> propertyGetter.apply(annotation.cast(annotation1
                        ))));
    }

    private CommonProperties getCommonProperties(Field field) {
        return argsHandler.entrySet()
                .stream()
                .filter(en -> field.isAnnotationPresent(en.getKey()))
                .findFirst()
                .map(en -> en.getValue().argsSorter.apply(field.getAnnotation(en.getKey())))
                .orElseThrow(() -> new IllegalStateException("Field " + field.getName() + " has no annotation!"));
    }

    public Map.Entry<WideCommandArgHandler, CommonProperties> getArgsEntry(Field field) {
        return argsHandler.entrySet()
                .stream()
                .filter(en -> field.isAnnotationPresent(en.getKey()))
                .findFirst()
                .map(en ->
                        new AbstractMap.SimpleEntry<WideCommandArgHandler, CommonProperties>(
                                (type, argumentManager, iterator, sender) -> {
                                    return en.getValue().argsHandler.argHandle(field.getAnnotation(en.getKey()), type, argumentManager, iterator, sender);
                                },
                                en.getValue().argsSorter.apply(field.getAnnotation(en.getKey()))))
                .orElseThrow(() -> new IllegalStateException("Field " + field.getName() + " has no annotation!"));
    }


    public String getPlaceholderLabel(Field field) {
        var arg = getCommonProperties(field);
        String label;
        if (arg.labels.length > 0) {
            label = String.join(", ", arg.labels);
        } else {
            label = field.getName();
        }
        return arg.optional ? "[" + label + "]" : "<" + label + ">";
    }


    public int sortArgsField(Field f1, Field f2) {
        var sorter1 = getCommonProperties(f1);
        var sorter2 = getCommonProperties(f2);
        return Integer.compare(sorter1.order, sorter2.order) * Boolean.compare(sorter1.optional, sorter2.optional);
    }


    public boolean isCommandArg(Field field) {
        return argsHandler.keySet().stream().anyMatch(field::isAnnotationPresent);
    }


    public record CommonProperties(int order, boolean optional, String[] labels) {

    }

    private record ArgsHandler(CommandArgHandler<Annotation> argsHandler,
                               Function<Annotation, CommonProperties> argsSorter) {
    }
}
