package com.ericlam.mc.test.eld;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        var list = new ArrayList<Sortable>();
        list.add(new Sortable(0, true));
        list.add(new Sortable(1, false));
        list.add(new Sortable(2, true));
        list.add(new Sortable(3, false));
        list.sort((s1, s2) -> Integer.compare(s1.order, s2.order) * Boolean.compare(s1.optional, s2.optional));
        System.out.println(list.toString());
    }

    static class Sortable {
        final int order;
        final boolean optional;

        Sortable(int order, boolean optional) {
            this.order = order;
            this.optional = optional;
        }

        @Override
        public String toString() {
            return "Sortable{" +
                    "order=" + order +
                    ", optional=" + optional +
                    '}';
        }
    }
}
