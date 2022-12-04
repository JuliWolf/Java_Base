package Manipulations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Scanner;

public class TestReflection {
  public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
    Scanner scanner = new Scanner(System.in);
    // Название_класса1 Название_класса2 Название_метода
    Class classObject1 = Class.forName(scanner.next());
    Class classObject2 = Class.forName(scanner.next());
    String methodName = scanner.next();

    Method m = classObject1.getMethod(methodName, classObject2);
    Object o1 = classObject1.getDeclaredConstructor().newInstance();
    Object o2 = classObject2.getConstructor(String.class).newInstance("String value");

    m.invoke(o1, o2);

    System.out.println(o1);

    // Example.Person java.lang.String setName -> Person{id=-1, name='String value'}
    // java.lang.Thread java.lang.String setName -> Thread[String value,5,main]
  }
}
