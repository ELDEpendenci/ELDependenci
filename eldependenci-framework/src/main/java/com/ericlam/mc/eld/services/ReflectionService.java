package com.ericlam.mc.eld.services;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
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

    /**
     * 從快取獲取反射加快速度
     * @param method 方法
     * @return 標註列表
     */
    Annotation[] getDeclaredAnnotations(Method method);

    /**
     * 從快取獲取反射加快速度
     * @param clazz 類別
     * @return 類別標註列表
     */
    Annotation[] getDeclaredAnnotations(Class<?> clazz);

    /**
     * 從快取獲取反射加快速度
     * @param clazz 類別
     * @return 公共方法列表
     */
    Method[] getMethods(Class<?> clazz);

    /**
     * 從快取獲取反射加快速度
     * @param method 方法
     * @return 參數標註列表
     */
    Annotation[][] getParameterAnnotations(Method method);

    /**
     * 從快取獲取反射加快速度
     * @param method 方法
     * @return 參數類型列表
     */
    Type[] getParameterTypes(Method method);

}
