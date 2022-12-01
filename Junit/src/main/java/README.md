# Junit

Библиотека для создания модульных тестов

## Пример

1. Для создания теста создается класс с названием соотвествующим названию класса, который будет в нем тестироваться + постфикс "Test", например `Vector2DTest`, который будет содержать тесты класса `Vector2D`
2. Класс создается в папке `test/java`
3. Для обозначения метода необходимо написать аннотацию `@Test` 
4. Методы теста обязательно должны быть `public void`
5. Для проверки результатов используются статические методы класса `Assert`

```
public class Vector2D {
  private double x;
  private double y;

  public double getX() {
    return x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }

  public double length () {
    return Math.sqrt(x * x + y * y);
  }
}
```

```
import org.junit.Assert;
import org.junit.Test;

public class Vector2DTest {
  @Test
  public void newVectorShouldHaveZeroLength () {
    Vector2D v1 = new Vector2D(); // action

    // assertion
    // 1e-9 = 0.000...0001
    Assert.assertEquals("Vector2D length method", 0 , v1.length(), 1e-9);
  }

  @Test
  public void newVectorShouldHaveZeroX () {
    Vector2D v1 = new Vector2D();

    Assert.assertEquals("Vector2D x value", 0, v1.getX(), 1e-9);
  }

  @Test
  public void newVectorShouldHaveZeroY () {
    Vector2D v1 = new Vector2D();

    Assert.assertEquals("Vector2D y value", 0, v1.getY(), 1e-9);
  }
}
```

## Expected

Для проверки некоторого ожидаемого результата можно передать параметр `expected` в аннотацию `@Test`

```
public class MyMath {
  public static double divide (int number1, int number2) {
    if (number2 == 0) {
      throw new ArithmeticException("Can't divide by zero");
    }

    return number1 / number2;
  }
}
```
```
import org.junit.Test;

public class MyMathTest {

    // Ожидается, что будет выброшено исключение
  @Test(expected = ArithmeticException.class)
  public void zeroDenominatorShouldThrowException () {
    MyMath.divide(1, 0);
  }
}

```

## Timeout

Для проверки времени работы метода, можно передать параметр `timeout` в аннотацию `@Test`

```
public class NetworkUtils {
  public static void getConnection () {
    // получаем соединение с сервером
    try {
      Thread.sleep(1500);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return;
  }
}

```
```
import org.junit.Test;

public class NetworkUtilsTest {

  @Test(timeout = 1000)
  public void getConnectionShouldReturnFasterThanOneSecond () {
    NetworkUtils.getConnection();
  }
}
```