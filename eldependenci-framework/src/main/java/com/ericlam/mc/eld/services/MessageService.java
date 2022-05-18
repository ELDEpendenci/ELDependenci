package com.ericlam.mc.eld.services;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.hover.content.Content;

/**
 * 訊息編輯器
 */
public interface MessageService {

    /**
     * 編輯訊息
     *
     * @param msg 訊息
     * @return this
     */
    MessageFactory edit(String... msg);

    interface MessageFactory {

        /**
         * 新增訊息
         *
         * @param msg 訊息
         * @return this
         */
        MessageFactory add(String... msg);

        /**
         * 新增可點擊網址
         *
         * @param website 網址
         * @return this
         */
        MessageFactory url(String website);

        /**
         * 新增指令執行
         *
         * @param command 指令
         * @return this
         */
        MessageFactory command(String command);

        /**
         * 新增建議訊息
         *
         * @param suggest 建議訊息
         * @return this
         */
        MessageFactory suggest(String suggest);

        /**
         * 新增換頁操作
         *
         * @param page 頁數
         * @return this
         */
        MessageFactory page(String page);

        /**
         * 新增浮動文字
         *
         * @param hoverTxt 浮動文字
         * @return this
         */
        MessageFactory hoverText(Content... hoverTxt);

        /**
         * 換行
         *
         * @return this
         */
        MessageFactory nextLine();

        /**
         * 生成訊息
         *
         * @return 訊息
         */
        BaseComponent[] build();

    }
}
