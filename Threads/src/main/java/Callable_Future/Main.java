package Callable_Future;

import java.util.Random;
import java.util.concurrent.*;

public class Main {
//  private static int result;
//
//  public static void main(String[] args) throws InterruptedException {
//    ExecutorService executorService = Executors.newFixedThreadPool(1);
//    executorService.submit(new Runnable() {
//      @Override
//      public void run() {
//        try {
//          System.out.println("Starting");
//
//          Thread.sleep(3000);
//        } catch (InterruptedException e) {
//          throw new RuntimeException(e);
//        }
//
//        System.out.println("Finished");
//        result = 5;
//      }
//    });
//
//    executorService.shutdown();
//
//    executorService.awaitTermination(1, TimeUnit.DAYS);
//
//    System.out.println(result);
//  }
//
//  public static int calculate () {
//    return 5 + 4;
//  }

  public static void main(String[] args) {
    ExecutorService executorService = Executors.newFixedThreadPool(1);
    Future<Integer> future = executorService.submit(() -> {
      try {
        System.out.println("Starting");

        Thread.sleep(500);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

      System.out.println("Finished");

      Random random = new Random();

      int randomValue = random.nextInt(10);

      if (randomValue < 5) {
        throw new Exception("Something bad happened!");
      }

      return randomValue;
    });

    executorService.shutdown();

    try {
      int result = future.get(); // get дожидается окончания выполнения потока
      System.out.println(result);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      Throwable ex = e.getCause();

      System.out.println(ex);
    }

  }
}
