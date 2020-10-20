package com.ericlam.mc.eld.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 多態指令參數
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamicArg {

    /**
     * 參數順序
     * @return 參數順序
     */
    int order();

    /**
     * 可能的參數類型 (順序)
     * @return 可能的參數類型
     */
    Class<?>[] types();

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
