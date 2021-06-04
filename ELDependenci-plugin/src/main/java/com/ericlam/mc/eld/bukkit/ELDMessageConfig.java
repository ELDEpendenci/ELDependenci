package com.ericlam.mc.eld.bukkit;

import com.ericlam.mc.eld.annotations.Prefix;
import com.ericlam.mc.eld.annotations.Resource;
import com.ericlam.mc.eld.components.LangConfiguration;

import java.text.MessageFormat;

@Prefix(path = "prefix")
@Resource(locate = "messages.yml")
public final class ELDMessageConfig extends LangConfiguration {

    public String getConvertError(String path, Object... objects){
        return MessageFormat.format(getLang().get("convert-error.".concat(path)), objects);
    }
}
