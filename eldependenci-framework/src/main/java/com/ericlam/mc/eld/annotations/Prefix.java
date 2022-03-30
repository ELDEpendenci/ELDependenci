package com.ericlam.mc.eld.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 前綴，用於訊息文件
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Prefix {
    /**
     *
     * @return 前綴路徑
     */
    String path();
}
