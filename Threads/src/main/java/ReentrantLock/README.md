# ReentrantLock

ReentrantLock - класс для работы с потоками.
Задача класса ограничить доступ до lock объекта, аналог оператора synchronized

## Пример

1. Создаем класс с методами `firstThread` `secondThread` и `increment`
2. Объявляем свойство `counter`
3. Объявляем свойство `lock` и присваиваем ему значение `new ReentrantLock()`
4. В методах `firstThread` и `secondThread` будет вызываться метод `increment`
5. При вызове метода `lock` у объекта `lock` остальные потоки переходят в режим ожидания и не имеют доступа к монитору
6. При вызове метода `unlock` у объекта `lock` монитор освобождается, что дает возможность другим потокам получить доступ к монитору

NOTE: Если метод `lock` вызывается несколько раз, то чтобы монитор разблокировался нужно столько же раз вызвать метод `unlock`
```
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
  public static void main(String[] args) throws InterruptedException {
    Task task = new Task();

    Thread thread1 = new Thread(
        new Runnable() {
          @Override
          public void run() {
            task.firstThread();
          }
        }
    );

    Thread thread2 = new Thread(
        new Runnable() {
          @Override
          public void run() {
            task.secondThread();
          }
        }
    );

    thread1.start();
    thread2.start();

    thread1.join();
    thread2.join();

    task.showCounter();
  }
}

class Task {
  private int counter;
  private Lock lock = new ReentrantLock();

  private void increment () {
    for (int i = 0; i < 1000; i++) {
      counter++;
    }
  }

  public void firstThread() {
    lock.lock();
    increment();
    lock.unlock();
  }

  public void secondThread() {
    lock.lock();
    increment();
    lock.unlock();
  }

  public void showCounter () {
    System.out.println(counter);
  }
}
```