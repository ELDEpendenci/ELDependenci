package com.ericlam.mc.eld.configurations.filewalk;

import com.ericlam.mc.eld.configurations.PageRequest;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SimpleFileWalker implements FileWalker {

    @Override
    public Stream<Path> walkAll(File folder, @Nullable Predicate<Path> filter) throws IOException {
        var s = Files.walk(folder.toPath()).filter(p -> p.toString().endsWith(".yml"));
        if (filter != null) s = s.filter(filter);
        return s;
    }

    @Override
    public Stream<Path> walkAll(File folder, PageRequest request) throws IOException {
        int page = request.getPage();
        int size = request.getSize();
        final int from = page * size;
        var s = Files.walk(folder.toPath())
                .filter(p -> p.toString().endsWith(".yml"));
        if (request.getFilter() != null) s = s.filter(request.getFilter());
        return s.skip(from)
                .limit(size);
    }

    @Override
    public long totalSize(File folder, Predicate<Path> filter) throws IOException {
        return Files.walk(folder.toPath())
                .filter(p -> p.toString().endsWith(".yml"))
                .filter(filter)
                .count();
    }
}
