package com.ericlam.mc.eld.services.scheduler;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public final class BukkitCallable<E> extends CatchableRunnable {

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
            if (catcher != null) catcher.accept(e);
            else e.printStackTrace();
        }
    }
}
