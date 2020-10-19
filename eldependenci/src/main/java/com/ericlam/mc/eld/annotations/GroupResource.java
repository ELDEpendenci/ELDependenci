package com.ericlam.mc.eld.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 文件池標註
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GroupResource {

    /**
     * 文件夾名稱
     * @return 文件夾名稱
     */
    String folder();

}
