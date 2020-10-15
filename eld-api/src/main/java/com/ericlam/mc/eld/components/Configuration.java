package com.ericlam.mc.eld.components;

import com.ericlam.mc.eld.controllers.FileController;

/**
 * 用於定義文件配置
 */
public abstract class Configuration {

    private FileController controller;

    /**
     * 獲取文件配置控制器
     * @return 文件配置控制器
     */
    public final FileController getController() {
        return controller;
    }
}
