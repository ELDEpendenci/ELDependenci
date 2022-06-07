package com.ericlam.mc.eld.annotations;

import java.lang.annotation.*;

/**
 * 文件池標註
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GroupResource {

    /**
     * 文件夾名稱
     * @return 文件夾名稱
     */
    String folder();

    /**
     * 列表內的文件會被預先儲存到插件文件夾
     * @return 文件名稱
     */
    String[] preloads() default {};

}
