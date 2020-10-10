package com.ericlam.mc.eld;

import com.ericlam.mc.eld.managers.ConfigStorage;
import com.ericlam.mc.eld.managers.ArgumentManager;
import com.ericlam.mc.eld.managers.ItemInteractManager;

public interface ManagerProvider {

    ConfigStorage getConfigStorage();

    ArgumentManager getArgumentManager();

    ItemInteractManager getItemInteractManager();

}
