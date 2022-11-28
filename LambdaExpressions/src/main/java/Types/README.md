# Как использовать Лямбда выражения

## Выражение больше одной строки

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

## Использование аргументов метода

В краткой форме лямбда выражения можно не писать типы принимаемых значений, кроме того
можно не использовать круглые скобки если метод ожидает только один параметр

```
interface Executable {
  int execute(int x);
}

class Runner {
  public void run(Executable e) {
    int a = e.execute(10);
    System.out.println(a);
  }
}

public class Main {
  public static void main(String[] args) {
    Runner runner = new Runner();
    runner.run(new Executable() {
      @Override
      public int execute(int x) {
        return x + 10;
      }
    });
    runner.run((int x) -> {
      return x + 10;
    });

    runner.run(x -> x + 10);
  }
}
```

```
interface Executable {
  int execute(int x, int y);
}

class Runner {
  public void run(Executable e) {
    int a = e.execute(10, 15);
    System.out.println(a);
  }
}

public class Main {
  public static void main(String[] args) {
    Runner runner = new Runner();
    runner.run(new Executable() {
      @Override
      public int execute(int x, int y) {
        return x + y;
      }
    });
    runner.run((int x, int y) -> {
      return x + y;
    });

    runner.run((x, y) -> x + y);
  }
}
```

## Область видимости

У лямбда выражений нет своей области видимости. 

```
interface Executable {
  int execute(int x, int y);
}

class Runner {
  public void run(Executable e) {
    int a = e.execute(10, 15);
    System.out.println(a);
  }
}

public class Main {
  public static void main(String[] args) {
    Runner runner = new Runner();

    int a = 1;

    runner.run((x, y) -> {
//      int a = 5; // Variable 'a' is already defined in the scope
      return x + y;
    });
  }
}
```

## Пример как отсортировать строки по длине

```
public class Main {
  public static void main(String[] args) {
    List<String> list = new ArrayList<>();

    list.add("Hello");
    list.add("Goodbye");
    list.add("a");
    list.add("ab");

    list.sort((s1, s2) -> {
      if (s1.length() > s2.length()) return 1;
      else if (s1.length() < s2.length()) return -1;
      else return 0;
    });

    System.out.println(list);
  }
}
```

```
public class Main {
  public static void main(String[] args) {
    List<String> list = new ArrayList<>();

    list.add("Hello");
    list.add("Goodbye");
    list.add("a");
    list.add("ab");

    Comparator<String> comparator = (s1, s2) -> {
      if (s1.length() > s2.length()) return 1;
      else if (s1.length() < s2.length()) return -1;
      else return 0;
    };

    list.sort(comparator);

    System.out.println(list);
  }
}
```