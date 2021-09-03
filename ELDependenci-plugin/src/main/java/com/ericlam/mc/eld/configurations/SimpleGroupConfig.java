package com.ericlam.mc.eld.configurations;

import com.ericlam.mc.eld.ELDependenci;
import com.ericlam.mc.eld.components.GroupConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.Validate;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SimpleGroupConfig<T extends GroupConfiguration> implements GroupConfig<T>, PreLoadable {

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
                .map(this::mapToInstance)
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public Page<T> findAll(PageRequest pageRequest) {
        int page = pageRequest.getPage();
        int size = pageRequest.getSize();
        try {
            int totalPages = (int) Math.round((double)(totalSize() / size));
            List<T> list = Files.walk(folder.toPath())
                    .filter(p -> p.endsWith(".yml")) // adding '&& Files.isRegularFile(p)' will increase loading time
                    .sorted(pageRequest.getComparator())
                    .skip((long) page * size)
                    .limit(size)
                    .map(Path::toFile)
                    .map(this::mapToInstance)
                    .collect(Collectors.toList());
            return new YamlPage<>(list, pageRequest, totalPages, totalSize());
        } catch (IOException e) {
            LOGGER.warn("Error while loading Folder " + folder.toPath() + "" + e.getMessage(), e);
            e.printStackTrace();
        }
        // empty page if error
        return new YamlPage<>(List.of(), pageRequest, 0, 0);
    }

    @Nullable
    private T mapToInstance(File f) {
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
            LOGGER.warn("Error while loading " + id + ".yml: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public synchronized Optional<T> findById(String id) {
        if (cached.containsKey(id)) return Optional.of(cached.get(id));
        File file = new File(folder, id + ".yml");
        if (!file.exists()) return Optional.empty();
        try {
            T data = mapper.readValue(file, groupType);
            data.setId(id);
            cached.put(id, data);
            return Optional.of(data);
        } catch (IOException e) {
            LOGGER.warn("error while loading " + id + ".yml: " + e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public synchronized void save(T config) {
        validateIdExist(config);
        File yml = new File(folder, config.getId() + ".yml");
        try {
            mapper.writeValue(yml, config);
            this.cached.put(config.getId(), config);
        } catch (IOException e) {
            LOGGER.warn("error while saving " + config.getId() + ".yml: " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized boolean deleteById(String id) {
        File file = new File(folder, id + ".yml");
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

    private void validateIdExist(T data) {
        Validate.notNull(data.getId(), "id is null");
    }

    @Override
    public synchronized void loadAll() {
        File[] child = folder.listFiles(f -> f.getName().endsWith(".yml"));
        if (child == null) return;
        Arrays.stream(child)
                .parallel()
                .forEach(f -> {
                    String id = FilenameUtils.getBaseName(f.getName());
                    if (cached.containsKey(id)) {
                        return;
                    }
                    try {
                        T data = mapper.readValue(f, groupType);
                        data.setId(id);
                        cached.put(id, data);
                    } catch (IOException e) {
                        LOGGER.warn("Error while loading " + id + ".yml: " + e.getMessage(), e);
                    }
                });
    }

    private static class YamlPage<T> implements Page<T> {

        private final List<T> content;
        private final PageRequest pageRequest;
        private final long totalSize;
        private final int totalPages;
        private final boolean hasNext;

        private YamlPage(
                List<T> content,
                PageRequest pageRequest,
                int totalPages,
                long totalSize
        ) {
            this.content = content;
            this.pageRequest = pageRequest;
            this.totalPages = totalPages;
            this.totalSize = totalSize;
            this.hasNext = this.pageRequest.getPage() < this.totalPages;
        }


        @Override
        public List<T> getContent() {
            return content;
        }

        @Override
        public boolean hasContent() {
            return !content.isEmpty();
        }

        @Override
        public int getCurrentPage() {
            return pageRequest.getPage();
        }

        @Override
        public int getTotalPages() {
            return totalPages;
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public boolean hasPrevious() {
            return getCurrentPage() > 0;
        }

        @Override
        public long getTotalElements() {
            return totalSize;
        }

        @Override
        public PageRequest getPageRequest() {
            return pageRequest;
        }

        @Override
        public <U> Page<U> map(Function<T, U> converter) {
            var newList = content.stream().map(converter).collect(Collectors.toList());
            return new YamlPage<>(newList, pageRequest, totalPages, totalSize);
        }
    }
}
