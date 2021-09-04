package com.ericlam.mc.eld.configurations.filewalk;

import com.ericlam.mc.eld.configurations.PageRequest;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface FileWalker {

    Stream<Path> walkAll(File folder, @Nullable Predicate<Path> filter) throws IOException;

    Stream<Path> walkAll(File folder, PageRequest request) throws IOException;

    long totalSize(File folder, Predicate<Path> filter) throws IOException;

}
