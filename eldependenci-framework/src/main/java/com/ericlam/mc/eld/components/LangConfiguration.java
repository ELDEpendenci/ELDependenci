package com.ericlam.mc.eld.components;

import com.ericlam.mc.eld.controllers.LangController;

/**
 * 用於定義訊息文件
 */
public abstract class LangConfiguration extends Configuration {
    private LangController lang;

    /**
     * 獲取訊息文件控制器
     * @return 訊息文件控制器
     */
    public final LangController getLang() {
        return lang;
    }
}
