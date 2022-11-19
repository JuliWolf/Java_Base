package Join;

public class Main {
  private int counter;

  public static void main(String[] args) throws InterruptedException {
    Synchronized.Main main = new Synchronized.Main();

    main.doWork();
  }

  public void doWork() throws InterruptedException {
    Thread thread1 = new Thread(new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < 10000; i++) {
          counter++;
        }
      }
    });

    Thread thread2 = new Thread(new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < 10000; i++) {
          counter++;
        }
      }
    });

    thread1.start();
    thread2.start();

    thread1.join();
    thread2.join();

    System.out.println(counter);
  }
}
