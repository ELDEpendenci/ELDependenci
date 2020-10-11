package com.ericlam.mc.eld;

import java.util.function.Consumer;

public interface ELDependenciAPI {

    ManagerProvider register(ELDBukkitPlugin plugin, Consumer<ServiceCollection> injector);

}
