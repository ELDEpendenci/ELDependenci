package com.ericlam.mc.test.eld;

public class BeInject implements ToInject<Long> {

    @Override
    public void assign(Long data) {
        System.out.println("data: "+data);
        System.out.println("type: "+data.getClass());
    }
}
