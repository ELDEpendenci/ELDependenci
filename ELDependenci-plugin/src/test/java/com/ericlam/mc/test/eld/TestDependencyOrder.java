package com.ericlam.mc.test.eld;


import com.ericlam.mc.eld.ELDependenci;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class TestDependencyOrder {

    @Test
    public void testDependency() {
        // if throws unsupported operation error, it means dependency order not correct
        Assertions.assertThrows(IllegalStateException.class, ELDependenci::getApi);
    }

    public static void main(String[] args) {
        int a = (int)Math.ceil((double) 1/10);
        System.out.println(a);
    }
}
