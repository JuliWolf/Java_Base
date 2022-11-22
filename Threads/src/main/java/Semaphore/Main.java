package Semaphore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Main {
//  public static void main(String[] args) {
//    Semaphore semaphore = new Semaphore(3);
//
////    semaphore.release();
//
//    try {
//      semaphore.acquire();
//      semaphore.acquire();
//      semaphore.acquire();
//
//      System.out.println("All permits have been acquired");
//
//      semaphore.acquire();
//
//      System.out.println("Can't reach here...");
//    } catch (InterruptedException e) {
//      throw new RuntimeException(e);
//    }
//
//    System.out.println(semaphore.availablePermits());
//  }

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
