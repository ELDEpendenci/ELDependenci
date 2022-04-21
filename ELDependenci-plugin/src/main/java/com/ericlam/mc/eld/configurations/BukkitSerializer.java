package com.ericlam.mc.eld.configurations;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class BukkitSerializer<T extends ConfigurationSerializable> extends JsonSerializer<T> implements ContextualSerializer {

    private final Class<T> t;
    private final JsonSerializer<?> originalSerializer;

    public BukkitSerializer(Class<T> t, JsonSerializer<?> originalSerializer) {
        this.t = t;
        this.originalSerializer = originalSerializer;
    }

    @Override
    public void serialize(T t, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        serializerProvider.defaultSerializeValue(toMap(t), jsonGenerator);
    }

    private <CS extends ConfigurationSerializable> LinkedHashMap<String, Object> toMap(CS cs) {
        var map = new LinkedHashMap<String, Object>();
        map.put("==", ConfigurationSerialization.getAlias(cs.getClass()));
        map.putAll(cs.serialize().entrySet().stream().map(en -> {
            var value = en.getValue() instanceof ConfigurationSerializable ? toMap((ConfigurationSerializable) en.getValue()) : en.getValue();
            return new BukkitBeanModifier.Entry(en.getKey(), value);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        return map;
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) throws JsonMappingException {
        var typeOpt = Optional.ofNullable(beanProperty).map(BeanProperty::getType).map(JavaType::getRawClass);
        if (typeOpt.isEmpty()){
            return this.originalSerializer;
        }
        var type = typeOpt.get();
        if (ConfigurationSerializable.class.isAssignableFrom(type)) {
            return this;
        } else {
            return serializerProvider.findValueSerializer(type);
        }
    }

    @Override
    public Class<T> handledType() {
        return t;
    }
}

