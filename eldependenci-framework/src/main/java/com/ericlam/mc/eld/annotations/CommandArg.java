package com.ericlam.mc.eld.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指令參數
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandArg {

    /**
     * 參數順序
     * @return 參數順序
     */
    int order();

    /**
     * 參數標識
     * @return 參數標識
     */
    String identifier() default "default";

    /**
     * 自定義參數顯示
     * @return 自定義參數顯示
     */
    String[] labels() default {};

    /**
     * 可填參數
     * @return 是否為可填參數
     */
    boolean optional() default false;

}
