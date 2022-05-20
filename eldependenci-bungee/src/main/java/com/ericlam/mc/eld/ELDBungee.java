package com.ericlam.mc.eld;

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
    Class<? extends BungeeLifeCycle> lifeCycle();


    /**
     *
     * @return 組件註冊類定位
     */
    Class<? extends BungeeRegistry> registry();

}
