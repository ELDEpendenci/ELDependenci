package com.ericlam.mc.eld.services.scheduler;

import com.ericlam.mc.eld.InstanceInjector;
import com.ericlam.mc.eld.misc.ChainCallable;
import com.ericlam.mc.eld.services.ScheduleService;
import com.google.inject.Inject;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.Callable;

public final class ELDSchedulerService implements ScheduleService {

    @Inject
    private InstanceInjector injector;


    @Override
    public ScheduleFactory injectTask(BukkitRunnable runnable) {
        return new ELDSchedulerFactory(runnable);
    }

    @Override
    public <E> ChainScheduleService<E> callAsync(Callable<E> callable, Plugin plugin) {
        injector.inject(callable);
        return new ELDChainScheduleService<>(callable, plugin);
    }


    private class ELDSchedulerFactory implements ScheduleFactory {

        private final BukkitRunnable bukkitRunnable;
        private boolean async = false;
        private long interval = -1;
        private long timeout = -1;

        private ELDSchedulerFactory(BukkitRunnable bukkitRunnable) {
            injector.inject(bukkitRunnable);
            this.bukkitRunnable = bukkitRunnable;
        }

        @Override
        public ScheduleFactory asynchronous(boolean async) {
            this.async = async;
            return this;
        }

        @Override
        public ScheduleFactory interval(long ticks) {
            this.interval = ticks;
            return this;
        }

        @Override
        public ScheduleFactory timeout(long ticks) {
            this.timeout = ticks;
            return this;
        }

        @Override
        public void run(Plugin plugin) {
            if (timeout == -1 && interval == -1) {
                if (async) {
                    bukkitRunnable.runTaskAsynchronously(plugin);
                } else {
                    bukkitRunnable.runTask(plugin);
                }
            } else if (timeout != -1 && interval == -1) {
                if (async) {
                    bukkitRunnable.runTaskLaterAsynchronously(plugin, timeout);
                } else {
                    bukkitRunnable.runTaskLater(plugin, timeout);
                }
            } else if (timeout != -1) {
                if (async) {
                    bukkitRunnable.runTaskTimerAsynchronously(plugin, timeout, interval);
                } else {
                    bukkitRunnable.runTaskTimer(plugin, timeout, interval);
                }
            }
        }
    }


    private class ELDChainScheduleService<E> implements ChainScheduleService<E> {

        private final Plugin plugin;
        private final BukkitCallable<E> bukkitCallable;

        private ELDChainScheduleService(Callable<E> callable, Plugin plugin) {
            injector.inject(callable);
            bukkitCallable = new BukkitCallable<>(callable);
            this.plugin = plugin;
        }

        @Override
        public <R> ChainScheduleService<R> thenRunSync(ChainCallable<E, R> function) {
            var s = new ELDChainScheduleService2<>(bukkitCallable, function, plugin, false);
            bukkitCallable.setHandler(e -> {
                s.setElement(e);
                s.start();
            });
            return s;
        }

        @Override
        public <R> ChainScheduleService<R> thenRunAsync(ChainCallable<E, R> function) {
            var s = new ELDChainScheduleService2<>(bukkitCallable, function, plugin, true);
            bukkitCallable.setHandler(e -> {
                s.setElement(e);
                s.start();
            });
            return s;
        }

        @Override
        public void join() {
            bukkitCallable.runTaskAsynchronously(plugin);
        }
    }


    private class ELDChainScheduleService2<E, R> implements ChainScheduleService<R> {

        public final Plugin plugin;
        private final boolean async;
        private final BukkitRunnable firstCallable;
        private final BukkitChainCallable<E, R> bukkitChainCallable;


        public ELDChainScheduleService2(final BukkitRunnable firstCallable, ChainCallable<E, R> function, Plugin plugin, boolean async) {
            injector.inject(function);
            bukkitChainCallable = new BukkitChainCallable<>(function);
            this.firstCallable = firstCallable;
            this.plugin = plugin;
            this.async = async;
        }

        public void setElement(E element) {
            bukkitChainCallable.setAccept(element);
        }


        @Override
        public <J> ChainScheduleService<J> thenRunSync(ChainCallable<R, J> function) {
            var s = new ELDChainScheduleService2<>(firstCallable, function, plugin, false);
            bukkitChainCallable.setHandler(e -> {
                s.setElement(e);
                s.start();
            });
            return s;
        }

        @Override
        public <J> ChainScheduleService<J> thenRunAsync(ChainCallable<R, J> function) {
            var s = new ELDChainScheduleService2<>(firstCallable, function, plugin, true);
            bukkitChainCallable.setHandler(e -> {
                s.setElement(e);
                s.start();
            });
            return s;
        }

        private void start() {
            if (async) {
                bukkitChainCallable.runTaskAsynchronously(plugin);
            } else {
                bukkitChainCallable.runTask(plugin);
            }
        }

        @Override
        public void join() {
            firstCallable.runTaskAsynchronously(plugin);
        }
    }


}
