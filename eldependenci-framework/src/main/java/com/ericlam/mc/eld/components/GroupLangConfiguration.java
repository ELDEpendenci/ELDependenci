package com.ericlam.mc.eld.components;

/**
 * 用於定義語言文件池
 */
public abstract class GroupLangConfiguration extends LangConfiguration {

    private String locale;

    /**
     * 獲取文件語言標簽
     * @return 語言標簽
     */
    public String getLocale() {
        return locale;
    }

}
