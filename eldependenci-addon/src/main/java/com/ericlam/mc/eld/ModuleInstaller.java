package com.ericlam.mc.eld;

import com.google.inject.Module;

/**
 * Module 安裝 (Addon 專用)
 */
public interface ModuleInstaller {

    /**
     * 安裝 Module
     * @param module Guice Module
     */
    void install(Module module);

}
