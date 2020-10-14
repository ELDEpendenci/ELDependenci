package com.ericlam.mc.eld.services;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.hover.content.Content;

public interface MessageService {

    MessageFactory edit(String... msg);

    interface MessageFactory {

        MessageFactory add(String... msg);

        MessageFactory url(String website);

        MessageFactory command(String command);

        MessageFactory suggest(String suggest);

        MessageFactory page(String page);

        MessageFactory hoverText(Content... hoverTxt);

        MessageFactory nextLine();

        BaseComponent[] build();

    }
}
