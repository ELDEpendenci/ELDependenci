package com.ericlam.mc.test.eld;

import com.ericlam.mc.eld.annotations.Commander;
import com.ericlam.mc.eld.components.CommandNode;
import org.bukkit.command.CommandSender;

@Commander(
        name = "test",
        description = "test command"
)
public class TestCommand implements CommandNode {

    @Override
    public void execute(CommandSender sender) {
        sender.sendMessage("hello world!");
    }
}
