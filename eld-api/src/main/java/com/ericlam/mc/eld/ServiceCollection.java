package com.ericlam.mc.eld;

import com.ericlam.mc.eld.components.Configuration;

public interface ServiceCollection {

    ServiceCollection addSingleton(Class<?> singleton);

    <T, L extends T> ServiceCollection addService(Class<T> service, Class<L> implement);

    <T extends Configuration> ServiceCollection addConfiguration(Class<T> config);

}
