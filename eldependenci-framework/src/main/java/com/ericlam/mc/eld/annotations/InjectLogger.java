package com.ericlam.mc.eld.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 透過標註注入 DebugLogger
 */
@Target({ElementType.FIELD, ElementType.TYPE_PARAMETER, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectLogger {

    /**
     * DebugLogger 的名稱，如無則以注入 class 作為名稱
     * @return 自定義名稱
     */
    String name() default "";

}
