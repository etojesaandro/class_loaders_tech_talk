package com.example;

public class Foo {
    public void print() {
        System.out.println("Foo: " + getClass().getClassLoader());
    }
}
