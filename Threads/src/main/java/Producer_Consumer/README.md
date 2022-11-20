# Паттерн Producer Consumer

Паттер "Производитель-Потребитель" заключается в том, что один поток создает данные, а второй эти данные использует.
Для релизации паттерна чаще всегь используеются классы из модуля `java.util.concurrent` 
В данном модуле созданы классы для работы с данными из нескольких потоков. Все классы синхронизированы и потогают избежать "Состояние гонки"

## Пример

1. Producer добавляет рандомные числа от 0 до 99 в массив. Максимально количество чисел 10
2. Consumer раз в 100 милисекунд берет данные из очереди. 
3. Если очередь была заполнена, то Producer ожидает пока Consumer не заберет данные из очереди

```
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {
  private static BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(10);

  public static void main(String[] args) throws InterruptedException {
    Thread thread1 = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          produce();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    });

    Thread thread2 = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          consume();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    });

    thread1.start();
    thread2.start();

    thread1.join();
    thread2.join();
  }

  private static void produce () throws InterruptedException {
    Random random = new Random();

    while(true) {
      queue.put(random.nextInt(100));
    }
  }

  private static void consume () throws InterruptedException {
    Random random = new Random();

    while (true) {
      Thread.sleep(100);

      if (random.nextInt(10) == 5) {
        System.out.println(queue.take());
        System.out.println("Queue size is " + queue.size());
      }
    }
  }
}

```