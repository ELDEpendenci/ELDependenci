package com.ericlam.mc.eld;

import com.google.inject.Module;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

public class ELDependenci extends Plugin implements ELDependenciAPI, MCPlugin, AddonInstallation {

    private final ELDCommonModule commonModule = new ELDCommonModule(this);

    @Override
    public void installModule(Module module) {

    }

    @Override
    public <T> void customInstallation(Class<T> regCls, T ins) {

    }

    @Override
    public ManagerProvider<?> register(ELDPlugin plugin, Consumer<ServiceCollection> injector) {
        return null;
    }

    @Override
    public <T> T exposeService(Class<T> serviceCls) {
        return null;
    }

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
