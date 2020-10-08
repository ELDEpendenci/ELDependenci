package com.ericlam.mc.test.eld.experinment;

import com.ericlam.mc.eld.ServiceCollection;
import com.ericlam.mc.eld.components.CommandNode;
import org.reflections.Reflections;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ELDependenciTest {

    private Set<Class<? extends CommandNode>> commands;
    private final TestServiceCollection collection = new TestServiceCollection();

    private ELDependenciTest(Object plugin, Consumer<ServiceCollection> injector) {
        Reflections ref = new Reflections(plugin.getClass().getPackageName());
        this.commands = ref.getSubTypesOf(CommandNode.class);
    }

    public static void register(Object plugin, Consumer<ServiceCollection> injector) {
        new ELDependenciTest(plugin, injector);
    }
}
