package com.ericlam.mc.eld;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

public class ELDependenci implements ELDependenciAPI {


    public static ELDependenciAPI getApi() {
        throw new UnsupportedOperationException("not plugin");
    }

    public ManagerProvider register(JavaPlugin plugin, Consumer<ServiceCollection> injector) {
        return null;
    }


}
