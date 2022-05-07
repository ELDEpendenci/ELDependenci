package com.ericlam.mc.eld.services;


import com.ericlam.mc.eld.misc.DebugLogger;
import org.bukkit.plugin.Plugin;

public interface LoggingService {

    DebugLogger getLogger(Class<?> cls);

    DebugLogger getLogger(String name);


}
