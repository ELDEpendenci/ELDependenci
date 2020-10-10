package com.ericlam.mc.eld.managers;

import com.ericlam.mc.eld.components.Configuration;

public interface ConfigStorage {

    <T extends Configuration> T getConfigAs(Class<T> config);

}
