package com.ericlam.mc.eld.annotations;

import com.ericlam.mc.eld.components.CommandNode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Commander {

    String name();

    String description();

    boolean playerOnly() default false;

    String permission() default "";

    String[] alias() default {};

}
