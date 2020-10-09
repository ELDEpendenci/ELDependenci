package com.ericlam.mc.eld.registrations;

import com.ericlam.mc.eld.components.CommandNode;

import java.util.function.Consumer;

public interface CommandRegistry {

    void command(Class<? extends CommandNode> node, Consumer<CommandRegistry> child);

    void command(Class<? extends CommandNode> node);

}
