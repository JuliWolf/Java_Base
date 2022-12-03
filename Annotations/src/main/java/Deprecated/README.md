# @Deprecated

Аннотация для обозначения того, аннотированное свойство, метода, класс и т.д. устарело и его больше не стоит использовать

```
package Deprecated;

public class MyClass {
  @Deprecated
  public void myMethod () {
    System.out.println("Hello from myMethod");
  }
}
```

```
package Deprecated;

public class Main {
  public static void main(String[] args) {
    MyClass myCLass = new MyClass();
    myCLass.myMethod(); // 'myMethod()' is deprecated 
  }
}
```
