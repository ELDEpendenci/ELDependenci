package com.ericlam.mc.eld;

import com.ericlam.mc.eld.annotations.InjectPool;
import com.ericlam.mc.eld.components.GroupConfiguration;
import com.ericlam.mc.eld.components.GroupLangConfiguration;
import com.ericlam.mc.eld.configurations.GroupConfig;
import com.ericlam.mc.eld.configurations.GroupLang;
import com.ericlam.mc.eld.services.ConfigPoolService;
import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

@SuppressWarnings("unchecked")
public final class ELDTypeListener implements TypeListener {

    private final ConfigPoolService configPoolService;

    public ELDTypeListener(ConfigPoolService configPoolService) {
        this.configPoolService = configPoolService;
    }

    @Override
    public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
        for (Field field : typeLiteral.getRawType().getFields()) {
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

    private static class InstanceInjector<T> implements MembersInjector<T> {

        private final Field field;
        private final Object value;

        public InstanceInjector(Field field, Object value) {
            this.field = field;
            this.value = value;
            field.setAccessible(true);
        }


        @Override
        public void injectMembers(T t) {
            try {
                field.set(t, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
