package com.ericlam.mc.eld.configurations;

import com.ericlam.mc.eld.ConfigHandler;
import com.ericlam.mc.eld.ELDCommonModule;
import com.ericlam.mc.eld.MCPlugin;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Injector;
import net.md_5.bungee.api.ChatColor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.ericlam.mc.eld.configurations.ELDConfigManager.ConfigUtils.setField;

public final class ELDConfigManager implements ConfigStorage {

    public static final ObjectMapper YAML_MAPPER = new ObjectMapper(
            new YAMLFactory()
                    .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                    .enable(JsonParser.Feature.ALLOW_YAML_COMMENTS)
    );

    public static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    static {

        setup(YAML_MAPPER);
        setup(JSON_MAPPER);

        skipType(FileController.class);
        skipType(LangController.class);
    }

    private static void setup(ObjectMapper mapper) {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(JsonParser.Feature.ALLOW_COMMENTS)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .registerModule(new JavaTimeModule());
    }

    private static void skipType(Class<?> type) {
        YAML_MAPPER.configOverride(type)
                .setVisibility(JsonAutoDetect.Value.construct(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE))
                .setIsIgnoredType(true)
                .setSetterInfo(JsonSetter.Value.construct(Nulls.SKIP, Nulls.SKIP));
    }


    private final ELDCommonModule module;
    private final MCPlugin plugin;
    private final ConfigHandler configHandler;

    private final Map<Class<? extends Configuration>, Configuration> configurationMap = new LinkedHashMap<>();


    private Injector injector = null;

    public ELDConfigManager(ELDCommonModule module, MCPlugin plugin, ConfigHandler configHandler) {
        this.module = module;
        this.plugin = plugin;
        this.configHandler = configHandler;
    }


    public void dumpAll() {
        configurationMap.forEach(module::bindConfig);
    }

    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    // config pool

    private final Set<Class<?>> groupConfigSet = new HashSet<>();

    public <T extends GroupConfiguration> void loadConfigPool(Class<T> config) {
        preloadYaml(config);
        groupConfigSet.add(config);
    }

    public <T extends GroupLangConfiguration> void loadLanguagePool(Class<T> config) {
        preloadYaml(config);
        groupConfigSet.add(config);
    }

    private void preloadYaml(Class<?> config) {
        CompletableFuture.runAsync(() -> {
            if (!config.isAnnotationPresent(GroupResource.class))
                throw new IllegalStateException(config.getSimpleName() + " is lack of @GroupResource annotation");
            var resource = config.getAnnotation(GroupResource.class);
            var folder = new File(plugin.getDataFolder(), resource.folder());
            if (!folder.exists() && folder.mkdirs())
                plugin.getLogger().info("Folder " + resource.folder() + " created.");
            if (!folder.isDirectory())
                throw new IllegalStateException("config pool " + config.getSimpleName() + " 's path ' " + resource.folder() + " is not a directory!");
            for (String preload : resource.preloads()) {
                String yml = preload.concat(".yml");
                File preLoadFile = new File(folder, yml);
                if (!preLoadFile.exists()) plugin.saveResource(resource.folder().concat("/").concat(yml));
            }
        }).whenComplete((v, ex) -> {
            if (ex != null) ex.printStackTrace();
        });
    }

    public Set<Class<?>> getGroupConfigSet() {
        return groupConfigSet;
    }

    // =========

    public <T extends Configuration> T initConfiguration(Class<T> config, File f) throws Exception {
        var ins = YAML_MAPPER.readValue(f, config);
        return this.initConfiguration(config, f, ins);
    }

    public <T extends Configuration> T initConfiguration(Class<T> config, File f, T ins) throws Exception {
        class FileControllerImpl implements FileController, FileLocator {

            private final Field[] fields;

            public FileControllerImpl() {
                this.fields = config.getFields();
            }

            @Override
            public boolean reload() {
                try {
                    if (reloadConfig(config, ins)) {
                        var latest = YAML_MAPPER.readValue(f, config);
                        for (Field f : fields) {
                            var data = f.get(latest);
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
                YAML_MAPPER.writeValue(f, ins);
            }

            @Override
            public File getLocator() {
                return f;
            }
        }

        Field controller = Configuration.class.getDeclaredField("controller");
        setField(controller, new FileControllerImpl(), ins);

        return ins;
    }

    public <T extends Configuration> void loadConfig(Class<T> config, T o) {
        if (!config.isAnnotationPresent(Resource.class))
            throw new IllegalStateException("config " + config.getSimpleName() + " is lack of @Resource annotation");
        var resource = config.getAnnotation(Resource.class);
        try {
            File f = new File(plugin.getDataFolder(), resource.locate());
            if (!f.exists()) plugin.saveResource(resource.locate());
            var ins = initConfiguration(config, f, o);
            if (ins instanceof LangConfiguration) {
                var messageYaml = configHandler.loadYaml(f);
                var controller = LangConfiguration.class.getDeclaredField("lang");
                setField(controller, new MessageGetterImpl(ins, messageYaml, f, plugin), ins);
            }

            this.configurationMap.putIfAbsent(config, ins);
        } catch (Exception e) {
            plugin.getLogger().warning("Error while loading yaml " + resource.locate());
            e.printStackTrace();
        }
    }

    public <T extends Configuration> void loadConfig(Class<T> config) {
        if (!config.isAnnotationPresent(Resource.class))
            throw new IllegalStateException("config " + config.getSimpleName() + " is lack of @Resource annotation");
        var resource = config.getAnnotation(Resource.class);
        try {
            File f = new File(plugin.getDataFolder(), resource.locate());
            if (!f.exists()) plugin.saveResource(resource.locate());
            var ins = initConfiguration(config, f);
            if (ins instanceof LangConfiguration) {
                var messageYaml = configHandler.loadYaml(f);
                var controller = LangConfiguration.class.getDeclaredField("lang");
                setField(controller, new MessageGetterImpl(ins, messageYaml, f, plugin), ins);
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


    private <T extends Configuration> boolean reloadConfig(Class<T> config, T ins) {
        if (!configurationMap.containsKey(config)) {
            plugin.getLogger().log(Level.SEVERE, "cannot find " + config.getSimpleName() + " in plugin folder, make sure you have registered " + config.getSimpleName());
            return false;
        }
        this.loadConfig(config, ins);
        return true;
    }


    static class MessageGetterImpl implements LangController {

        private final Object ins;
        private final MessageYaml configuration;
        private final File data;
        private final MCPlugin plugin;

        MessageGetterImpl(Object ins, MessageYaml configuration, File data, MCPlugin plugin) {
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
        public String get(String node, Object... args) {
            return MessageFormat.format(get(node), args);
        }

        @Override
        public String getF(String node, Object... args) {
            return String.format(get(node), args);
        }

        @Override
        public String getPure(String path) {
            if (!configuration.contains(path)) {
                return noPath(path);
            }
            return ELDConfigManager.ConfigUtils.translate(configuration.getString(path));
        }

        @Override
        public String getPure(String node, Object... args) {
            return MessageFormat.format(getPure(node), args);
        }

        @Override
        public String getPureF(String node, Object... args) {
            return String.format(getPure(node), args);
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
}
