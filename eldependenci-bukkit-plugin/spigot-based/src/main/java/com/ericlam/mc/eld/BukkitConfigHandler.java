package com.ericlam.mc.eld;

import com.ericlam.mc.eld.configurations.MessageYaml;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class BukkitConfigHandler implements ConfigHandler {

    @Override
    public MessageYaml loadYaml(File file) {
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        return new MessageYaml() {
            @Override
            public boolean contains(String path) {
                return configuration.contains(path);
            }

            @Override
            public String getString(String path) {
                return configuration.getString(path);
            }

            @Override
            public List<String> getStringList(String path) {
                return configuration.getStringList(path);
            }
        };
    }

}
