package com.ericlam.mc.eld.configurations.filewalk;

import com.ericlam.mc.eld.configurations.PageRequest;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TreeFileWalker implements FileWalker {

    @Override
    public Stream<Path> walkAll(File folder, @Nullable Predicate<Path> filter) throws IOException {
        List<Path> paths = new ArrayList<>();
        Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (dir.equals(folder.toPath())) {
                    return FileVisitResult.CONTINUE;
                }
                return FileVisitResult.SKIP_SUBTREE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".yml") && (filter == null || filter.test(file))) {
                    paths.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return paths.stream();
    }

    @Override
    public Stream<Path> walkAll(File folder, PageRequest request) throws IOException {
        final int from = request.getPage() * request.getSize();
        final int to = from + request.getSize();
        final Predicate<Path> filter = request.getFilter();
        List<Path> paths = new ArrayList<>();
        Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<>() {
            private int index = 0;

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (dir.equals(folder.toPath())) return FileVisitResult.CONTINUE;
                return FileVisitResult.SKIP_SUBTREE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!file.toString().endsWith(".yml")) return FileVisitResult.CONTINUE;
                if (filter != null && !filter.test(file)) return FileVisitResult.CONTINUE;
                if (index >= from) {
                    paths.add(file);
                }
                index++;
                if (index >= to) return FileVisitResult.TERMINATE;
                return super.visitFile(file, attrs);
            }
        });
        return paths.stream();
    }
}
