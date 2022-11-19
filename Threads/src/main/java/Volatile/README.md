# Volatile

Volatile - ключевое слово java, которое говорит о том, что переменная будет меняться и поэтому ее не надо кешировать.

## Проблема
1. При запуске потока, значения которые используются внутри потока кешируются. Это сделано для того, чтобы программа работала максимально быстро и брала данные из самого быстрого источника - кеша.
2. При изменении переменных может возникнуть кейс, когда значение свойства в кеше осталось прежнее, когда как в процессе значение переменной уже поменялось, что приведет к не правильному поведению в работе программы.

## Решение
1. Использовать ключевое слово `volatile` для того, чтобы переменная не кешировалась и ее значение всегда бралось из самой переменной, а не из кеша

```
import java.util.Scanner;

public class Main {
  public static void main(String[] args) {
    MyThread myThread = new MyThread();
    myThread.start();

    // Создаем сканнер
    Scanner scanner = new Scanner(System.in);
    // Ждем следующую линию
    scanner.nextLine();

    // останавливаем поток
    myThread.shutdown();
  }
}

class MyThread extends Thread {
  // Отключаем кеширование для свойства running
  private volatile boolean running = true;

  public void run () {
    while (running) {
      System.out.println("Hello");
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void shutdown () {
    this.running = false;
  }
}
```