package com.ericlam.mc.eld.configurations;

import com.ericlam.mc.eld.ELDependenci;
import com.ericlam.mc.eld.components.GroupConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.Validate;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SimpleGroupConfig<T extends GroupConfiguration> implements GroupConfig<T> {

    private final static Logger LOGGER = ELDependenci.getProvidingPlugin(ELDependenci.class).getSLF4JLogger();

    private final ObjectMapper mapper;
    private final File folder;
    private final Class<T> groupType;

    private final Map<String, T> cached = new ConcurrentHashMap<>();

    public SimpleGroupConfig(ObjectMapper mapper, File folder, Class<T> groupType) {
        this.mapper = mapper;
        this.folder = folder;
        this.groupType = groupType;
    }

    @Override
    public synchronized List<T> findAll() {
        File[] child = folder.listFiles(f -> f.getName().endsWith(".yml"));
        if (child == null) return List.of();
        return Arrays.stream(child)
                .parallel()
                .map(f -> {
                    String id = FilenameUtils.getBaseName(f.getName());
                    if (cached.containsKey(id)) {
                        return cached.get(id);
                    }
                    try {
                        T data = mapper.readValue(f, groupType);
                        data.setId(id);
                        cached.put(id, data);
                        return data;
                    } catch (IOException e) {
                        LOGGER.warn("Error while loading "+id+".yml: "+e.getMessage(), e);
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public synchronized Optional<T> findById(String id) {
        if (cached.containsKey(id)) return Optional.of(cached.get(id));
        File file = new File(folder, id+".yml");
        if (!file.exists()) return Optional.empty();
        try {
            T data = mapper.readValue(file, groupType);
            data.setId(id);
            cached.put(id, data);
            return Optional.of(data);
        }catch (IOException e){
            LOGGER.warn("error while loading "+id+".yml: "+e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public synchronized void save(T config) {
        validateIdExist(config);
        File yml = new File(folder, config.getId()+".yml");
        try {
            mapper.writeValue(yml, config);
            this.cached.put(config.getId(), config);
        } catch (IOException e) {
            LOGGER.warn("error while saving "+config.getId()+".yml: "+e.getMessage(), e);
        }
    }

    @Override
    public synchronized boolean deleteById(String id) {
        File file = new File(folder, id+".yml");
        if (!file.exists()) {
            return false;
        }
        cached.remove(id);
        return file.delete();
    }

    @Override
    public synchronized long totalSize() {
        return folder.length();
    }

    @Override
    public synchronized boolean delete(T config) {
        return this.deleteById(config.getId());
    }

    @Override
    public synchronized void fetch() {
        this.cached.clear();
    }

    @Override
    public synchronized void fetchById(String id) {
        this.cached.remove(id);
    }

    private void validateIdExist(T data){
        Validate.notNull(data.getId(), "id is null");
    }
}
