package com.ericlam.mc.eld.services.scheduler;

import com.ericlam.mc.eld.InstanceInjector;
import com.ericlam.mc.eld.misc.ChainCallable;
import com.ericlam.mc.eld.services.ScheduleService;
import com.google.inject.Inject;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public final class ELDSchedulerService implements ScheduleService {

    @Inject
    private InstanceInjector injector;


    @Override
    public ScheduleFactory injectTask(BukkitRunnable runnable) {
        return new ELDSchedulerFactory(runnable);
    }

    @Override
    public <E> BukkitPromise<E> callAsync(Plugin plugin, Callable<E> callable) {
        injector.inject(callable);
        return new ELDBukkitPromise<>(callable, plugin);
    }

    @Override
    public BukkitPromise<Void> runAsync(Plugin plugin, Runnable runnable) {
        injector.inject(runnable);
        return new ELDBukkitPromise<>(() -> {
            runnable.run();
            return Void.TYPE.cast(null);
        }, plugin);
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
        public BukkitTask run(Plugin plugin) {
            if (timeout == -1 && interval == -1) {
                if (async) {
                    return bukkitRunnable.runTaskAsynchronously(plugin);
                } else {
                    return bukkitRunnable.runTask(plugin);
                }
            } else if (timeout != -1 && interval == -1) {
                if (async) {
                    return bukkitRunnable.runTaskLaterAsynchronously(plugin, timeout);
                } else {
                    return bukkitRunnable.runTaskLater(plugin, timeout);
                }
            } else  {
                if (async) {
                    return bukkitRunnable.runTaskTimerAsynchronously(plugin, timeout, interval);
                } else {
                    return bukkitRunnable.runTaskTimer(plugin, timeout, interval);
                }
            }
        }
    }


    private class ELDBukkitPromise<E> implements BukkitPromise<E> {

        private final Plugin plugin;
        private final BukkitCallable<E> bukkitCallable;

        private ELDBukkitPromise(Callable<E> callable, Plugin plugin) {
            injector.inject(callable);
            bukkitCallable = new BukkitCallable<>(callable);
            this.plugin = plugin;
        }

        @Override
        public <R> BukkitPromise<R> thenApplySync(ChainCallable<E, R> function) {
            var s = new ELDBukkitPromise2<>(new LinkedList<>(List.of(bukkitCallable)), function, plugin, false);
            bukkitCallable.setHandler(e -> {
                s.setElement(e);
                s.start();
            });
            return s;
        }

        @Override
        public BukkitPromise<Void> thenRunSync(Consumer<E> function) {
            return thenApplySync(e -> {
                function.accept(e);
                return Void.TYPE.cast(null);
            });
        }

        @Override
        public <R> BukkitPromise<R> thenApplyAsync(ChainCallable<E, R> function) {
            var s = new ELDBukkitPromise2<>(new LinkedList<>(List.of(bukkitCallable)), function, plugin, true);
            bukkitCallable.setHandler(e -> {
                s.setElement(e);
                s.start();
            });
            return s;
        }

        @Override
        public BukkitPromise<Void> thenRunAsync(Consumer<E> function) {
            return thenApplyAsync(e -> {
                function.accept(e);
                return Void.TYPE.cast(null);
            });
        }

        @Override
        public void join() {
            bukkitCallable.runTaskAsynchronously(plugin);
        }

        @Override
        public void joinWithCatch(Consumer<Throwable> handler) {
            bukkitCallable.catcher = handler;
            this.join();
        }
    }


    private class ELDBukkitPromise2<E, R> implements BukkitPromise<R> {

        public final Plugin plugin;
        private final boolean async;
        private final LinkedList<CatchableRunnable> catchableRunnableLinkedList;
        private final BukkitChainCallable<E, R> bukkitChainCallable;


        public ELDBukkitPromise2(final LinkedList<CatchableRunnable> catchableRunnableLinkedList, ChainCallable<E, R> function, Plugin plugin, boolean async) {
            injector.inject(function);
            bukkitChainCallable = new BukkitChainCallable<>(function);
            this.catchableRunnableLinkedList = catchableRunnableLinkedList;
            this.catchableRunnableLinkedList.addLast(bukkitChainCallable);
            this.plugin = plugin;
            this.async = async;
        }

        public void setElement(E element) {
            bukkitChainCallable.setAccept(element);
        }


        @Override
        public <J> BukkitPromise<J> thenApplySync(ChainCallable<R, J> function) {
            var s = new ELDBukkitPromise2<>(catchableRunnableLinkedList, function, plugin, false);
            bukkitChainCallable.setHandler(e -> {
                s.setElement(e);
                s.start();
            });
            return s;
        }

        @Override
        public BukkitPromise<Void> thenRunSync(Consumer<R> function) {
            return thenApplySync(e -> {
                function.accept(e);
                return Void.TYPE.cast(null);
            });
        }

        @Override
        public <J> BukkitPromise<J> thenApplyAsync(ChainCallable<R, J> function) {
            var s = new ELDBukkitPromise2<>(catchableRunnableLinkedList, function, plugin, true);
            bukkitChainCallable.setHandler(e -> {
                s.setElement(e);
                s.start();
            });
            return s;
        }

        @Override
        public BukkitPromise<Void> thenRunAsync(Consumer<R> function) {
            return thenApplyAsync(e ->{
                function.accept(e);
                return Void.TYPE.cast(null);
            });
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
            var runnable = catchableRunnableLinkedList.pollFirst();
            if (runnable != null) runnable.runTaskAsynchronously(plugin);
        }

        @Override
        public void joinWithCatch(Consumer<Throwable> handler) {
            catchableRunnableLinkedList.forEach(r -> r.catcher = handler);
            this.join();
        }
    }


}
