package com.ericlam.mc.test.eld;

import java.util.List;
import java.util.ListIterator;

public class Main {

    public static void main(String[] args) {
        List<String> originList = List.of(
                "a",
                "b",
                "c",
                "d",
                "e",
                "f",
                "g"
        );
        ListIterator<String> iterator = originList.listIterator();
        var index = iterator.nextIndex();
        System.out.println(iterator.next());
        System.out.println(iterator.next());
        System.out.println(iterator.next());

        ListIterator<String> iterator2 = originList.listIterator(index);
        iterator2.forEachRemaining(System.out::println);
    }


}
