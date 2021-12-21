package com.ericlam.mc.eld.services.scheduler;

import com.ericlam.mc.eld.misc.ChainCallable;
import com.ericlam.mc.eld.services.ScheduleService;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public final class ELDSchedulerService implements ScheduleService {

    @Inject
    private Injector injector;

    //private static Logger LOGGER = LoggerFactory.getLogger(ELDSchedulerService.class);


    @Override
    public ScheduleFactory injectTask(BukkitRunnable runnable) {
        return new ELDSchedulerFactory(runnable);
    }

    @Override
    public <E> BukkitPromise<E> callAsync(Plugin plugin, Callable<E> callable) {
        return new ELDBukkitPromise<>(callable, plugin);
    }

    @Override
    public BukkitPromise<Void> runAsync(Plugin plugin, Runnable runnable) {
        return new ELDBukkitPromise<>(() -> {
            runnable.run();
            return Void.TYPE.cast(null);
        }, plugin);
    }

    @Override
    public BukkitPromise<Object[]> callAllAsync(Plugin plugin, List<BukkitPromise<Object>> promises) {
        Object[] accepts = new Object[promises.size()];
        AtomicInteger atoi = new AtomicInteger(0);
        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        for (int i = 0; i < promises.size(); i++) {
            var promise = promises.get(i);
            int index = i;
            toCallableHandler(promise).setHandler(e -> {
                lock.lock();
                accepts[index] = e;
                atoi.incrementAndGet();
                condition.signal();
                lock.unlock();
            });
        }
        return new ELDBukkitPromise<>(() -> {
            for (BukkitPromise<Object> promise : promises) {
                promise.join();
            }
            while (atoi.get() < promises.size()){
                lock.lock();
                condition.await();
                lock.unlock();
            }
            return accepts;

        }, plugin){

            @Override
            public void joinWithCatch(Consumer<Throwable> handler) {
                promises.forEach(p -> toCallableHandler(p).setCatcher(handler));
                super.joinWithCatch(handler);
            }

        };
    }

    @Override
    public BukkitPromise<Void> runAllAsync(Plugin plugin, List<BukkitPromise<Void>> promises) {
        AtomicInteger atoi = new AtomicInteger(0);
        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        for (BukkitPromise<Void> promise : promises) {
            toCallableHandler(promise).setHandler(v -> {
                lock.lock();
                atoi.incrementAndGet();
                condition.signal();
                lock.unlock();
            });
        }
        return new ELDBukkitPromise<>(() -> {
            for (BukkitPromise<Void> promise : promises) {
                promise.join();
            }

            while (atoi.get() < promises.size()){
                lock.lock();
                condition.await();
                lock.unlock();
            }

            return Void.TYPE.cast(null);
        }, plugin) {

            @Override
            public void joinWithCatch(Consumer<Throwable> handler) {
                promises.forEach(p -> toCallableHandler(p).setCatcher(handler));
                super.joinWithCatch(handler);
            }

        };
    }


    private class ELDSchedulerFactory implements ScheduleFactory {

        private final BukkitRunnable bukkitRunnable;
        private boolean async = false;
        private long interval = -1;
        private long timeout = -1;

        private ELDSchedulerFactory(BukkitRunnable bukkitRunnable) {
            injector.injectMembers(bukkitRunnable);
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
            injector.injectMembers(callable);
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
            injector.injectMembers(function);
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


    // no more than two, so else-if can be used
    private <E> CallableHandler<E> toCallableHandler(BukkitPromise<E> promise) {
        if (promise instanceof ELDBukkitPromise){
            var bukkitPromise = (ELDBukkitPromise<E>) promise;
            return new CallableHandler<>() {
                @Override
                public void setHandler(Consumer<E> handler) {
                    bukkitPromise.bukkitCallable.setHandler(handler);
                }

                @Override
                public void setCatcher(Consumer<Throwable> catcher) {
                    bukkitPromise.bukkitCallable.catcher = catcher;
                }
            };
        } else if (promise instanceof ELDBukkitPromise2) {
            var bukkitPromise = (ELDBukkitPromise2<? , E>) promise;
            return new CallableHandler<>() {
                @Override
                public void setHandler(Consumer<E> handler) {
                    bukkitPromise.bukkitChainCallable.setHandler(handler);
                }

                @Override
                public void setCatcher(Consumer<Throwable> catcher) {
                    bukkitPromise.bukkitChainCallable.catcher = catcher;
                }
            };
        }else{
            throw new IllegalArgumentException("unknown implementation: "+promise.getClass().getName());
        }
    }


    interface CallableHandler<E> {

        void setHandler(Consumer<E> handler);


        void setCatcher(Consumer<Throwable> catcher);

    }


}
