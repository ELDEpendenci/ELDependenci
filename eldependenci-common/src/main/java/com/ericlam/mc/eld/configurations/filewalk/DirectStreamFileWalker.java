package com.ericlam.mc.eld.configurations.filewalk;

import com.ericlam.mc.eld.configurations.PageRequest;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DirectStreamFileWalker implements FileWalker {

    @Override
    public Stream<Path> walkAll(File folder, @Nullable Predicate<Path> filter) throws IOException {
        List<Path> paths = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(folder.toPath(), "*.yml")) {
            for (Path path : ds) {
                if (filter != null && !filter.test(path)) continue;
                paths.add(path);
            }
        }
        return paths.stream();
    }

    @Override
    public Stream<Path> walkAll(File folder, PageRequest request) throws IOException {
        final int from = request.getPage() * request.getSize();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(folder.toPath(), "*.yml")) {
            var s = StreamSupport.stream(ds.spliterator(), false);
            if (request.getFilter() != null) s = s.filter(request.getFilter());
            return s.skip(from).limit(request.getSize());
        }
    }

    @Override
    public long totalSize(File folder, Predicate<Path> filter) throws IOException {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(folder.toPath(), "*.yml")) {
            return StreamSupport.stream(ds.spliterator(), false).filter(filter).count();
        }
    }
}
