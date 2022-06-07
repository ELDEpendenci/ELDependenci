package com.ericlam.mc.eld.services;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ELDReflectionService implements ReflectionService {

    @Override
    public List<Field> getDeclaredFieldsUpTo(@Nonnull Class<?> startClass, @Nullable Class<?> exclusiveParent) {
        return getDeclaredFieldsUpToStatic(startClass, exclusiveParent != null ? exclusiveParent : Object.class);
    }

    @Override
    public List<Method> getDeclaredMethodsUpTo(@Nonnull Class<?> startClass, @Nullable Class<?> exclusiveParent) {
        return getDeclaredMethodsUpToStatic(startClass, exclusiveParent != null ? exclusiveParent : Object.class);
    }

    private static final Map<Class<?>, Field[]> declaredFieldsCache = new HashMap<>();
    private static final Map<Class<?>, Method[]> declaredMethodsCache = new HashMap<>();

    private static Field[] getDeclareFields(Class<?> cls){
        if (declaredFieldsCache.containsKey(cls)){
            return declaredFieldsCache.get(cls);
        }else{
            Field[] fields = cls.getDeclaredFields();
            declaredFieldsCache.put(cls, fields);
            return fields;
        }
    }

    private static Method[] getDeclareMethods(Class<?> cls){
        if (declaredMethodsCache.containsKey(cls)){
            return declaredMethodsCache.get(cls);
        }else{
            Method[] methods = cls.getDeclaredMethods();
            declaredMethodsCache.put(cls, methods);
            return methods;
        }
    }


    public static List<Field> getDeclaredFieldsUpToStatic(@Nonnull Class<?> startClass, @Nullable Class<?> exclusiveParent) {
        List<Field> currentClassFields = Lists.newArrayList(getDeclareFields(startClass));
        Class<?> parentClass = startClass.getSuperclass();

        if (parentClass != null && !(parentClass.equals(exclusiveParent))) {
            List<Field> parentClassFields =
                    (List<Field>) getDeclaredFieldsUpToStatic(parentClass, exclusiveParent);
            currentClassFields.addAll(parentClassFields);
        }

        return currentClassFields;
    }

    public List<Method> getDeclaredMethodsUpToStatic(@Nonnull Class<?> startClass, @Nullable Class<?> exclusiveParent) {
        List<Method> currentClassMethods = Lists.newArrayList(getDeclareMethods(startClass));
        Class<?> parentClass = startClass.getSuperclass();

        if (parentClass != null && !(parentClass.equals(exclusiveParent))) {
            List<Method> parentClassFields =
                    (List<Method>) getDeclaredMethodsUpToStatic(parentClass, exclusiveParent);
            currentClassMethods.addAll(parentClassFields);
        }

        return currentClassMethods;
    }

}
