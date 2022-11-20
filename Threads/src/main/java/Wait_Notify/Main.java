package Wait_Notify;

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
