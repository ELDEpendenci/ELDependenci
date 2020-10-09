package com.ericlam.mc.eld.configurations;

import com.ericlam.mc.eld.components.Configuration;

public interface ConfigStorage {

    <T extends Configuration> T getConfigAs(Class<T> config);

}
