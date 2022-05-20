package com.ericlam.mc.eldtest;

import com.ericlam.mc.eld.annotations.Commander;

import javax.inject.Singleton;
import java.util.Arrays;

public class TestAnnotationPresent {

    public static void main(String[] args) {
        System.out.println(Impl.class.isAnnotationPresent(Commander.class));
        System.out.println(Impl.class.getAnnotation(Commander.class) != null);
        System.out.println(Arrays.toString(Impl.class.getAnnotations()));
        System.out.println(Arrays.toString(Impl.class.getDeclaredAnnotations()));
    }


    @Commander(name = "test", description = "xyz")
    public static abstract class Abstracter {
    }

    @Singleton
    public static class Impl extends Abstracter {
    }
}
