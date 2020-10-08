package com.ericlam.mc.eld;

import org.reflections.Reflections;

import java.util.function.Consumer;

public class ELDenpendenci {

    public static void register(Object plugin, Consumer<ELDependencyInjector> injector){
        System.out.println(plugin.getClass().getPackage().getName());
        Reflections ref = new Reflections(plugin.getClass().getPackage().getName());
    }



}
