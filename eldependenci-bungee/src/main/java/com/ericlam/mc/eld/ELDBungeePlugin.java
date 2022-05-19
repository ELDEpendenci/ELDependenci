package com.ericlam.mc.eld;

import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public abstract class ELDBungeePlugin extends Plugin implements ELDPlugin {

    @Override
    public final void onLoad() {
        final var provider = ELDependenci.getApi().register(this, this::bindServices);
        this.manageProvider((BungeeManageProvider) provider);
    }


    @Override
    public final void onEnable() {
        getLogger().info("Enabling plugin " + this.getName());
        getLogger().info("This plugin is hooking the lifecycle of ELDependenci framework.");
    }

    @Override
    public final void onDisable() {
        getLogger().info("Disabling plugin " + this.getName());
    }

    /**
     * 用於生命週期之前的操作
     *
     * @param provider 管理器提供
     */
    protected abstract void manageProvider(BungeeManageProvider provider);


    @Override
    public void saveResource(String path, boolean replace) {
        var target = new File(getDataFolder(), path);
        var ins = this.getResourceAsStream(path);
        try {
            Files.copy(ins, target.toPath());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot copy file: " + path, e);
        }
    }

    @Override
    public String getName() {
        return this.getDescription().getName();
    }
}
