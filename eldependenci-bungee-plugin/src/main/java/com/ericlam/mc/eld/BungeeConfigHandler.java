package com.ericlam.mc.eld;

import com.ericlam.mc.eld.configurations.MessageYaml;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.util.List;

public class BungeeConfigHandler implements ConfigHandler{

    private final ConfigurationProvider provider = ConfigurationProvider.getProvider(YamlConfiguration.class);

    @Override
    public MessageYaml loadYaml(File file) throws Exception {
        Configuration configuration = provider.load(file);
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
