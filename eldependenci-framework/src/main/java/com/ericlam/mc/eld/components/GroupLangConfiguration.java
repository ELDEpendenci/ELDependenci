package com.ericlam.mc.eld.components;

import com.ericlam.mc.eld.controllers.LangController;

/**
 * 用於定義語言文件池
 */
public abstract class GroupLangConfiguration {

    private String locale;
    private LangController lang;

    /**
     * 獲取文件語言標簽
     * @return 語言標簽
     */
    public String getLocale() {
        return locale;
    }

    /**
     * 獲取訊息文件控制器
     * @return 訊息文件控制器
     */
    public final LangController getLang() {
        return lang;
    }

}
