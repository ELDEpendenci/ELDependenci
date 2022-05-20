package com.ericlam.mc.eld.annotations;

import java.lang.annotation.*;

/**
 * 前綴，用於訊息文件
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Prefix {
    /**
     *
     * @return 前綴路徑
     */
    String path();
}
