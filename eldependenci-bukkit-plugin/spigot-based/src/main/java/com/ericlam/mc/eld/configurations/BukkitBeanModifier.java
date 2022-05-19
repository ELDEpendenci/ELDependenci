package com.ericlam.mc.eld.configurations;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Map;

/*


.registerModule(new SimpleModule()
                        .setDeserializerModifier(new BukkitBeanModifier.Deserializer())
                        .setSerializerModifier(new BukkitBeanModifier.Serializer()))


 */

@SuppressWarnings({"rawtypes", "unchecked"})
public final class BukkitBeanModifier {

    private BukkitBeanModifier() {
    }

    public static class Deserializer extends BeanDeserializerModifier {
        @Override
        public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
            if (ConfigurationSerializable.class.isAssignableFrom(beanDesc.getBeanClass())) {
                return new BukkitDeserializer(beanDesc.getBeanClass());
            }
            return super.modifyDeserializer(config, beanDesc, deserializer);
        }
    }

    public static class Serializer extends BeanSerializerModifier {
        @Override
        public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
            if (ConfigurationSerializable.class.isAssignableFrom(beanDesc.getBeanClass())) {
                return new BukkitSerializer(beanDesc.getBeanClass());
            }
            return super.modifySerializer(config, beanDesc, serializer);
        }
    }

    public static class Entry implements Map.Entry<String, Object> {

        private final String str;
        private Object v;

        public Entry(String str, Object v) {
            this.str = str;
            this.v = v;
        }

        @Override
        public String getKey() {
            return str;
        }

        @Override
        public Object getValue() {
            return v;
        }

        @Override
        public Object setValue(Object value) {
            this.v = value;
            return v;
        }
    }
}

