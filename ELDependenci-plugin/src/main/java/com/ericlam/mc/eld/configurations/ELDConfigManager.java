package com.ericlam.mc.eld.configurations;

import com.ericlam.mc.eld.ELDModule;
import com.ericlam.mc.eld.annotations.GroupResource;
import com.ericlam.mc.eld.annotations.Prefix;
import com.ericlam.mc.eld.annotations.Resource;
import com.ericlam.mc.eld.components.Configuration;
import com.ericlam.mc.eld.components.GroupConfiguration;
import com.ericlam.mc.eld.components.GroupLangConfiguration;
import com.ericlam.mc.eld.components.LangConfiguration;
import com.ericlam.mc.eld.controllers.FileController;
import com.ericlam.mc.eld.controllers.LangController;
import com.ericlam.mc.eld.managers.ConfigStorage;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FilenameUtils;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.ericlam.mc.eld.configurations.ELDConfigManager.ConfigUtils.setField;

public final class ELDConfigManager implements ConfigStorage, ConfigInitializer {

    private final ELDModule module;
    private final JavaPlugin plugin;
    private final ObjectMapper mapper;
    private final Map<Class<? extends Configuration>, Configuration> configurationMap = new LinkedHashMap<>();


    private final ELDGroupConfigManager groupConfigManager;
    private final ELDLangConfigManager langConfigManager;


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
        Module bukkitModule = new SimpleModule()
                .setDeserializerModifier(new BukkitBeanModifier.Deserializer())
                .setSerializerModifier(new BukkitBeanModifier.Serializer());
        this.mapper.registerModule(bukkitModule);
        this.mapper.registerModule(new JavaTimeModule());
        this.skipType(FileController.class);
        this.skipType(LangController.class);
        if (module != null) this.module.bindInstance(ObjectMapper.class, this.mapper);
        this.plugin = plugin;

