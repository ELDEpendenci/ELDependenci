package com.ericlam.mc.eldtest;

import com.ericlam.mc.eld.common.CommonCommandNode;
import com.ericlam.mc.eld.common.CommonRegistry;
import com.ericlam.mc.eld.registration.CommandRegistry;
import com.ericlam.mc.eld.registration.ListenerRegistry;

import java.util.List;

public class TestAPIUse {


    public interface MySender {
    }

    public interface MyListener{

    }

    public interface MyCommandNode extends CommonCommandNode<MySender> {
    }


    public interface MyRegistry extends CommonRegistry<MyCommandNode, MyListener>{

    }


    public static class CommandA implements MyCommandNode {
        @Override
        public void execute(MySender mySender) {
        }
    }

    public static class CommandB implements MyCommandNode {
        @Override
        public void execute(MySender mySender) {
        }
    }

    public static class CommandC implements MyCommandNode {
        @Override
        public void execute(MySender mySender) {
        }
    }

    public static class ListenerA implements MyListener {
    }

    public static class ListenerB implements MyListener {
    }

    public static class MyRegistryImpl implements MyRegistry {

        @Override
        public void registerCommand(CommandRegistry<MyCommandNode> registry) {
            registry.command(CommandA.class, c -> {

                c.command(CommandB.class, cc -> {

                    cc.command(CommandC.class);

                });

            });
        }

        @Override
        public void registerListeners(ListenerRegistry<MyListener> registry) {
            registry.listeners(List.of(ListenerA.class, ListenerB.class));
        }
    }


}
