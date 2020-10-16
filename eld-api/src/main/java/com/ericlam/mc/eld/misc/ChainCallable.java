package com.ericlam.mc.eld.misc;

@FunctionalInterface
public interface ChainCallable<E, R> {

    R call(E e) throws Exception;

}
