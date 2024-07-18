package com.example;

public class Main
{
  
  private final Foo foo;
  
  public static void run(Foo foo)
  {
    foo.print();
  }
  
  public Main(Foo foo)
  {
    this.foo = foo;
  }
  
}
