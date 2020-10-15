package com.ericlam.mc.eld.services.scheduler;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public final class BukkitCallable<E> extends BukkitRunnable {

    private final Callable<E> callable;
    private Consumer<E> handler;

    public BukkitCallable(Callable<E> callable) {
        this.callable = callable;
    }

    public void setHandler(Consumer<E> handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            var result = callable.call();
            if (handler != null) handler.accept(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
