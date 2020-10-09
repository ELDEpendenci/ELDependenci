package com.ericlam.mc.test.eld;

import com.google.inject.Module;
import com.google.inject.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
public class TestModule implements Module {

    private Map<Class, Object> toBind = new HashMap<>();
    private Map<TypeLiteral, Object> generics = new HashMap<>();

    private Set<Class<? extends ToInject<? extends Number>>> bindInject = new HashSet<>();

    @Override
    public void configure(Binder binder) {
        //binder.bind(BeInject.class).in(Scopes.SINGLETON);
        //binder.bind(BeInject2.class).in(Scopes.SINGLETON);

        toBind.forEach((cls, instance) -> binder.bind(cls).toInstance(instance));
        generics.forEach((key, o) -> binder.bind(key).toInstance(o));
        bindInject.forEach(i -> binder.bind(i).in(Scopes.SINGLETON));
    }


    public <T> void bind(Class<T> cls, T instance) {
        this.toBind.put(cls, instance);
    }

    public <T> void bindSingleton(T ins){
        this.toBind.put(ins.getClass(), ins);
    }


    public <T> void bindGeneric(TypeLiteral<T> literal, T o) {
        this.generics.put(literal, o);
    }

    public <T> void bindGeneric(T o) {
        this.generics.put(TypeLiteral.get(o.getClass()), o);
    }

    public <T extends ToInject<? extends Number>> void bindInject(Class<T> cls){
        this.bindInject.add(cls);
    }
}
