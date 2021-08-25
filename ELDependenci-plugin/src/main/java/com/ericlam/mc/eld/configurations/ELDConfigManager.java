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
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import io.netty.util.internal.ConcurrentSet;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FilenameUtils;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class ELDConfigManager implements ConfigStorage {

    private final ELDModule module;
    private final JavaPlugin plugin;
    private final ObjectMapper mapper;
    private final Map<Class<? extends Configuration>, Configuration> configurationMap = new LinkedHashMap<>();

    private final Map<Class<? extends GroupConfiguration>, Map<String, GroupConfiguration>> configPoolMap = new ConcurrentHashMap<>();
    private final Map<Class<? extends GroupLangConfiguration>, Map<String, GroupLangConfiguration>> langPoolMap = new ConcurrentHashMap<>();

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
    }

    public Map<Class<? extends GroupConfiguration>, Map<String, GroupConfiguration>> getConfigPoolMap() {
        return ImmutableMap.copyOf(configPoolMap);
    }


    public Map<Class<? extends GroupLangConfiguration>, Map<String, GroupLangConfiguration>> getLangPoolMap() {
        return ImmutableMap.copyOf(langPoolMap);
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

    public <T extends Configuration> void loadConfig(Class<T> config) {
        if (!config.isAnnotationPresent(Resource.class))
            throw new IllegalStateException("config " + config.getSimpleName() + " is lack of @Resource annotation");
        var resource = config.getAnnotation(Resource.class);
        try {
            File f = new File(plugin.getDataFolder(), resource.locate());
            if (!f.exists()) plugin.saveResource(resource.locate(), true);
            var ins = initConfiguration(config, f);
            if (ins instanceof LangConfiguration){
                YamlConfiguration configuration = YamlConfiguration.loadConfiguration(f);
                var controller = LangConfiguration.class.getDeclaredField("lang");
                setField(controller, new MessageGetterImpl(ins, configuration, f), ins);
            }

            this.configurationMap.putIfAbsent(config, ins);
        } catch (Exception e) {
            plugin.getLogger().warning("Error while loading yaml " + resource.locate());
            e.printStackTrace();
        }
    }

    // === group config ===
    public <T extends GroupConfiguration> void loadConfigPool(Class<T> config){
        configPoolMap.putIfAbsent(config, new ConcurrentHashMap<>());
        preloadConfigPool(config).whenComplete((p, ex) ->{
            if (ex != null) ex.printStackTrace();
            else configPoolMap.put(config, p);
        });
    }

    public <T extends GroupConfiguration> CompletableFuture<Map<String, GroupConfiguration>> preloadConfigPool(Class<T> config){
        if (!config.isAnnotationPresent(GroupResource.class))
            throw new IllegalStateException("config pool " + config.getSimpleName() + " is lack of @GroupResource annotation");
        var resource = config.getAnnotation(GroupResource.class);
        return CompletableFuture.supplyAsync(() -> {
            var pool = new HashMap<String, GroupConfiguration>();
            try {
                File[] child = loadGroupConfigs(resource, config.getSimpleName());
                if (child == null){
                    return pool;
                }
                for (File data : child) {
                    var id = FilenameUtils.getBaseName(data.getName());
                    var ins = initConfiguration(config, data);
                    var idField = GroupConfiguration.class.getDeclaredField("id");
                    setField(idField, id, ins);
                    pool.put(id, ins);
                }
                return pool;
            } catch (Exception e) {
                plugin.getLogger().warning("Error while loading config pool " + resource.folder());
                e.printStackTrace();
            }
            return pool;
        }).thenApply(p -> {
            plugin.getLogger().info("All resources in folder "+resource.folder()+" has been loaded.");
            return p;
        });
    }

    public <T extends GroupConfiguration> CompletableFuture<T> loadOneGroupConfig(Class<T> config, String key){
        if (!config.isAnnotationPresent(GroupResource.class))
            throw new IllegalStateException("config pool " + config.getSimpleName() + " is lack of @GroupResource annotation");
        var resource = config.getAnnotation(GroupResource.class);
        return CompletableFuture.supplyAsync(() -> {
            File[] childs = Optional.ofNullable(loadGroupConfigs(resource, config.getSimpleName())).orElse(new File[0]);
            Optional<File> child = Arrays.stream(childs).filter(data -> FilenameUtils.getBaseName(data.getName()).equals(key)).findAny();
            if (child.isEmpty()) throw new IllegalStateException("unknown config: "+key+".yml");
            try {
                var data = child.get();
                var ins = initConfiguration(config, data);
                var idField = GroupConfiguration.class.getDeclaredField("id");
                setField(idField, key, ins);
                return ins;
            }catch (Exception e){
                throw new CompletionException(e);
            }
        });
    }


    // ====

    // === lang group config ===

    public <T extends GroupLangConfiguration> void loadLanguagePool(Class<T> config) {
        langPoolMap.putIfAbsent(config, new ConcurrentHashMap<>());
        preloadLanguagePool(config).whenComplete((p, ex) ->{
            if (ex != null) ex.printStackTrace();
            else langPoolMap.put(config, p);
        });
    }

    public <T extends GroupLangConfiguration> CompletableFuture<Map<String, GroupLangConfiguration>> preloadLanguagePool(Class<T> config){
        if (!config.isAnnotationPresent(GroupResource.class))
            throw new IllegalStateException("config pool " + config.getSimpleName() + " is lack of @GroupResource annotation");
        var resource = config.getAnnotation(GroupResource.class);
        return CompletableFuture.supplyAsync(() -> {
            Map<String, GroupLangConfiguration> groupMap = new LinkedHashMap<>();
            File[] child = loadGroupConfigs(resource, config.getSimpleName());
            if (child == null) return groupMap;
            try {
                for (File data : child) {
                    var id = FilenameUtils.getBaseName(data.getName());
                    var ins = initConfiguration(config, data);
                    YamlConfiguration configuration = YamlConfiguration.loadConfiguration(data);
                    var controller = LangConfiguration.class.getDeclaredField("lang");
                    var locale = GroupLangConfiguration.class.getDeclaredField("locale");
                    setField(controller, new MessageGetterImpl(ins, configuration, data), ins);
                    setField(locale, id, ins);
                    groupMap.put(id, ins);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error while loading config pool " + resource.folder());
                e.printStackTrace();
            }
            return groupMap;
        }).thenApply(p -> {
            plugin.getLogger().info("All language resources in folder "+resource.folder()+" has been loaded.");
            return p;
        });
    }

    public <T extends GroupLangConfiguration> CompletableFuture<T> loadOneLangConfig(Class<T> config, String key){
        if (!config.isAnnotationPresent(GroupResource.class))
            throw new IllegalStateException("config pool " + config.getSimpleName() + " is lack of @GroupResource annotation");
        var resource = config.getAnnotation(GroupResource.class);
        return CompletableFuture.supplyAsync(() -> {
            File[] childs = Optional.ofNullable(loadGroupConfigs(resource, config.getSimpleName())).orElse(new File[0]);
            Optional<File> child = Arrays.stream(childs).filter(data -> FilenameUtils.getBaseName(data.getName()).equals(key)).findAny();
            if (child.isEmpty()) throw new IllegalStateException("unknown config: "+key+".yml");
            try {
                var data = child.get();
                var ins = initConfiguration(config, data);
                YamlConfiguration configuration = YamlConfiguration.loadConfiguration(data);
                var controller = LangConfiguration.class.getDeclaredField("lang");
                var locale = GroupLangConfiguration.class.getDeclaredField("locale");
                setField(controller, new MessageGetterImpl(ins, configuration, data), ins);
                setField(locale, key, ins);
                return ins;
            }catch (Exception e){
                throw new CompletionException(e);
            }
        });
    }

    // =====

    @Nullable
    private File[] loadGroupConfigs(GroupResource resource, String simpleName) {
        File f = new File(plugin.getDataFolder(), resource.folder());
        if (!f.exists()) f.mkdirs();
        if (!f.isDirectory()) throw new IllegalStateException("config pool "+ simpleName +" 's path ' "+resource.folder()+" is not a directory!");
        for (String preload : resource.preloads()) {
            String yml = preload.concat(".yml");
            File preLoadFile = new File(f, yml);
            if (!preLoadFile.exists()) plugin.saveResource(resource.folder().concat("/").concat(yml), true);
        }
        return f.listFiles(fi -> FilenameUtils.getExtension(fi.getName()).equals("yml"));
    }







    private <T extends Configuration> T initConfiguration(Class<T> config, File f) throws Exception{
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

    private class MessageGetterImpl implements LangController {

        private final Object ins;
        private final FileConfiguration configuration;
        private final File data;

        private MessageGetterImpl(Object ins, FileConfiguration configuration, File data) {
            this.ins = ins;
            this.configuration = configuration;
            this.data = data;
        }

        private String noPath(String path){
            return translate("&4message resource ("+data.getPath()+") is lack of path: "+path);
        }

        @Override
        public String getPrefix() {
            var prefix = ins.getClass().getAnnotation(Prefix.class);
            return Optional.ofNullable(prefix).map(pre -> translate(configuration.getString(pre.path()))).orElseGet(() -> {
                plugin.getLogger().warning("Plugin "+plugin.getName()+" is trying to get message with prefix but the prefix path is not defined.");
                return "";
            });
        }

        @Override
        public String get(String path) {
            return getPrefix() + getPure(path);
        }

        @Override
        public String getPure(String path) {
            if (!configuration.contains(path)){
                return noPath(path);
            }
            return translate(configuration.getString(path));
        }

        @Override
        public List<String> getList(String path) {
            return getPureList(path).stream().map(l -> getPrefix() + l).collect(Collectors.toList());
        }

        @Override
        public List<String> getPureList(String path) {
            if (!configuration.contains(path)){
                return List.of(noPath(path));
            }
            return configuration.getStringList(path).stream().map(ELDConfigManager.this::translate).collect(Collectors.toList());
        }
    }

    @Override
    public <T extends Configuration> T getConfigAs(Class<T> config){
        if (injector != null){
            return injector.getInstance(config);
        }else{
            return Optional.ofNullable(this.configurationMap.get(config)).map(config::cast).orElseThrow(() -> new IllegalStateException("cannot find " + config.getSimpleName() + " config object, make sure you have registered " + config.getSimpleName()));
        }
    }


    private  <T extends Configuration> boolean reloadConfig(Class<T> config) {
        if (!configurationMap.containsKey(config)) {
            plugin.getLogger().log(Level.SEVERE, "cannot find " + config.getSimpleName() + " in plugin folder, make sure you have registered " + config.getSimpleName());
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

    private void setField(Field field, Object newValue, Object ins) throws Exception {
        field.setAccessible(true);
        field.set(ins, newValue);
    }
}
