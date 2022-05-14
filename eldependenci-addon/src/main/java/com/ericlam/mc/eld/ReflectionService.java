package com.ericlam.mc.eld;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 反射工具服務
 */
public interface ReflectionService {

    /**
     * 取得整個 class 上下關係的 declared fields
     *
     * @param startClass      開始類別
     * @param exclusiveParent 到此父類別停止
     * @return 整個 class 上下關係的 declared fields
     */
    List<Field> getDeclaredFieldsUpTo(@Nonnull Class<?> startClass, @Nullable Class<?> exclusiveParent);

    /**
     * 取得整個 class 上下關係的 declared methods
     *
     * @param startClass      開始類別
     * @param exclusiveParent 到此父類別停止
     * @return 整個 class 上下關係的 declared methods
     */
    List<Method> getDeclaredMethodsUpTo(@Nonnull Class<?> startClass, @Nullable Class<?> exclusiveParent);

}
