package com.ericlam.mc.eld.commands;

import java.util.LinkedList;
import java.util.ListIterator;

public final class ArgIterator implements ListIterator<String> {

    private final LinkedList<String> list;
    private final ListIterator<String> iterator;

    public ArgIterator(LinkedList<String> list) {
        this.list = list;
        this.iterator = list.listIterator();
    }

    public ArgIterator(LinkedList<String> list, int currentIndex){
        this.list = list;
        this.iterator = list.listIterator(currentIndex);
    }

    public ArgIterator cloneIterator() {
        return this.cloneIterator(false);
    }

    public ArgIterator cloneIterator(boolean current) {
        if (current) {
            return new ArgIterator(list, iterator.nextIndex());
        } else {
            return new ArgIterator(list);
        }
    }


    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public String next() {
        return iterator.next();
    }

    @Override
    public boolean hasPrevious() {
        return iterator.hasPrevious();
    }

    @Override
    public String previous() {
        return iterator.previous();
    }

    @Override
    public int nextIndex() {
        return iterator.nextIndex();
    }

    @Override
    public int previousIndex() {
        return iterator.previousIndex();
    }

    @Override
    public void remove() {
        iterator.remove();
    }

    @Override
    public void set(String s) {
        iterator.set(s);
    }

    @Override
    public void add(String s) {
        iterator.add(s);
    }
}
