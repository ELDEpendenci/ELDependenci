package com.ericlam.mc.eld.guice;

import com.google.inject.MembersInjector;

import java.lang.reflect.Field;

public class InstanceInjector<T> implements MembersInjector<T> {

    private final Field field;
    private final Object value;

    public InstanceInjector(Field field, Object value) {
        this.field = field;
        this.value = value;
        field.setAccessible(true);
    }


    @Override
    public void injectMembers(T t) {
        try {
            field.set(t, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
