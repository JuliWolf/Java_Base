# CountDownLatch

CountDownLatch - класс для работы с потоками.
Принимает первым параметром количество итераций, которые необходимо отсчитать назад прежде чем "защелка" отопрется.

## Пример
1. Создать объект класса CountDownLatch
2. Передать при создании число итераций
3. Создать поток, действием которого будет уменьшение количества итерация объекта класса CountDownLatch с помощью вызова метода `countDown`
4. В основном потоке вызвать метод `await` - это место где будет блокировать поток до тех пор пока не выполнится 3 итерации

```
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
  public static void main(String[] args) throws InterruptedException {
    CountDownLatch countDownLatch = new CountDownLatch(3);

    ExecutorService executorService = Executors.newFixedThreadPool(3);
    for (int i = 0; i < 3; i++) {
      executorService.submit(new Processor(i, countDownLatch));
    }

    executorService.shutdown();

    countDownLatch.await();

    System.out.println("Latch has been opened, main thread is proceeding!");
  }
}

class Processor implements Runnable {
  private int id;
  private CountDownLatch countDownLatch;

  public Processor (int id, CountDownLatch countDownLatch) {
    this.id = id;
    this.countDownLatch = countDownLatch;
  }

  @Override
  public void run() {
    try {
      Thread.sleep(3000);
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }

    countDownLatch.countDown();
  }
}
```

1. Создать объект класса CountDownLatch
2. Передать при создании число итераций
3. Создать поток, который будет ожидать разблокировки CountDownLatch
4. В основном потоке вызвать вызвать метод `countDown` для разблокировки потоков

```
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
  public static void main(String[] args) throws InterruptedException {
    CountDownLatch countDownLatch = new CountDownLatch(3);

    ExecutorService executorService = Executors.newFixedThreadPool(3);
    for (int i = 0; i < 3; i++) {
      executorService.submit(new Processor(i, countDownLatch));
    }

    executorService.shutdown();

    for (int i = 0; i < 3; i++) {
      Thread.sleep(1000);
      countDownLatch.countDown();
    }
  }
}

class Processor implements Runnable {
  private int id;
  private CountDownLatch countDownLatch;

  public Processor (int id, CountDownLatch countDownLatch) {
    this.id = id;
    this.countDownLatch = countDownLatch;
  }

  @Override
  public void run() {
    try {
      Thread.sleep(3000);
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }

    try {
      countDownLatch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    System.out.println("Thread with id " + id + " proceeded.");
  }
}
```
