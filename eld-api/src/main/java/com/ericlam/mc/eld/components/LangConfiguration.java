package com.ericlam.mc.eld.components;

import com.ericlam.mc.eld.controllers.LangController;

public abstract class LangConfiguration extends Configuration {
    private LangController lang;

    public final LangController getLang() {
        return lang;
    }
}
