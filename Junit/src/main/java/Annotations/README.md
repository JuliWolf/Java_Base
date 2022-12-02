# Annotations

Аннотации для тестов это некоторые вспомогательные классы, которые могут выполнить некоторую логику до выполнения теста и после


## Жизненный цикл тестирующего класса

1. При запуске каждого тестового метода создается обхект тестового класса
2. `@BeforeClass` - запускается один раз для тестового класса (является статическим)
3. Для каждого `@Test` метода создается эксземпляр тестового класса
- Запускается вспомогательный метод `@Before` (перед каждым тестовым методом будет запускаться метод с аннотацией `@Before`)
- Далее запускается сам `@Test` метод
- После запускается метод с аннотацией `@After`
4. `@AfterClass` - запускается после того как все методы тестового класса выполнятся (является статическим)

```
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class Vector2DTest {
  private final double EPS = 1e-9;

  private static Vector2D v1;

  @BeforeClass
  public static void createNewVector () {
    v1 = new Vector2D();
  }

  @Test
  public void newVectorShouldHaveZeroLength () {
    Assert.assertEquals("Vector2D length method", 0 , v1.length(), EPS);
  }

  @Test
  public void newVectorShouldHaveZeroX () {
    Assert.assertEquals("Vector2D x value", 0, v1.getX(), EPS);
  }

  @Test
  public void newVectorShouldHaveZeroY () {
    Assert.assertEquals("Vector2D y value", 0, v1.getY(), EPS);
  }
}
```