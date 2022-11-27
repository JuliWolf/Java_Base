# Лямбда выражения

Это анонимные функции или методы.

## Пример использования

1. Лямбда выражения подходят при реализации функциональных интерфейсов, то есть интерфейсов, к которых не реализован один метод

```
interface Executable {
  void execute();
}

class Runner {
  public void run(Executable e) {
    e.execute();
  }
}

class ExecutableImplementation implements Executable {

  @Override
  public void execute() {
    System.out.println("Hello");
  }
}

public class Main {
  public static void main(String[] args) {
//    Thread thread = new Thread(() -> System.out.println("Hello!"));
    Runner runner = new Runner();
    runner.run(new ExecutableImplementation());
    runner.run(new Executable() {
      @Override
      public void execute() {
        System.out.println("Hello");
      }
    });
    runner.run(() -> System.out.println("Hello"));
  }
}
```