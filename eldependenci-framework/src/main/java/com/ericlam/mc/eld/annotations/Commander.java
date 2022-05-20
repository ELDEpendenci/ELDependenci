package com.ericlam.mc.eld.annotations;

import java.lang.annotation.*;

/**
 * 用於標註指令
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Commander {

    /**
     *
     * @return 指令名稱
     */
    String name();

    /**
     *
     * @return 指令描述
     */
    String description();

    /**
     *
     * @return 僅限玩家
     */
    boolean playerOnly() default false;

    /**
     *
     * @return 權限
     */
    String permission() default "";

    /**
     *
     * @return 指令別稱
     */
    String[] alias() default {};

}
