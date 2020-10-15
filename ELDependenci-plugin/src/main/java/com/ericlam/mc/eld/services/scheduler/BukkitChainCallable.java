package com.ericlam.mc.eld.services.scheduler;

import com.ericlam.mc.eld.misc.ChainCallable;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Consumer;

public final class BukkitChainCallable<E, R> extends BukkitRunnable {

    private E accept;
    private final ChainCallable<E, R> function;
    private Consumer<R> handler;

    public BukkitChainCallable(ChainCallable<E, R> function) {
        this.function = function;
    }

    public void setHandler(Consumer<R> handler) {
        this.handler = handler;
    }

    public void setAccept(E accept) {
        this.accept = accept;
    }

    @Override
    public void run() {
        try {
            var result = function.call(accept);
            if (handler != null) handler.accept(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
