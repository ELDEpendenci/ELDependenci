package com.ericlam.mc.eld;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ELDBungee {

    Class<? extends BungeeLifeCycle> lifeCycle();

    Class<? extends BungeeRegistry> registry();

}