        this.groupConfigManager = new ELDGroupConfigManager(plugin, this);
        this.langConfigManager = new ELDLangConfigManager(plugin, this);
    }

    public Map<Class<? extends GroupConfiguration>, Map<String, GroupConfiguration>> getConfigPoolMap() {
        return ImmutableMap.copyOf(groupConfigManager.getConfigPoolMap());
    }

    public Map<Class<? extends GroupLangConfiguration>, GroupLangConfiguration> getDefaultLanguageMap() {
        return ImmutableMap.copyOf(langConfigManager.getDefaultLanguageMap());
    }

    public Map<Class<? extends GroupLangConfiguration>, Map<String, GroupLangConfiguration>> getLangPoolMap() {
        return ImmutableMap.copyOf(langConfigManager.getLangPoolMap());
    }

    private void skipType(Class<?> type) {
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

    public <T extends GroupConfiguration> void loadConfigPool(Class<T> config) {
        this.groupConfigManager.loadConfigPool(config);
    }

    public <T extends GroupLangConfiguration> void loadLanguagePool(Class<T> config) {
        this.langConfigManager.loadLanguagePool(config);
    }

    public <T extends GroupLangConfiguration> CompletableFuture<Optional<GroupLangConfiguration>> loadOneLangConfig(Class<T> config, String key) {
        return this.langConfigManager.loadOneLangConfig(config, key);
    }

    public <T extends GroupConfiguration> CompletableFuture<Optional<GroupConfiguration>> loadOneGroupConfig(Class<T> config, String key) {
        return this.groupConfigManager.loadOneGroupConfig(config, key);
    }

    @Nullable
    @Override
    public File[] loadGroupConfigs(GroupResource resource, String simpleName) {
        File f = new File(plugin.getDataFolder(), resource.folder());
        if (!f.exists()) f.mkdirs();
        if (!f.isDirectory())
            throw new IllegalStateException("config pool " + simpleName + " 's path ' " + resource.folder() + " is not a directory!");
        for (String preload : resource.preloads()) {
            String yml = preload.concat(".yml");
            File preLoadFile = new File(f, yml);
            if (!preLoadFile.exists()) plugin.saveResource(resource.folder().concat("/").concat(yml), true);
        }
        return f.listFiles(fi -> FilenameUtils.getExtension(fi.getName()).equals("yml"));
    }


    @Override
    public <T extends Configuration> T initConfiguration(Class<T> config, File f) throws Exception {
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
        setField(controller, new FileControllerImpl(), ins);

        return ins;
    }


    public <T extends Configuration> void loadConfig(Class<T> config) {
        if (!config.isAnnotationPresent(Resource.class))
            throw new IllegalStateException("config " + config.getSimpleName() + " is lack of @Resource annotation");
        var resource = config.getAnnotation(Resource.class);
        try {
            File f = new File(plugin.getDataFolder(), resource.locate());
            if (!f.exists()) plugin.saveResource(resource.locate(), true);
            var ins = initConfiguration(config, f);
            if (ins instanceof LangConfiguration) {
                YamlConfiguration configuration = YamlConfiguration.loadConfiguration(f);
                var controller = LangConfiguration.class.getDeclaredField("lang");
                setField(controller, new MessageGetterImpl(ins, configuration, f, plugin), ins);
            }

            this.configurationMap.putIfAbsent(config, ins);
        } catch (Exception e) {
            plugin.getLogger().warning("Error while loading yaml " + resource.locate());
            e.printStackTrace();
        }
    }


    @Override
    public <T extends Configuration> T getConfigAs(Class<T> config) {
        if (injector != null) {
            return injector.getInstance(config);
        } else {
            return Optional.ofNullable(this.configurationMap.get(config)).map(config::cast).orElseThrow(() -> new IllegalStateException("cannot find " + config.getSimpleName() + " config object, make sure you have registered " + config.getSimpleName()));
        }
    }


    private <T extends Configuration> boolean reloadConfig(Class<T> config) {
        if (!configurationMap.containsKey(config)) {
            plugin.getLogger().log(Level.SEVERE, "cannot find " + config.getSimpleName() + " in plugin folder, make sure you have registered " + config.getSimpleName());
            return false;
        }
        this.loadConfig(config);
        return true;
    }


    static class ConfigUtils {

        static void setFinalField(Field field, Object newValue, Object ins) throws Exception {
            field.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            field.set(ins, newValue);
        }

        static void setField(Field field, Object newValue, Object ins) throws Exception {
            field.setAccessible(true);
            field.set(ins, newValue);
        }

        static String translate(String str) {
            if (str == null) return "null";
            return ChatColor.translateAlternateColorCodes('&', str);
        }

    }

     static class MessageGetterImpl implements LangController {

        private final Object ins;
        private final FileConfiguration configuration;
        private final File data;
        private final Plugin plugin;

        MessageGetterImpl(Object ins, FileConfiguration configuration, File data, Plugin plugin) {
            this.ins = ins;
            this.configuration = configuration;
            this.data = data;
            this.plugin = plugin;
        }

        private String noPath(String path) {
            return ELDConfigManager.ConfigUtils.translate("&4message resource (" + data.getPath() + ") is lack of path: " + path);
        }

        @Override
        public String getPrefix() {
            var prefix = ins.getClass().getAnnotation(Prefix.class);
            return Optional.ofNullable(prefix).map(pre -> ELDConfigManager.ConfigUtils.translate(configuration.getString(pre.path()))).orElseGet(() -> {
                plugin.getLogger().warning("Plugin " + plugin.getName() + " is trying to get message with prefix but the prefix path is not defined.");
                return "";
            });
        }

        @Override
        public String get(String path) {
            return getPrefix() + getPure(path);
        }

        @Override
        public String getPure(String path) {
            if (!configuration.contains(path)) {
                return noPath(path);
            }
            return ELDConfigManager.ConfigUtils.translate(configuration.getString(path));
        }

        @Override
        public List<String> getList(String path) {
            return getPureList(path).stream().map(l -> getPrefix() + l).collect(Collectors.toList());
        }

        @Override
        public List<String> getPureList(String path) {
            if (!configuration.contains(path)) {
                return List.of(noPath(path));
            }
            return configuration.getStringList(path).stream().map(ELDConfigManager.ConfigUtils::translate).collect(Collectors.toList());
        }
    }


}
