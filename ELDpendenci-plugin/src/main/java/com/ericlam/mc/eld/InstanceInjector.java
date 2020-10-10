package com.ericlam.mc.eld;

import com.google.inject.Injector;
import org.bukkit.Bukkit;

public class InstanceInjector {

    private Injector injector;

    void setInjector(Injector injector) {
        this.injector = injector;
    }

    public void inject(Object instance){
        if (injector != null){
            injector.injectMembers(instance);
        }else{
            Bukkit.getLogger().warning("cannot inject "+instance.getClass()+" due to null injector");
        }
    }

}
