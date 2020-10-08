package com.ericlam.mc.test.eld.experinment;

import com.ericlam.mc.eld.ServiceCollection;
import com.ericlam.mc.eld.components.CommandNode;
import com.ericlam.mc.eld.services.Service;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;

import java.util.HashSet;
import java.util.Set;

public class TestServiceCollection implements ServiceCollection, Module {

    private final Set<Class<? extends Service>> services = new HashSet<>();
    private final Set<Class<? extends CommandNode<?>>> commands = new HashSet<>();

    @Override
    public <T extends Service> ServiceCollection registerService(Class<T> service) {
        this.services.add(service);
        return this;
    }

    public <T extends CommandNode<?>> void registerCommand(Class<T> command) {
        this.commands.add(command);
    }

    @Override
    public void configure(Binder binder) {
        commands.forEach(cls -> binder.bind(cls).in(Scopes.NO_SCOPE));
        services.forEach(cls -> binder.bind(cls).in(Scopes.SINGLETON));
    }
}
