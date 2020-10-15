package com.ericlam.mc.eld.services;

import com.ericlam.mc.eld.exceptions.ArgumentParseException;
import org.bukkit.command.CommandSender;

import java.util.Iterator;

/**
 * 參數解析器
 */
public interface ArgParserService {

    /**
     * 嘗試解析參數
     * @param type 解析類別
     * @param identifier 標識文字
     * @param args 參數
     * @param sender 指令發送者
     * @param <T> 解析類別
     * @return 實例
     * @throws ArgumentParseException 參數解析失敗時
     */
    <T> T tryParse(Class<T> type, String identifier, Iterator<String> args, CommandSender sender) throws ArgumentParseException;

    /**
     * 嘗試解析參數
     * @param type 解析類別
     * @param args 參數
     * @param sender 指令發送者
     * @param <T> 解析類別
     * @return 實例
     * @throws ArgumentParseException 參數解析失敗時
     */
    <T> T tryParse(Class<T> type, Iterator<String> args, CommandSender sender) throws ArgumentParseException;

}
