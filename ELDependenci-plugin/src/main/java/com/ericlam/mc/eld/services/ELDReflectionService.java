package com.ericlam.mc.eld.services;

import com.ericlam.mc.eld.ReflectionService;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ELDReflectionService implements ReflectionService {

    @Override
    public List<Field> getDeclaredFieldsUpTo(@NotNull Class<?> startClass, @Nullable Class<?> exclusiveParent) {
        return getDeclaredFieldsUpToStatic(startClass, exclusiveParent);
    }

    @Override
    public List<Method> getDeclaredMethodsUpTo(@NotNull Class<?> startClass, @Nullable Class<?> exclusiveParent) {
        return getDeclaredMethodsUpToStatic(startClass, exclusiveParent);
    }

    public static List<Field> getDeclaredFieldsUpToStatic(@NotNull Class<?> startClass, @Nullable Class<?> exclusiveParent) {
        List<Field> currentClassFields = Lists.newArrayList(startClass.getDeclaredFields());
        Class<?> parentClass = startClass.getSuperclass();

        if (parentClass != null && !(parentClass.equals(exclusiveParent))) {
            List<Field> parentClassFields =
                    (List<Field>) getDeclaredFieldsUpToStatic(parentClass, exclusiveParent);
            currentClassFields.addAll(parentClassFields);
        }

        return currentClassFields;
    }

    public List<Method> getDeclaredMethodsUpToStatic(@NotNull Class<?> startClass, @Nullable Class<?> exclusiveParent) {
        List<Method> currentClassMethods = Lists.newArrayList(startClass.getDeclaredMethods());
        Class<?> parentClass = startClass.getSuperclass();

        if (parentClass != null && !(parentClass.equals(exclusiveParent))) {
            List<Method> parentClassFields =
                    (List<Method>) getDeclaredMethodsUpToStatic(parentClass, exclusiveParent);
            currentClassMethods.addAll(parentClassFields);
        }

        return currentClassMethods;
    }

}
