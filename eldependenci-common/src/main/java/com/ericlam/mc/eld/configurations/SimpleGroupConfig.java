package com.ericlam.mc.eld.configurations;

import com.ericlam.mc.eld.components.GroupConfiguration;
import com.ericlam.mc.eld.configurations.filewalk.FileWalker;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SimpleGroupConfig<T extends GroupConfiguration> implements GroupConfig<T>, PreLoadable {

    private final static Logger LOGGER = LoggerFactory.getLogger(GroupConfig.class);

    private final ObjectMapper mapper;
    private final File folder;
    private final Class<T> groupType;
    private final FileWalker fileWalker;

    private final Map<String, T> cached = new ConcurrentHashMap<>();

    public SimpleGroupConfig(ObjectMapper mapper,
                             File folder,
                             Class<T> groupType,
                             FileWalker fileWalker) {
        this.mapper = mapper;
        this.folder = folder;
        this.groupType = groupType;
        this.fileWalker = fileWalker;
    }

    @Override
    public synchronized List<T> findAll() {
        try {
            return fileWalker.walkAll(folder, (Predicate<Path>) null).map(Path::toFile).map(this::mapToInstance).collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.warn("Error while loading Folder " + folder.toPath() + "" + e.getMessage(), e);
            e.printStackTrace();
        }
        return List.of();
    }

    @Override
    public synchronized List<T> findAll(Predicate<Path> filter) {
        try {
            return fileWalker.walkAll(folder, filter).map(Path::toFile).map(this::mapToInstance).collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.warn("Error while loading Folder " + folder.toPath() + "" + e.getMessage(), e);
            e.printStackTrace();
        }
        return List.of();
    }

    @Override
    public synchronized Page<T> findAll(PageRequest pageRequest) {
        try {
            List<T> list = fileWalker.walkAll(folder, pageRequest).map(Path::toFile).map(this::mapToInstance).collect(Collectors.toList());
            long totalSize = pageRequest.getFilter() == null ? totalSize() : totalSize(pageRequest.getFilter());
            return new YamlPage<>(list, pageRequest, totalSize);
        } catch (IOException e) {
            LOGGER.warn("Error while loading Folder " + folder.toPath() + "" + e.getMessage(), e);
            e.printStackTrace();
        }
        // empty page if error
        return new YamlPage<>(List.of(), pageRequest, 0);
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
        return Optional.ofNullable(folder.list()).map(l -> l.length).orElse(0);
    }

    private long totalSize(Predicate<Path> filter) throws IOException {
        return fileWalker.totalSize(folder, filter);
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
        if (data.getId() == null) throw new IllegalStateException("id cannot be null");
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
                long totalSize
        ) {
            this.content = content;
            this.pageRequest = pageRequest;
            this.totalSize = totalSize;
            this.totalPages = (int) Math.ceil((double) totalSize / pageRequest.getSize());
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
            return new YamlPage<>(newList, pageRequest, totalSize);
        }
    }
}
