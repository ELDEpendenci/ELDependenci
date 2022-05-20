package com.ericlam.mc.eld;

import com.ericlam.mc.eld.configurations.MessageYaml;

import java.io.File;

public interface ConfigHandler {

    MessageYaml loadYaml(File file) throws Exception;

}
