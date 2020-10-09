package com.ericlam.mc.eld.configurations;

import com.ericlam.mc.eld.ELDModule;
import com.ericlam.mc.eld.annotations.Prefix;
import com.ericlam.mc.eld.annotations.Resource;
import com.ericlam.mc.eld.components.Configuration;
import com.ericlam.mc.eld.components.LangConfiguration;
import com.ericlam.mc.eld.controllers.FileController;
import com.ericlam.mc.eld.controllers.LangController;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.google.inject.Injector;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ELDConfigManager implements ConfigStorage {

    private final ELDModule module;
    private final JavaPlugin plugin;
    private final ObjectMapper mapper;
    private final Map<Class<? extends Configuration>, Configuration> configurationMap = new LinkedHashMap<>();
    private Injector injector = null;

    public ELDConfigManager(ELDModule module, JavaPlugin plugin) {
        this.module = module;
        this.mapper = new ObjectMapper(new YAMLFactory()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                .enable(JsonParser.Feature.ALLOW_YAML_COMMENTS));
        this.mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(JsonParser.Feature.ALLOW_COMMENTS)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.skipType(FileController.class);
        this.skipType(LangController.class);
        this.plugin = plugin;
    }

    public void skipType(Class<?> type) {
        mapper.configOverride(type)
                .setVisibility(JsonAutoDetect.Value.construct(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE))
                .setIsIgnoredType(true)
                .setSetterInfo(JsonSetter.Value.construct(Nulls.SKIP, Nulls.SKIP));
    }


    public void dumpAll() {
        configurationMap.forEach(module::bindConfig);
    }

    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    public <T extends Configuration> void loadConfig(Class<T> config) {
        if (!config.isAnnotationPresent(Resource.class))
            throw new IllegalStateException("config " + config.getSimpleName() + " 缺少 @Resource 標註");
        var resource = config.getAnnotation(Resource.class);
        try {
            File f = new File(plugin.getDataFolder(), resource.locate());
            if (!f.exists()) plugin.saveResource(resource.locate(), true);
            var ins = mapper.readValue(f, config);
            class FileControllerImpl implements FileController {

                @Override
                public boolean reload() {
                    try {
                        if (reloadConfig(config)) {
                            var latest = mapper.readValue(f, config);
                            for (Field f : latest.getClass().getDeclaredFields()) {
                                var dataField = latest.getClass().getDeclaredField(f.getName());
                                dataField.setAccessible(true);
                                var data = dataField.get(latest);
                                f.setAccessible(true);
                                f.set(ins, data);
                            }
                            return true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }

                @Override
                public void save() throws IOException {
                    mapper.writeValue(f, ins);
                }
            }

            Field controller = Configuration.class.getDeclaredField("controller");
            setFinalField(controller, new FileControllerImpl(), ins);
            if (ins instanceof LangConfiguration){
                YamlConfiguration configuration = YamlConfiguration.loadConfiguration(f);
                class MessageGetterImpl implements LangController {

                    @Override
                    public String getPrefix() {
                        var prefix = ins.getClass().getAnnotation(Prefix.class);
                        return Optional.ofNullable(prefix).map(pre -> translate(configuration.getString(pre.path()))).orElseThrow(() -> new IllegalStateException("缺少 @Prefix 標註"));
                    }

                    @Override
                    public String get(String path) {
                        return getPrefix() + getPure(path);
                    }

                    @Override
                    public String getPure(String path) {
                        return translate(configuration.getString(path));
                    }

                    @Override
                    public List<String> getList(String path) {
                        return getPureList(path).stream().map(l -> getPrefix() + l).collect(Collectors.toList());
                    }

                    @Override
                    public List<String> getPureList(String path) {
                        return configuration.getStringList(path).stream().map(ELDConfigManager.this::translate).collect(Collectors.toList());
                    }
                }
                controller = LangConfiguration.class.getDeclaredField("lang");
                setFinalField(controller, new MessageGetterImpl(), ins);
            }

            this.configurationMap.putIfAbsent(config, ins);
        } catch (Exception e) {
            plugin.getLogger().warning("Error while loading yaml " + resource.locate());
            e.printStackTrace();
        }
    }

    @Override
    public <T extends Configuration> T getConfigAs(Class<T> config){
        if (injector != null){
            return injector.getInstance(config);
        }else{
            return Optional.ofNullable(this.configurationMap.get(config)).map(config::cast).orElseThrow(() -> new IllegalStateException("找不到 " + config.getSimpleName() + " 的映射物件，請確保你已經註冊了 " + config.getSimpleName()));
        }
    }


    private  <T extends Configuration> boolean reloadConfig(Class<T> config) {
        if (!configurationMap.containsKey(config)) {
            plugin.getLogger().log(Level.SEVERE, "找不到 " + config.getSimpleName() + " 的輸出文件路徑， 請確保你已經註冊了 " + config.getSimpleName());
            return false;
        }
        this.loadConfig(config);
        return true;
    }

    private String translate(String str) {
        if (str == null) return "null";
        return ChatColor.translateAlternateColorCodes('&', str);
    }


    private void setFinalField(Field field, Object newValue, Object ins) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(ins, newValue);
    }
}
