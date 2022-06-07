package com.ericlam.mc.eld.annotations;

import java.lang.annotation.*;

/**
 * 指定默認語言
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultLanguage {

    /**
     *
     * @return 默認語言數值
     */
    String value();

}
