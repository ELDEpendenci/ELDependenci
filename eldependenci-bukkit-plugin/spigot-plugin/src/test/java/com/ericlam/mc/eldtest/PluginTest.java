package com.ericlam.mc.eldtest;

import com.ericlam.mc.eld.ELDependenci;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PluginTest {

    @Test
    public void testDependencyOrder() {
        // if throws unsupported operation error, it means dependency order not correct
        Assertions.assertThrows(IllegalStateException.class, ELDependenci::getApi);
    }


}
