package com.ericlam.mc.test.eld;


import com.ericlam.mc.eld.ELDependenci;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestDependencyOrder {

    @Test
    public void testDependency() {
        // if throws unsupported operation error, it means dependency order not correct
        Assertions.assertThrows(IllegalStateException.class, ELDependenci::getApi);
    }
}
