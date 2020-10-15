package com.ericlam.mc.test.eld;

import com.ericlam.mc.eld.ELDependenci;

import java.util.Map;

public class Main {

    public static void main(String[] args) {
        ELDependenci.getApi().register(null, inject -> {
            inject.addServices(MyService.class, Map.of(
                    "A", MyServiceA.class,
                    "B", MyServiceB.class
            ));
        });
    }


}
