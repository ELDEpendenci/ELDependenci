package com.ericlam.mc.eld;

import com.ericlam.mc.eld.configurations.ConfigStorage;
import com.ericlam.mc.eld.managers.ArgumentManager;

public interface ManagerProvider {

    ConfigStorage getConfigStorage();

    ArgumentManager getArgumentManager();

}
