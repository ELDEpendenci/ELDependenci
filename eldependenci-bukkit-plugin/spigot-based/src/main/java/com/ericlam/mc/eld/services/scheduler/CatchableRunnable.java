package com.ericlam.mc.eld.services.scheduler;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Consumer;

public abstract class CatchableRunnable extends BukkitRunnable {

    protected Consumer<Throwable> catcher;

}
