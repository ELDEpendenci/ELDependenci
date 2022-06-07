package com.ericlam.mc.eld;

import com.ericlam.mc.eld.bungee.ComponentsRegistry;
import com.ericlam.mc.eld.bungee.ELDLifeCycle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 標註為 ELD Bungee 插件
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ELDBungee {

    /**
     *
     * @return 生命週期類定位
     */
    Class<? extends ELDLifeCycle> lifeCycle();


    /**
     *
     * @return 組件註冊類定位
     */
    Class<? extends ComponentsRegistry> registry();

}
