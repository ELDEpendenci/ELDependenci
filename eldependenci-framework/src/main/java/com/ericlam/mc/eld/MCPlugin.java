package com.ericlam.mc.eld;

import com.ericlam.mc.eld.controllers.LangController;

import java.io.File;
import java.util.logging.Logger;

public interface MCPlugin {

    Logger getLogger();

    File getDataFolder();

    String getName();

    void saveResource(String path, boolean replace);
}
