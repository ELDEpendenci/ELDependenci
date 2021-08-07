package com.ericlam.mc.eld;

import com.google.inject.Module;

/**
 * Addon 管理器
 */
public interface AddonManager {

    /**
     * 安裝 Module
     * @param module Guice Module
     */
    void installModule(Module module);

    /**
     * 新增自定義安裝
     * @param regCls 自定義安裝類
     * @param ins 實例
     * @param <T> 安裝類
     */
    <T> void customInstallation(Class<T> regCls, T ins);

}
