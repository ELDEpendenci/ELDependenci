package com.ericlam.mc.test.eld;


import com.ericlam.mc.eld.ELDependenci;
import com.ericlam.mc.eld.configurations.ELDConfigManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class TestDependencyOrder {

    private static ObjectMapper MAPPER = ELDConfigManager.OBJECT_MAPPER;

    @Test
    public void testDependency() {
        // if throws unsupported operation error, it means dependency order not correct
        Assertions.assertThrows(IllegalStateException.class, ELDependenci::getApi);
    }


    public static void main(String[] args) throws IOException {
        Object[] os = new Object[3];
        System.out.println(os.length);
        os[1] = 123;
        os[0] = "123";
        System.out.println(os.length);
        System.out.println(os[2]);
    }

    public static class TestPojo {

        public String a;
        public int b;
        public double c;
        public boolean d;
        public NestedPojo e;

        public static class NestedPojo {
            public int e;
            public float f;
            public String g;
        }

    }
}
