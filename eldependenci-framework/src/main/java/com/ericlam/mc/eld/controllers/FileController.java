package com.ericlam.mc.eld.controllers;

import java.io.IOException;

/**
 * 文件控制器
 */
public interface FileController {

    /**
     * 重載文件
     * @return 是否成功
     */
    boolean reload();

    /**
     * 儲存文件
     * @throws IOException 儲存失敗時
     */
    void save() throws IOException;

}
