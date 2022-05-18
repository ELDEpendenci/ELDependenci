package com.ericlam.mc.eld.configurations;

import java.util.List;

public interface MessageYaml {

    boolean contains(String path);

    String getString(String path);

    List<String> getStringList(String path);

}
