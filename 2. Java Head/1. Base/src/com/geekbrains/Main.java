package com.geekbrains;

public class Main {
  public static void main(String[] args) {
    System.out.println("Hello world");
    
    int test = 10;
//  Произодет переолнение и значение будет -127
    byte b = (byte) 129;

    int a = 20;
        a *= 20;


    System.out.println(test);
    System.out.println(b);
  }
}
