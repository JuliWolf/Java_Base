# Semaphore

Semaphore - класс для работы с потоками
Задача класса ограничить доступ к ресурсу в один момент. При создании класса указывается максимальное количество потоков, которые могут получить доступ до ресурса

## Метод `availablePermits`

1. Метод возвращает количество свободных разрешений к ресурсу

```
import java.util.concurrent.Semaphore;

public class Main {
  public static void main(String[] args) {
    Semaphore semaphore = new Semaphore(3);

    System.out.println(semaphore.availablePermits()); // 3
  }
}
```


## Метод `acquire`

1. Метод оповещает о том, что одно разрешение на данный момент используется

```
import java.util.concurrent.Semaphore;

public class Main {
  public static void main(String[] args) {
    Semaphore semaphore = new Semaphore(3);

    try {
      semaphore.acquire();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    System.out.println(semaphore.availablePermits()); // 2
  }
}
```

2. Если метод был вызван например 4м потоком, когда разрешений всего 3, то поток будет ожидать, пока какой-нибудь из потоков не вызовет метод `release`

```
import java.util.concurrent.Semaphore;

public class Main {
  public static void main(String[] args) {
    Semaphore semaphore = new Semaphore(3);

    try {
      semaphore.acquire();
      semaphore.acquire();
      semaphore.acquire();

      System.out.println("All permits have been acquired");

      semaphore.acquire();

      System.out.println("Can't reach here...");
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    System.out.println(semaphore.availablePermits());
  }
}
```

## Метод `release`

1. Метод оповещает о том, что разрешение более не используется

```
import java.util.concurrent.Semaphore;

public class Main {
  public static void main(String[] args) {
    Semaphore semaphore = new Semaphore(3);

    try {
      semaphore.acquire();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    
    semaphore.release()

    System.out.println(semaphore.availablePermits()); // 3
  }
}
```

## Пример

```
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Main {
  public static void main(String[] args) throws InterruptedException {
    ExecutorService executorService = Executors.newFixedThreadPool(200);

    Connection connection = Connection.getConnection();

    for (int i = 0; i < 200; i++) {
      executorService.submit(new Runnable() {
        @Override
        public void run() {
          try {
            connection.work();
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }
      });
    }

    executorService.shutdown();
    executorService.awaitTermination(1, TimeUnit.DAYS);
  }
}

// Singleton
class Connection {
  private static Connection connection = new Connection();
  private int connectionsCount;
  private Semaphore semaphore = new Semaphore(10);

  private Connection () {

  }

  public static Connection getConnection () {
    return connection;
  }

  public void work () throws InterruptedException {
    try {
      semaphore.acquire();
      
      doWork();
    } finally {
      semaphore.release();
    }
  }

  private void doWork () throws InterruptedException {
    synchronized (this) {
      connectionsCount++;
      System.out.println("Connection added. Total " + connectionsCount);
    }

    Thread.sleep(5000);

    synchronized (this) {
      connectionsCount--;
      System.out.println("Connections removed. Total " + connectionsCount);
    }
  }
}
```