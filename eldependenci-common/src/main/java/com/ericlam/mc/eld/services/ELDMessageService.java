package com.ericlam.mc.eld.services;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Content;

public class ELDMessageService implements MessageService {

    @Override
    public MessageFactory edit(String... msg) {
        return new ELDMessageFactory();
    }

    private static class ELDMessageFactory implements MessageFactory {

        private final ComponentBuilder componentBuilder;

        public ELDMessageFactory(String... msg) {
            this.componentBuilder = new ComponentBuilder("");
            this.add(msg);
        }

        @Override
        public MessageFactory add(String... msg) {
            for (int i = 0; i < msg.length; i++) {
                var txt = ChatColor.translateAlternateColorCodes('&', msg[i]);
                componentBuilder.append(TextComponent.fromLegacyText(txt));
                if (i != msg.length - 1) {
                    this.nextLine();
                }
            }
            return this;
        }

        @Override
        public MessageFactory url(String website) {
            componentBuilder.event(new ClickEvent(ClickEvent.Action.OPEN_URL, website));
            return this;
        }

        @Override
        public MessageFactory command(String command) {
            componentBuilder.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
            return this;
        }

        @Override
        public MessageFactory suggest(String suggest) {
            componentBuilder.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggest));
            return this;
        }

        @Override
        public MessageFactory page(String page) {
            componentBuilder.event(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, page));
            return this;
        }

        @Override
        public MessageFactory hoverText(Content... contents) {
            componentBuilder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, contents));
            return this;
        }

        @Override
        public MessageFactory nextLine() {
            componentBuilder.append("\n");
            return this;
        }

        @Override
        public BaseComponent[] build() {
            return componentBuilder.create();
        }
    }
}
