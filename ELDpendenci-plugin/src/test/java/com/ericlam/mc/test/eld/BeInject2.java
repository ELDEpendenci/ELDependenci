package com.ericlam.mc.test.eld;

public class BeInject2 implements ToInject<Integer>{
    @Override
    public void assign(Integer data) {
        System.out.println("data: "+data);
        System.out.println("type: "+data.getClass());
    }
}
