package com.ericlam.mc.eld.guice;

import com.ericlam.mc.eld.annotations.InjectPool;
import com.ericlam.mc.eld.components.GroupConfiguration;
import com.ericlam.mc.eld.components.GroupLangConfiguration;
import com.ericlam.mc.eld.configurations.GroupConfig;
import com.ericlam.mc.eld.configurations.GroupLang;
import com.ericlam.mc.eld.services.ConfigPoolService;
import com.ericlam.mc.eld.services.ReflectionService;
import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

@SuppressWarnings("unchecked")
public final class ELDConfigPoolListener implements TypeListener {

    private final ConfigPoolService configPoolService;
    private final ReflectionService reflectionService;

    public ELDConfigPoolListener(ConfigPoolService configPoolService, ReflectionService reflectionService) {
        this.configPoolService = configPoolService;
        this.reflectionService = reflectionService;
    }

    @Override
    public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
        for (Field field : reflectionService.getDeclaredFieldsUpTo(typeLiteral.getRawType(), null)) {
            if (!field.isAnnotationPresent(InjectPool.class)) continue;
            if (!(field.getGenericType() instanceof ParameterizedType)) continue;
            if (field.getType() == GroupConfig.class) {
                ParameterizedType t = (ParameterizedType) field.getGenericType();
                Class<? extends GroupConfiguration> groupConfigType = (Class<? extends GroupConfiguration>) t.getActualTypeArguments()[0];
                typeEncounter.register(new InstanceInjector<>(field, configPoolService.getGroupConfig(groupConfigType)));
            } else if (field.getType() == GroupLang.class) {
                ParameterizedType t = (ParameterizedType) field.getGenericType();
                Class<? extends GroupLangConfiguration> groupConfigType = (Class<? extends GroupLangConfiguration>) t.getActualTypeArguments()[0];
                typeEncounter.register(new InstanceInjector<>(field, configPoolService.getGroupLang(groupConfigType)));
            }
        }
    }

}
