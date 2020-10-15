package com.ericlam.mc.eld.managers;

import com.ericlam.mc.eld.misc.ArgParser;

/**
 * 參數解析管理器
 */
public interface ArgumentManager {

    /**
     * 註冊參數解析
     * @param parser 要解析的類型
     * @param getter 解析器
     * @param <T> 類型
     */
    <T> void registerParser(Class<T> parser, ArgParser<T> getter);

    /**
     * 註冊有標識文字的參數解析
     * @param parser 要解析的類型
     * @param identifier 標識文字
     * @param getter 解析器
     * @param <T> 類型
     */
    <T> void registerParser(Class<T> parser, String identifier, ArgParser<T> getter);

}
