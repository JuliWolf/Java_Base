# Как использовать Лямбда выражения

## Выражение больше одной сткри

Если выражение внутри анонимной функции больше одной строки, то необходимо использовать фигурные скобки

```
runner.run(() -> {
  System.out.println("Hello");
  System.out.println("Goodbye");
});
```

## Возвращаемое значение

Тип возвращаемого значения определяется в функциональном интерфейсе
Чтобы вернуть значение из лямбда выражения необходимо использовать ключевое слово `return` или лямбда выражение должно быть в одну строку

```
interface Executable {
  int execute();
}

class Runner {
  public void run(Executable e) {
    int a = e.execute();
    System.out.println(a);
  }
}

public class Main {
  public static void main(String[] args) {
    Runner runner = new Runner();
    runner.run(() -> {
      System.out.println("Hello");
      System.out.println("Goodbye");


      return 2;
    });
    
    runner.run(() -> 2);
  }
}
```