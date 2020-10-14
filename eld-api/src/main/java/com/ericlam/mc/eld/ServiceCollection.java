package com.ericlam.mc.eld;

import com.ericlam.mc.eld.components.Configuration;

import java.util.Map;

public interface ServiceCollection {

    ServiceCollection addSingleton(Class<?> singleton);

    <T, L extends T> ServiceCollection addService(Class<T> service, Class<L> implementation);

    <T> ServiceCollection addServices(Class<T> service, Map<String, Class<? extends T>> implementations);

    <T extends Configuration> ServiceCollection addConfiguration(Class<T> config);

}
