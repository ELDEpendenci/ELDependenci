package com.ericlam.mc.eld.configurations;

import com.ericlam.mc.eld.annotations.GroupResource;
import com.ericlam.mc.eld.components.Configuration;

import javax.annotation.Nullable;
import java.io.File;

interface ConfigInitializer {

    <T extends Configuration> T initConfiguration(Class<T> config, File f) throws Exception;

    @Nullable
    File[] loadGroupConfigs(GroupResource resource, String simpleName);

}