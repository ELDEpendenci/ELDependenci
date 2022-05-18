package com.ericlam.mc.eld.services;

import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ELDReflectionService implements ReflectionService {

    @Override
    public List<Field> getDeclaredFieldsUpTo(@Nonnull Class<?> startClass, @Nullable Class<?> exclusiveParent) {
        return getDeclaredFieldsUpToStatic(startClass, exclusiveParent != null ? exclusiveParent : Object.class);
    }

    @Override
    public List<Method> getDeclaredMethodsUpTo(@Nonnull Class<?> startClass, @Nullable Class<?> exclusiveParent) {
        return getDeclaredMethodsUpToStatic(startClass, exclusiveParent != null ? exclusiveParent : Object.class);
    }

    public static List<Field> getDeclaredFieldsUpToStatic(@Nonnull Class<?> startClass, @Nullable Class<?> exclusiveParent) {
        List<Field> currentClassFields = Lists.newArrayList(startClass.getDeclaredFields());
        Class<?> parentClass = startClass.getSuperclass();

        if (parentClass != null && !(parentClass.equals(exclusiveParent))) {
            List<Field> parentClassFields =
                    (List<Field>) getDeclaredFieldsUpToStatic(parentClass, exclusiveParent);
            currentClassFields.addAll(parentClassFields);
        }

        return currentClassFields;
    }

    public List<Method> getDeclaredMethodsUpToStatic(@Nonnull Class<?> startClass, @Nullable Class<?> exclusiveParent) {
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
