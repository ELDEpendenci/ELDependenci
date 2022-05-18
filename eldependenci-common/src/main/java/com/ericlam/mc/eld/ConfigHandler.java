package com.ericlam.mc.eld;

import com.ericlam.mc.eld.configurations.MessageYaml;
import com.ericlam.mc.eld.controllers.LangController;

import java.io.File;

public interface ConfigHandler {

    MessageYaml loadYaml(File file);

}
