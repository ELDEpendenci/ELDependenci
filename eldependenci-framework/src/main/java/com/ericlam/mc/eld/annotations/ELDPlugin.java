package com.ericlam.mc.eld.annotations;

import com.ericlam.mc.eld.ELDLifeCycle;
import com.ericlam.mc.eld.registrations.ComponentsRegistry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 標註為ELD插件
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ELDPlugin {

    /**
     *
     * @return 組件註冊類定位
     */
    Class<? extends ComponentsRegistry> registry();

    /**
     *
     * @return 生命週期類定位
     */
    Class<? extends ELDLifeCycle> lifeCycle();


}
