package com.ericlam.mc.eld.services;

import com.ericlam.mc.eld.misc.ChainCallable;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.Callable;

public interface ScheduleService {

    ScheduleFactory injectTask(BukkitRunnable runnable);

    <E> ChainScheduleService<E> callAsync(Callable<E> callable, Plugin plugin);


    interface ScheduleFactory {

        ScheduleFactory asynchronous(boolean async);

        ScheduleFactory interval(long ticks);

        ScheduleFactory timeout(long ticks);

        BukkitTask run(Plugin plugin);

    }

    interface ChainScheduleService<E> {

        <R> ChainScheduleService<R> thenRunSync(ChainCallable<E, R> function);

        <R> ChainScheduleService<R> thenRunAsync(ChainCallable<E, R> function);

        void join();
    }

}
