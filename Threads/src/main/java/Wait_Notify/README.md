# Wait and Notify

Wait - метод для перевода потока в режим ожидания. Помимо перевода в поток ожидания поток освобождает монитор.
Wait может принимать параметр timeout - время по истечению которого поток перестает ждать.

Notify - метод для оповещения одного потока в состоянии wait чтобы тот проснулся. Оповещает один рандомный поток в состоянии wait. При вызове метода монитор не освобождается.
NotifyAll - метод для оповещения всех потоков в состоянии wait. Монитор при этом не освобождается.

## Пример
1. Создаем класс с методами `produce` & `consume`
2. В методе `produce` создаем синхронизованный блок, монитором которого будет являеться текущий класс (this)
3. В синхронизованном блоке вызываем метод `wait`. При вызове метода монитор (this) освобожается, что позволяет все остальные потоки, завязанные на текущий монитор работать
4. В методе `consume`, который будет вызываться в другом потоке, создаем синхронизованных блок, завязанный на монитор (this) и вызываем метод `notify`

Note: даже если монитором указан не `this` методы `wait` & `notify` все-равно вызываются в текущем контексте
Чтобы вызвать данные методы на конкретном объекте, необходимо явно обратиться к желаемому объекту и вызвать методы. 
```
import java.util.Scanner;

public class Main {
  public static void main(String[] args) throws InterruptedException {
    WaitAndNotify wn = new WaitAndNotify();

    Thread thread1 = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          wn.produce();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    });
    Thread thread2 = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          wn.consume();
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
}

class WaitAndNotify {
  public void produce () throws InterruptedException {
    synchronized (this) {
      System.out.println("Producer thread started...");

      // 1. отдаем intrinsic look
      // 2. ждем, пока будет вызван notify()
      wait();

      System.out.println("Producer thread resumed...");
    }
  }


  public void consume () throws InterruptedException {
    Thread.sleep(1000 * 2);
    Scanner scanner = new Scanner(System.in);

    synchronized (this) {
      System.out.println("Waiting for return key pressed");
      scanner.nextLine();
      notify();

      Thread.sleep(5000);
    }
  }
}
```

## Producer Consumer с использованием wait и notify

1. Создаем класс с двумя методами `consume` `produce`
2. Создаем свойства 
- `queue` для сохранения элементов
- `LIMIT` для обозначения максимального количества элементов в очереди
- `lock` для синхронизации
3. Внутри метода `produce` создаем синхронизованный блок с локом на объект `lock`
4. Когда размер очереди будет равно заданному лимиту поток должен переходить в режим ожидания
5. После добавления элемента в очереди необходимо оповестить другой поток о том, что элемент был добавлен
6. Внутри метода `consume` создаем синхронизованный блок с локом на объект `lock`
7. Когда размер очереди равен 0, тогда потом должен переходить в режим ожидания
8. Когда поток работает вытаскиваем элемент из очереди и выводим его на экран
9. После вытаскивания элемента необходимо оповестить другой поток о том, что элемент был вытащен из очереди

```
import java.util.LinkedList;
import java.util.Queue;

public class Producer_Consumer {

  public static void main(String[] args) throws InterruptedException {
    ProducerConsumer wn = new ProducerConsumer();

    Thread thread1 = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          wn.produce();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    });
    Thread thread2 = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          wn.consume();
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
}

class ProducerConsumer {
  private Queue<Integer> queue = new LinkedList<>();
  private final int LIMIT = 10;
  private final Object lock = new Object();

  public void produce () throws InterruptedException {
    int value = 0;

    while (true) {
      synchronized (lock) {
        while (queue.size() == LIMIT) {
          lock.wait();
        }

        queue.offer(value++);
        lock.notify();
      }
    }
  }

  public void consume () throws InterruptedException {
    while(true) {
      synchronized (lock) {
        while (queue.size() == 0) {
          lock.wait();
        }

        int value = queue.poll();
        System.out.println(value);
        System.out.println("Queue size is " + queue.size());

        lock.notify();
      }

      Thread.sleep(1000);
    }
  }
}
```
