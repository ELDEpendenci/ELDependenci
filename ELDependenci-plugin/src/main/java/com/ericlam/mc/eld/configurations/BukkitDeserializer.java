package com.ericlam.mc.eld.configurations;

import com.ericlam.mc.eld.ELDependenci;
import com.ericlam.mc.eld.misc.DebugLogger;
import com.ericlam.mc.eld.services.LoggingService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public final class BukkitDeserializer<T extends ConfigurationSerializable> extends JsonDeserializer<T> implements ContextualDeserializer {

    private final Class<T> t;

    public BukkitDeserializer(Class<T> t) {
        this.t = t;
    }

    @Override
    public Class<?> handledType() {
        return t;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {
        return this;
    }

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return (T) toDeserializeObject(jsonParser.readValueAs(new TypeReference<Map<String, Object>>() {
        }));
    }

    private <CS extends ConfigurationSerializable> CS toDeserializeObject(Map<String, Object> map) {
        //logger.debug("before map: "+map.toString());
        var mapp = map.entrySet().stream().map(en -> {
            var value = en.getValue() instanceof Map && ((Map<String, Object>) en.getValue()).containsKey("==") ? toDeserializeObject((Map<String, Object>) en.getValue()) : en.getValue();
            return new BukkitBeanModifier.Entry(en.getKey(), value);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        //logger.debug("after map: " + mapp.toString());
        return (CS) ConfigurationSerialization.deserializeObject(mapp);
    }


}

