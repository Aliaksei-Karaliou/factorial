package com.github.aliakseikaraliou.factorial.demo;

import com.github.aliakseikaraliou.factorial.SimpleFactory;

@SimpleFactory(postfix = "Name")
public class SomeClass {

    public SomeClass() {
        System.out.println();
    }

    public SomeClass(String name) {
        System.out.println();
    }
}
