package com.ericlam.mc.eld.registrations;

public interface ComponentsRegistry {

    void registerCommand(CommandRegistry registry);

    void registerListeners(CommandRegistry registry);

    void registerConfigs();
}
