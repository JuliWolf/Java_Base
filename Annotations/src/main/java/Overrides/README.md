# @Override

Для избежания ошибок при переопределении метода унаследованного от родителя

```
public class Overrides.A {
  public void testA () {
    System.out.println("Hello from class Overrides.A");
  }
}
```

```
public class Overrides.B extends Overrides.A {
  @Override
  public void testA () {
    System.out.println("Hello from class Overrides.B");
  }
}
```

## @Deprecated