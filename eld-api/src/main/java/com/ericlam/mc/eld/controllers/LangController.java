package com.ericlam.mc.eld.controllers;

import java.util.List;

public interface LangController {

    String get(String node);

    String getPure(String node);

    List<String> getList(String node);

    List<String> getPureList(String node);

    String getPrefix();

}
