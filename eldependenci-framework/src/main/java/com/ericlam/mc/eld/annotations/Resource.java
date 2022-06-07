package com.ericlam.mc.eld.annotations;

import java.lang.annotation.*;

/**
 * 定義文件配置
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Resource {

    /**
     *
     * @return 文件名稱
     */
    String locate();
}
